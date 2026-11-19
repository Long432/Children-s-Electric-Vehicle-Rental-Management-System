package org.group6.childrenelectricvehiclerentalmanagement.Services;

import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Session.SessionRevenueResponse;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Session.SessionStatsResponse;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.PaymentMethod;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.PaymentType;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.SessionStatus;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.TicketStatus;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.AbstractException;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.NoActiveSessionException;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.SessionNotExits;
import org.group6.childrenelectricvehiclerentalmanagement.Models.Session;
import org.group6.childrenelectricvehiclerentalmanagement.Repositories.PaymentRepository;
import org.group6.childrenelectricvehiclerentalmanagement.Repositories.SesssionRepository;
import org.group6.childrenelectricvehiclerentalmanagement.Repositories.TicketRepository;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.Validators.PageRules;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class SessionStatsService {

    private static final int DEFAULT_REVENUE_LIMIT = 10;

    private final SesssionRepository sessionRepository;
    private final TicketRepository ticketRepository;
    private final PaymentRepository paymentRepository;

    public SessionStatsService(SesssionRepository sessionRepository,
                               TicketRepository ticketRepository,
                               PaymentRepository paymentRepository) {
        this.sessionRepository = sessionRepository;
        this.ticketRepository = ticketRepository;
        this.paymentRepository = paymentRepository;
    }

    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public SessionStatsResponse current() throws AbstractException {
        Session session = sessionRepository.findFirstByStatus(SessionStatus.InProgress).orElseThrow(NoActiveSessionException::new);
        return build(session);
    }

    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public SessionStatsResponse get(long id) throws AbstractException {
        Session session = sessionRepository.findById(id).orElseThrow(SessionNotExits::new);
        return build(session);
    }

    private SessionStatsResponse build(Session session) {
        Map<String, Long> counts = new LinkedHashMap<>();
        for (TicketStatus status : TicketStatus.values()) {
            counts.put(status.name(), 0L);
        }
        long ticketCount = 0;
        for (Object[] row : ticketRepository.countGroupedByStatus(session.getId())) {
            long value = ((Number) row[1]).longValue();
            counts.put(String.valueOf(row[0]), value);
            ticketCount += value;
        }

        Totals totals = new Totals();
        for (Object[] row : paymentRepository.sumGroupedByTypeAndMethod(session.getId())) {
            totals.add(PaymentType.valueOf(String.valueOf(row[0])), PaymentMethod.valueOf(String.valueOf(row[1])), ((Number) row[2]).longValue());
        }
        return new SessionStatsResponse(session.getId(), session.getStatus(), ticketCount, counts,
                totals.cash, totals.bank, totals.total(), totals.ticketRevenue, totals.overdueFee, totals.refundTotal);
    }

    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public List<SessionRevenueResponse> revenue(Integer limit) throws AbstractException {
        PageRules.check(null, limit);
        int size = limit == null ? DEFAULT_REVENUE_LIMIT : limit;
        Pageable pageable = PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "endedAt", "id"));
        List<Session> sessions = sessionRepository.findByStatus(SessionStatus.Ended, pageable).getContent();
        if (sessions.isEmpty()) {
            return List.of();
        }
        List<Long> ids = sessions.stream().map(Session::getId).toList();

        Map<Long, Totals> totalsBySession = new HashMap<>();
        for (Object[] row : paymentRepository.sumGroupedBySessionTypeAndMethod(ids)) {
            long sessionId = ((Number) row[0]).longValue();
            PaymentType type = PaymentType.valueOf(String.valueOf(row[1]));
            PaymentMethod method = PaymentMethod.valueOf(String.valueOf(row[2]));
            totalsBySession.computeIfAbsent(sessionId, key -> new Totals()).add(type, method, ((Number) row[3]).longValue());
        }
        Map<Long, Long> ticketCounts = new HashMap<>();
        for (Object[] row : ticketRepository.countGroupedBySession(ids)) {
            ticketCounts.put(((Number) row[0]).longValue(), ((Number) row[1]).longValue());
        }

        return sessions.stream().map(session -> {
            Totals totals = totalsBySession.getOrDefault(session.getId(), new Totals());
            return SessionRevenueResponse.from(session, ticketCounts.getOrDefault(session.getId(), 0L),
                    totals.cash, totals.bank, totals.overdueFee, totals.refundTotal);
        }).toList();
    }

    private static final class Totals {
        private long cash;
        private long bank;
        private long ticketRevenue;
        private long overdueFee;
        private long refundTotal;

        private void add(PaymentType type, PaymentMethod method, long amount) {
            long signed = type == PaymentType.Refund ? -amount : amount;
            if (method == PaymentMethod.Cash) {
                cash += signed;
            } else {
                bank += signed;
            }
            switch (type) {
                case Ticket, Extension -> ticketRevenue += amount;
                case OverdueFee -> overdueFee += amount;
                case Refund -> refundTotal += amount;
            }
        }

        private long total() {
            return cash + bank;
        }
    }
}
