package org.group6.childrenelectricvehiclerentalmanagement.Services;

import org.group6.childrenelectricvehiclerentalmanagement.DTOs.PageResponse;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Report.AccountReportRow;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Report.IncidentRow;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Report.ParticipantRow;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Report.ReportSummaryResponse;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Report.SessionReportDetail;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Report.SessionReportRow;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Report.TimelineEvent;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Report.UserRef;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Report.VehicleReportRow;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.UserRole;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.NoPermission;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.SessionNotExits;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.ValidationException;
import org.group6.childrenelectricvehiclerentalmanagement.Repositories.ReportQueries;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.BusinessTime;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.Security.AuthenticatedUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReportServiceTest {

    private static final Instant NOW = Instant.parse("2026-11-11T05:00:00Z");

    private final BusinessTime businessTime = new BusinessTime(Clock.fixed(NOW, ZoneOffset.UTC), "Asia/Ho_Chi_Minh");
    private final AuthenticatedUser owner = new AuthenticatedUser(1L, "chu", null, UserRole.Owner);
    private final AuthenticatedUser staff = new AuthenticatedUser(2L, "an");
    private FakeQueries queries;
    private ReportService service;

    @BeforeEach
    void setUp() {
        queries = new FakeQueries();
        queries.users.add(new ReportQueries.UserRow(1L, "chu", "Owner", "Online"));
        queries.users.add(new ReportQueries.UserRow(2L, "an", "Staff", "Offline"));
        queries.users.add(new ReportQueries.UserRow(3L, "binh", "Staff", "Offline"));
        queries.users.add(new ReportQueries.UserRow(5L, "dev", "Staff", "Deactivate"));
        queries.sessions.add(session(3L, "2026-11-11", "InProgress", "2026-11-11T03:00:00", null, 2L, "an", 3L, "binh", null, 3, 0, 100000, 50000, 0, 0, 0));
        queries.sessions.add(session(2L, "2026-11-10", "Ended", "2026-11-10T02:00:00", "2026-11-10T08:00:00", 3L, "binh", 3L, "binh", 3L, 5, 0, 300000, 100000, 0, 0, 0));
        queries.sessions.add(session(1L, "2026-11-10", "Ended", "2026-11-10T01:00:00", "2026-11-10T09:30:00", 2L, "an", 3L, "binh", 2L, 10, 1, 800000, 400000, 12000, 50000, 1));
        service = new ReportService(queries, businessTime);
    }

    private ReportQueries.SessionRow session(long id, String date, String status, String start, String end, long chargeId, String chargeName,
                                             long createdId, String createdName, Long endedId, long tickets, long canceled,
                                             long cash, long bank, long overdue, long refund, long incidents) {
        return new ReportQueries.SessionRow(id, LocalDate.parse(date), status, LocalDateTime.parse(start),
                end == null ? null : LocalDateTime.parse(end), createdId, createdName, chargeId, chargeName,
                endedId, endedId == null ? null : (endedId == chargeId ? chargeName : createdName),
                tickets, canceled, cash, bank, overdue, refund, incidents);
    }

    @Test
    void summaryTotalsRevenueAndFillsEveryDayOfDefaultRange() throws Exception {
        ReportSummaryResponse summary = service.summary(owner, null, null, null);

        assertEquals("2026-10-13", summary.from());
        assertEquals("2026-11-11", summary.to());
        assertEquals(3L, summary.session_count());
        assertEquals(990L, summary.work_minutes());
        assertEquals(18L, summary.ticket_count());
        assertEquals(1L, summary.canceled_ticket_count());
        assertEquals(1200000L, summary.cash_revenue());
        assertEquals(550000L, summary.bank_revenue());
        assertEquals(1750000L, summary.total_revenue());
        assertEquals(12000L, summary.overdue_fee());
        assertEquals(50000L, summary.refund_total());
        assertEquals(1L, summary.incident_count());
        assertEquals(106061L, summary.revenue_per_hour());
        assertEquals(30, summary.daily().size());
        assertEquals("2026-10-13", summary.daily().get(0).date());
        assertEquals(0L, summary.daily().get(0).total_revenue());
        assertEquals(2L, summary.daily().get(28).session_count());
        assertEquals(15L, summary.daily().get(28).ticket_count());
        assertEquals(1100000L, summary.daily().get(28).cash_revenue());
        assertEquals(500000L, summary.daily().get(28).bank_revenue());
        assertEquals(1600000L, summary.daily().get(28).total_revenue());
        assertEquals(150000L, summary.daily().get(29).total_revenue());
        assertEquals(LocalDate.parse("2026-10-13"), queries.lastFrom);
        assertEquals(LocalDate.parse("2026-11-11"), queries.lastTo);
        assertNull(queries.lastScope);
    }

    @Test
    void summaryForStaffIsLimitedToTheirOwnSessions() throws Exception {
        ReportSummaryResponse summary = service.summary(staff, "2026-11-01", "2026-11-11", null);

        assertEquals(Long.valueOf(2L), queries.lastScope);
        assertEquals(2L, summary.session_count());
        assertEquals(1350000L, summary.total_revenue());
        assertEquals(11, summary.daily().size());
    }

    @Test
    void staffMayPassTheirOwnAccountId() throws Exception {
        service.summary(staff, null, null, 2L);

        assertEquals(Long.valueOf(2L), queries.lastScope);
    }

    @Test
    void staffCannotReadAnotherAccount() {
        assertThrows(NoPermission.class, () -> service.summary(staff, null, null, 3L));
        assertThrows(NoPermission.class, () -> service.sessions(staff, null, null, 3L, null, null));
        assertThrows(NoPermission.class, () -> service.incidents(staff, null, null, 3L, null, null));
        assertThrows(NoPermission.class, () -> service.vehicles(staff, null, null, 3L));
    }

    @Test
    void ownerCanFilterByAccount() throws Exception {
        ReportSummaryResponse summary = service.summary(owner, null, null, 3L);

        assertEquals(Long.valueOf(3L), queries.lastScope);
        assertEquals(1L, summary.session_count());
        assertEquals(400000L, summary.total_revenue());
    }

    @Test
    void rejectsInvalidDateRanges() {
        assertThrows(ValidationException.class, () -> service.summary(owner, "11/01/2026", null, null));
        assertThrows(ValidationException.class, () -> service.summary(owner, null, "abc", null));
        assertThrows(ValidationException.class, () -> service.summary(owner, "2026-11-12", "2026-11-11", null));
        assertThrows(ValidationException.class, () -> service.summary(owner, "2025-01-01", "2026-11-11", null));
        assertThrows(ValidationException.class, () -> service.summary(owner, null, null, 0L));
    }

    @Test
    void acceptsTheLongestAllowedRange() throws Exception {
        ReportSummaryResponse summary = service.summary(owner, "2025-11-11", "2026-11-11", null);

        assertEquals(366, summary.daily().size());
    }

    @Test
    void accountsAreRankedByRevenueAndHideIdleDeactivatedUsers() throws Exception {
        List<AccountReportRow> accounts = service.accounts(owner, null, null);

        assertEquals(List.of("an", "binh", "chu"), accounts.stream().map(AccountReportRow::username).toList());
        AccountReportRow an = accounts.get(0);
        assertEquals(2L, an.session_count());
        assertEquals(630L, an.work_minutes());
        assertEquals(13L, an.ticket_count());
        assertEquals(1L, an.canceled_ticket_count());
        assertEquals(900000L, an.cash_revenue());
        assertEquals(450000L, an.bank_revenue());
        assertEquals(1350000L, an.total_revenue());
        assertEquals(12000L, an.overdue_fee());
        assertEquals(50000L, an.refund_total());
        assertEquals(1L, an.incident_count());
        assertEquals(128571L, an.revenue_per_hour());
        AccountReportRow binh = accounts.get(1);
        assertEquals(1L, binh.session_count());
        assertEquals(360L, binh.work_minutes());
        assertEquals(66667L, binh.revenue_per_hour());
        AccountReportRow chu = accounts.get(2);
        assertEquals(0L, chu.session_count());
        assertEquals(0L, chu.revenue_per_hour());
        assertEquals("Owner", chu.role());
        assertNull(queries.lastUsersScope);
    }

    @Test
    void accountsSumToTheSummaryTotal() throws Exception {
        long fromAccounts = service.accounts(owner, null, null).stream().mapToLong(AccountReportRow::total_revenue).sum();

        assertEquals(service.summary(owner, null, null, null).total_revenue(), fromAccounts);
    }

    @Test
    void deactivatedUserWithSessionsStaysInTheList() throws Exception {
        queries.sessions.add(session(9L, "2026-11-09", "Ended", "2026-11-09T01:00:00", "2026-11-09T02:00:00", 5L, "dev", 5L, "dev", 5L, 1, 0, 10000, 0, 0, 0, 0));

        List<AccountReportRow> accounts = service.accounts(owner, null, null);

        assertTrue(accounts.stream().anyMatch(account -> account.username().equals("dev") && account.session_count() == 1));
    }

    @Test
    void staffSeesOnlyTheirOwnAccountRow() throws Exception {
        List<AccountReportRow> accounts = service.accounts(staff, null, null);

        assertEquals(1, accounts.size());
        assertEquals("an", accounts.get(0).username());
        assertEquals(Long.valueOf(2L), queries.lastUsersScope);
        assertEquals(Long.valueOf(2L), queries.lastScope);
    }

    @Test
    void sessionListIsPagedAndKeepsTheTotal() throws Exception {
        PageResponse<SessionReportRow> first = service.sessions(owner, null, null, null, 1, 2);
        PageResponse<SessionReportRow> second = service.sessions(owner, null, null, null, 2, 2);
        PageResponse<SessionReportRow> beyond = service.sessions(owner, null, null, null, 5, 2);

        assertEquals(3L, first.total());
        assertEquals(List.of(3L, 2L), first.items().stream().map(SessionReportRow::session_id).toList());
        assertEquals(List.of(1L), second.items().stream().map(SessionReportRow::session_id).toList());
        assertTrue(beyond.items().isEmpty());
        assertEquals(3L, beyond.total());
        assertThrows(ValidationException.class, () -> service.sessions(owner, null, null, null, 0, 10));
        assertThrows(ValidationException.class, () -> service.sessions(owner, null, null, null, 1, 101));
    }

    @Test
    void sessionRowNamesWhoIsInChargeAndWorkTime() throws Exception {
        SessionReportRow running = service.sessions(owner, null, null, null, null, null).items().get(0);
        SessionReportRow ended = service.sessions(owner, null, null, null, null, null).items().get(2);

        assertEquals(new UserRef(2L, "an"), running.in_charge());
        assertEquals(120L, running.work_minutes());
        assertNull(running.ended_at());
        assertNull(running.ended_by());
        assertEquals("2026-11-10T01:00:00Z", ended.started_at());
        assertEquals(new UserRef(3L, "binh"), ended.created_by());
        assertEquals(new UserRef(2L, "an"), ended.ended_by());
        assertEquals(510L, ended.work_minutes());
        assertEquals(1200000L, ended.total_revenue());
    }

    @Test
    void sessionDetailAllowsOwnerInChargeAndParticipants() throws Exception {
        queries.participants.add(new ParticipantRow(2L, "an", 4L, 3L, 1L, 800000L, 400000L, 1200000L));
        queries.participants.add(new ParticipantRow(4L, "chi", 6L, 6L, 0L, 0L, 0L, 0L));
        queries.incidents.add(new IncidentRow(1L, "2026-11-10T03:00:00Z", 2L, "X2", 1L, 7L, 3, "Ticket", new UserRef(2L, "an"), new UserRef(2L, "an")));
        queries.timeline.add(new TimelineEvent("2026-11-10T01:00:00Z", new UserRef(2L, "an"), "session.start", "session", 1L, "{}"));

        SessionReportDetail asOwner = service.session(owner, 1L);
        SessionReportDetail asCharge = service.session(staff, 1L);
        SessionReportDetail asParticipant = service.session(new AuthenticatedUser(4L, "chi"), 1L);

        assertEquals(1L, asOwner.session().session_id());
        assertEquals(2, asOwner.participants().size());
        assertEquals(1, asOwner.incidents().size());
        assertEquals(1, asOwner.timeline().size());
        assertEquals(1L, asCharge.session().session_id());
        assertEquals(1L, asParticipant.session().session_id());
        assertEquals(Long.valueOf(1L), queries.lastIncidentSession);
        assertNull(queries.lastIncidentScope);
    }

    @Test
    void sessionDetailRejectsStrangersMissingAndInvalidIds() {
        assertThrows(NoPermission.class, () -> service.session(new AuthenticatedUser(4L, "chi"), 1L));
        assertThrows(SessionNotExits.class, () -> service.session(owner, 99L));
        assertThrows(ValidationException.class, () -> service.session(owner, 0L));
    }

    @Test
    void incidentsUseVietnamDayBoundsAndDefaultLimit() throws Exception {
        service.incidents(owner, "2026-11-10", "2026-11-11", null, 2L, null);

        assertEquals(LocalDateTime.parse("2026-11-09T17:00:00"), queries.lastFromUtc);
        assertEquals(LocalDateTime.parse("2026-11-11T17:00:00"), queries.lastToUtc);
        assertEquals(50, queries.lastLimit);
        assertEquals(Long.valueOf(2L), queries.lastVehicleId);
        assertNull(queries.lastIncidentScope);

        service.incidents(staff, null, null, null, null, 10);

        assertEquals(10, queries.lastLimit);
        assertEquals(Long.valueOf(2L), queries.lastIncidentScope);
        assertThrows(ValidationException.class, () -> service.incidents(owner, null, null, null, 0L, null));
        assertThrows(ValidationException.class, () -> service.incidents(owner, null, null, null, null, 0));
        assertThrows(ValidationException.class, () -> service.incidents(owner, null, null, null, null, 101));
    }

    @Test
    void vehicleReportFollowsTheSameScopeRules() throws Exception {
        queries.vehicles.add(new VehicleReportRow(2L, "X2", "Repairing", 3L, "2026-11-10T03:00:00Z"));

        List<VehicleReportRow> asOwner = service.vehicles(owner, null, null, null);

        assertEquals(1, asOwner.size());
        assertNull(queries.lastVehicleScope);
        service.vehicles(staff, null, null, null);
        assertEquals(Long.valueOf(2L), queries.lastVehicleScope);
        assertEquals(LocalDateTime.parse("2026-10-12T17:00:00"), queries.lastFromUtc);
        assertEquals(LocalDateTime.parse("2026-11-11T17:00:00"), queries.lastToUtc);
    }

    private static final class FakeQueries implements ReportQueries {
        final List<SessionRow> sessions = new ArrayList<>();
        final List<UserRow> users = new ArrayList<>();
        final List<ParticipantRow> participants = new ArrayList<>();
        final List<IncidentRow> incidents = new ArrayList<>();
        final List<TimelineEvent> timeline = new ArrayList<>();
        final List<VehicleReportRow> vehicles = new ArrayList<>();
        LocalDate lastFrom;
        LocalDate lastTo;
        Long lastScope;
        Long lastUsersScope;
        LocalDateTime lastFromUtc;
        LocalDateTime lastToUtc;
        Long lastIncidentScope;
        Long lastVehicleId;
        Long lastIncidentSession;
        Long lastVehicleScope;
        int lastLimit;

        @Override
        public List<SessionRow> sessions(LocalDate from, LocalDate to, Long startedBy, Long sessionId) {
            lastFrom = from;
            lastTo = to;
            lastScope = startedBy;
            return sessions.stream()
                    .filter(row -> sessionId == null || row.id() == sessionId)
                    .filter(row -> from == null || !row.scheduledDate().isBefore(from))
                    .filter(row -> to == null || !row.scheduledDate().isAfter(to))
                    .filter(row -> startedBy == null || row.startedById() == startedBy)
                    .toList();
        }

        @Override
        public List<UserRow> users(Long onlyUserId) {
            lastUsersScope = onlyUserId;
            return users.stream().filter(user -> onlyUserId == null || user.id() == onlyUserId).toList();
        }

        @Override
        public List<ParticipantRow> participants(long sessionId) {
            return participants;
        }

        @Override
        public List<IncidentRow> incidents(LocalDateTime fromUtc, LocalDateTime toUtc, Long scopeUserId, Long vehicleId, Long sessionId, int limit) {
            lastFromUtc = fromUtc;
            lastToUtc = toUtc;
            lastIncidentScope = scopeUserId;
            lastVehicleId = vehicleId;
            lastIncidentSession = sessionId;
            lastLimit = limit;
            return incidents;
        }

        @Override
        public List<TimelineEvent> timeline(long sessionId, int limit) {
            return timeline;
        }

        @Override
        public List<VehicleReportRow> vehicles(LocalDateTime fromUtc, LocalDateTime toUtc, Long scopeUserId) {
            lastFromUtc = fromUtc;
            lastToUtc = toUtc;
            lastVehicleScope = scopeUserId;
            return vehicles;
        }
    }
}
