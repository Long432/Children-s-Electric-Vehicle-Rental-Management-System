package org.group6.childrenelectricvehiclerentalmanagement.Services;

import org.group6.childrenelectricvehiclerentalmanagement.DTOs.PageResponse;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Report.AccountReportRow;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Report.DailyRevenue;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Report.IncidentRow;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Report.ParticipantRow;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Report.ReportSummaryResponse;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Report.SessionReportDetail;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Report.SessionReportRow;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Report.UserRef;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Report.VehicleReportRow;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.SessionStatus;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.AbstractException;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.NoPermission;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.SessionNotExits;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.ValidationException;
import org.group6.childrenelectricvehiclerentalmanagement.Repositories.ReportQueries;
import org.group6.childrenelectricvehiclerentalmanagement.Repositories.ReportQueries.SessionRow;
import org.group6.childrenelectricvehiclerentalmanagement.Repositories.ReportQueries.UserRow;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.BusinessTime;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.SessionTimeRules;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.Security.AuthenticatedUser;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.TimeUtil;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.Validators.PageRules;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private static final int DEFAULT_RANGE_DAYS = 30;
    private static final int MAX_RANGE_DAYS = 366;
    private static final int DEFAULT_INCIDENT_LIMIT = 50;
    private static final int TIMELINE_LIMIT = 500;
    private static final int DETAIL_INCIDENT_LIMIT = 200;

    private final ReportQueries queries;
    private final BusinessTime businessTime;

    public ReportService(ReportQueries queries, BusinessTime businessTime) {
        this.queries = queries;
        this.businessTime = businessTime;
    }

    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public ReportSummaryResponse summary(AuthenticatedUser actor, String from, String to, Long userId) throws AbstractException {
        Range range = range(from, to);
        List<SessionReportRow> rows = sessionRows(range, scope(actor, userId));
        long minutes = sum(rows, SessionReportRow::work_minutes);
        long total = sum(rows, SessionReportRow::total_revenue);
        return new ReportSummaryResponse(
                range.from().toString(),
                range.to().toString(),
                rows.size(),
                minutes,
                sum(rows, SessionReportRow::ticket_count),
                sum(rows, SessionReportRow::canceled_ticket_count),
                sum(rows, SessionReportRow::cash_revenue),
                sum(rows, SessionReportRow::bank_revenue),
                total,
                sum(rows, SessionReportRow::overdue_fee),
                sum(rows, SessionReportRow::refund_total),
                sum(rows, SessionReportRow::incident_count),
                perHour(total, minutes),
                daily(range, rows));
    }

    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public List<AccountReportRow> accounts(AuthenticatedUser actor, String from, String to) throws AbstractException {
        Range range = range(from, to);
        Long scope = scope(actor, null);
        Map<Long, List<SessionReportRow>> byUser = sessionRows(range, scope).stream()
                .collect(Collectors.groupingBy(row -> row.in_charge().id()));
        List<AccountReportRow> result = new ArrayList<>();
        for (UserRow user : queries.users(scope)) {
            List<SessionReportRow> own = byUser.getOrDefault(user.id(), List.of());
            if (own.isEmpty() && "Deactivate".equals(user.status())) {
                continue;
            }
            result.add(account(user, own));
        }
        result.sort(Comparator.comparingLong(AccountReportRow::total_revenue).reversed()
                .thenComparing(AccountReportRow::username));
        return result;
    }

    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public PageResponse<SessionReportRow> sessions(AuthenticatedUser actor, String from, String to, Long userId, Integer page, Integer limit) throws AbstractException {
        PageRules.check(page, limit);
        Range range = range(from, to);
        List<SessionReportRow> rows = sessionRows(range, scope(actor, userId));
        int pageNumber = PageRules.page(page);
        int pageSize = PageRules.limit(limit);
        int start = (int) Math.min((long) (pageNumber - 1) * pageSize, rows.size());
        int end = Math.min(rows.size(), start + pageSize);
        return new PageResponse<>(rows.subList(start, end), pageNumber, pageSize, rows.size());
    }

    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public SessionReportDetail session(AuthenticatedUser actor, long id) throws AbstractException {
        if (id < 1) {
            throw new ValidationException("id", "Mã phiên không hợp lệ");
        }
        SessionRow found = queries.sessions(null, null, null, id).stream().findFirst().orElseThrow(SessionNotExits::new);
        List<ParticipantRow> participants = queries.participants(id);
        boolean involved = found.startedById() == actor.id()
                || participants.stream().anyMatch(participant -> participant.user_id() == actor.id());
        if (!actor.isOwner() && !involved) {
            throw new NoPermission();
        }
        return new SessionReportDetail(
                toRow(found),
                participants,
                queries.incidents(null, null, null, null, id, DETAIL_INCIDENT_LIMIT),
                queries.timeline(id, TIMELINE_LIMIT));
    }

    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public List<IncidentRow> incidents(AuthenticatedUser actor, String from, String to, Long userId, Long vehicleId, Integer limit) throws AbstractException {
        PageRules.check(null, limit);
        requireVehicleId(vehicleId);
        Range range = range(from, to);
        int size = limit == null ? DEFAULT_INCIDENT_LIMIT : limit;
        return queries.incidents(startUtc(range), endUtc(range), scope(actor, userId), vehicleId, null, size);
    }

    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public List<VehicleReportRow> vehicles(AuthenticatedUser actor, String from, String to, Long userId) throws AbstractException {
        Range range = range(from, to);
        return queries.vehicles(startUtc(range), endUtc(range), scope(actor, userId));
    }

    private record Range(LocalDate from, LocalDate to) {
    }

    private Range range(String from, String to) throws ValidationException {
        LocalDate end = isBlank(to) ? businessTime.today() : SessionTimeRules.parseDate("to", to);
        LocalDate start = isBlank(from) ? end.minusDays(DEFAULT_RANGE_DAYS - 1) : SessionTimeRules.parseDate("from", from);
        if (start.isAfter(end)) {
            throw new ValidationException("from", "Ngày bắt đầu phải trước hoặc bằng ngày kết thúc");
        }
        if (ChronoUnit.DAYS.between(start, end) + 1 > MAX_RANGE_DAYS) {
            throw new ValidationException("to", "Khoảng thời gian tối đa " + MAX_RANGE_DAYS + " ngày");
        }
        return new Range(start, end);
    }

    private Long scope(AuthenticatedUser actor, Long requestedUserId) throws NoPermission, ValidationException {
        if (requestedUserId != null && requestedUserId < 1) {
            throw new ValidationException("user_id", "Mã tài khoản không hợp lệ");
        }
        if (actor.isOwner()) {
            return requestedUserId;
        }
        if (requestedUserId != null && requestedUserId != actor.id()) {
            throw new NoPermission();
        }
        return actor.id();
    }

    private void requireVehicleId(Long vehicleId) throws ValidationException {
        if (vehicleId != null && vehicleId < 1) {
            throw new ValidationException("vehicle_id", "Mã xe không hợp lệ");
        }
    }

    private LocalDateTime startUtc(Range range) {
        return businessTime.toUtc(range.from(), LocalTime.MIDNIGHT);
    }

    private LocalDateTime endUtc(Range range) {
        return businessTime.toUtc(range.to().plusDays(1), LocalTime.MIDNIGHT);
    }

    private List<SessionReportRow> sessionRows(Range range, Long scope) {
        return queries.sessions(range.from(), range.to(), scope, null).stream().map(this::toRow).toList();
    }

    private SessionReportRow toRow(SessionRow row) {
        return new SessionReportRow(
                row.id(),
                row.scheduledDate().toString(),
                row.status(),
                TimeUtil.toIsoUtc(row.startedAt()),
                TimeUtil.toIsoUtc(row.endedAt()),
                workMinutes(row),
                new UserRef(row.startedById(), row.startedByName()),
                new UserRef(row.createdById(), row.createdByName()),
                row.endedById() == null ? null : new UserRef(row.endedById(), row.endedByName()),
                row.ticketCount(),
                row.canceledCount(),
                row.cash(),
                row.bank(),
                row.cash() + row.bank(),
                row.overdueFee(),
                row.refundTotal(),
                row.incidentCount());
    }

    private long workMinutes(SessionRow row) {
        if (row.startedAt() == null) {
            return 0;
        }
        LocalDateTime end = row.endedAt();
        if (end == null) {
            end = SessionStatus.InProgress.name().equals(row.status()) ? businessTime.nowUtc() : row.startedAt();
        }
        return Math.max(0, Duration.between(row.startedAt(), end).toMinutes());
    }

    private AccountReportRow account(UserRow user, List<SessionReportRow> rows) {
        long minutes = sum(rows, SessionReportRow::work_minutes);
        long total = sum(rows, SessionReportRow::total_revenue);
        return new AccountReportRow(
                user.id(),
                user.username(),
                user.role(),
                user.status(),
                rows.size(),
                minutes,
                sum(rows, SessionReportRow::ticket_count),
                sum(rows, SessionReportRow::canceled_ticket_count),
                sum(rows, SessionReportRow::cash_revenue),
                sum(rows, SessionReportRow::bank_revenue),
                total,
                sum(rows, SessionReportRow::overdue_fee),
                sum(rows, SessionReportRow::refund_total),
                sum(rows, SessionReportRow::incident_count),
                perHour(total, minutes));
    }

    private List<DailyRevenue> daily(Range range, List<SessionReportRow> rows) {
        Map<String, List<SessionReportRow>> byDate = rows.stream()
                .collect(Collectors.groupingBy(SessionReportRow::scheduled_date));
        Map<String, DailyRevenue> days = new LinkedHashMap<>();
        for (LocalDate day = range.from(); !day.isAfter(range.to()); day = day.plusDays(1)) {
            List<SessionReportRow> own = byDate.getOrDefault(day.toString(), List.of());
            days.put(day.toString(), new DailyRevenue(
                    day.toString(),
                    own.size(),
                    sum(own, SessionReportRow::ticket_count),
                    sum(own, SessionReportRow::cash_revenue),
                    sum(own, SessionReportRow::bank_revenue),
                    sum(own, SessionReportRow::total_revenue)));
        }
        return new ArrayList<>(days.values());
    }

    private static long sum(List<SessionReportRow> rows, ToLongFunction<SessionReportRow> field) {
        long total = 0;
        for (SessionReportRow row : rows) {
            total += field.applyAsLong(row);
        }
        return total;
    }

    private static long perHour(long total, long minutes) {
        return minutes > 0 ? Math.round(total * 60.0 / minutes) : 0;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
