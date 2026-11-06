package org.group6.childrenelectricvehiclerentalmanagement.Services;

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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class SessionStatsService {

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

        long cash = 0;
        long bank = 0;
        long ticketRevenue = 0;
        long overdueFee = 0;
        long refundTotal = 0;
        for (Object[] row : paymentRepository.sumGroupedByTypeAndMethod(session.getId())) {
            PaymentType type = PaymentType.valueOf(String.valueOf(row[0]));
            PaymentMethod method = PaymentMethod.valueOf(String.valueOf(row[1]));
            long amount = ((Number) row[2]).longValue();
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
        return new SessionStatsResponse(session.getId(), session.getStatus(), ticketCount, counts,
                cash, bank, cash + bank, ticketRevenue, overdueFee, refundTotal);
    }
}
