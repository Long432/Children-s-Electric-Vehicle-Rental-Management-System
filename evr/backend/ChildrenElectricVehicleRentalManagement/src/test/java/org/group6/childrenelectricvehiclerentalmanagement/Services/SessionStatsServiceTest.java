package org.group6.childrenelectricvehiclerentalmanagement.Services;

import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Session.SessionRevenueResponse;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Session.SessionStatsResponse;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.PaymentMethod;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.PaymentType;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.SessionStatus;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.TicketStatus;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.NoActiveSessionException;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.SessionNotExits;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.ValidationException;
import org.group6.childrenelectricvehiclerentalmanagement.Models.Session;
import org.group6.childrenelectricvehiclerentalmanagement.Repositories.PaymentRepository;
import org.group6.childrenelectricvehiclerentalmanagement.Repositories.SesssionRepository;
import org.group6.childrenelectricvehiclerentalmanagement.Repositories.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SessionStatsServiceTest {

    private final SesssionRepository sessionRepository = mock(SesssionRepository.class);
    private final TicketRepository ticketRepository = mock(TicketRepository.class);
    private final PaymentRepository paymentRepository = mock(PaymentRepository.class);
    private SessionStatsService service;

    @BeforeEach
    void setUp() {
        service = new SessionStatsService(sessionRepository, ticketRepository, paymentRepository);
    }

    private Session session(long id, SessionStatus status) {
        Session session = new Session();
        session.setId(id);
        session.setStatus(status);
        return session;
    }

    private Object[] row(Object... cells) {
        return cells;
    }

    @Test
    void currentSumsRevenueByMethodAndSubtractsRefund() throws Exception {
        when(sessionRepository.findFirstByStatus(SessionStatus.InProgress)).thenReturn(Optional.of(session(7L, SessionStatus.InProgress)));
        List<Object[]> tickets = new ArrayList<>();
        tickets.add(row(TicketStatus.Completed, 3L));
        tickets.add(row(TicketStatus.Canceled, 1L));
        tickets.add(row(TicketStatus.Waiting, 2L));
        when(ticketRepository.countGroupedByStatus(7L)).thenReturn(tickets);
        List<Object[]> payments = new ArrayList<>();
        payments.add(row(PaymentType.Ticket, PaymentMethod.Cash, 100000L));
        payments.add(row(PaymentType.Ticket, PaymentMethod.Bank, 50000L));
        payments.add(row(PaymentType.Extension, PaymentMethod.Cash, 20000L));
        payments.add(row(PaymentType.OverdueFee, PaymentMethod.Cash, 14000L));
        payments.add(row(PaymentType.Refund, PaymentMethod.Bank, 30000L));
        when(paymentRepository.sumGroupedByTypeAndMethod(7L)).thenReturn(payments);

        SessionStatsResponse stats = service.current();

        assertEquals(7L, stats.session_id());
        assertEquals(SessionStatus.InProgress, stats.status());
        assertEquals(6L, stats.ticket_count());
        assertEquals(3L, stats.ticket_counts().get("Completed"));
        assertEquals(1L, stats.ticket_counts().get("Canceled"));
        assertEquals(2L, stats.ticket_counts().get("Waiting"));
        assertEquals(0L, stats.ticket_counts().get("Inused"));
        assertEquals(0L, stats.ticket_counts().get("Overdue"));
        assertEquals(134000L, stats.cash_revenue());
        assertEquals(20000L, stats.bank_revenue());
        assertEquals(154000L, stats.total_revenue());
        assertEquals(170000L, stats.ticket_revenue());
        assertEquals(14000L, stats.overdue_fee());
        assertEquals(30000L, stats.refund_total());
        assertEquals(stats.ticket_revenue() + stats.overdue_fee() - stats.refund_total(), stats.total_revenue());
    }

    @Test
    void refundPaidInCashReducesOnlyCash() throws Exception {
        when(sessionRepository.findById(4L)).thenReturn(Optional.of(session(4L, SessionStatus.Ended)));
        when(ticketRepository.countGroupedByStatus(4L)).thenReturn(new ArrayList<>());
        List<Object[]> payments = new ArrayList<>();
        payments.add(row(PaymentType.Ticket, PaymentMethod.Bank, 90000L));
        payments.add(row(PaymentType.Refund, PaymentMethod.Cash, 90000L));
        when(paymentRepository.sumGroupedByTypeAndMethod(4L)).thenReturn(payments);

        SessionStatsResponse stats = service.get(4L);

        assertEquals(SessionStatus.Ended, stats.status());
        assertEquals(-90000L, stats.cash_revenue());
        assertEquals(90000L, stats.bank_revenue());
        assertEquals(0L, stats.total_revenue());
    }

    @Test
    void emptySessionReturnsZeros() throws Exception {
        when(sessionRepository.findById(5L)).thenReturn(Optional.of(session(5L, SessionStatus.Ended)));
        when(ticketRepository.countGroupedByStatus(5L)).thenReturn(new ArrayList<>());
        when(paymentRepository.sumGroupedByTypeAndMethod(5L)).thenReturn(new ArrayList<>());

        SessionStatsResponse stats = service.get(5L);

        assertEquals(0L, stats.ticket_count());
        assertEquals(TicketStatus.values().length, stats.ticket_counts().size());
        assertEquals(0L, stats.cash_revenue());
        assertEquals(0L, stats.bank_revenue());
        assertEquals(0L, stats.total_revenue());
        assertEquals(0L, stats.overdue_fee());
    }

    @Test
    void currentWithoutActiveSessionIsRejected() {
        when(sessionRepository.findFirstByStatus(SessionStatus.InProgress)).thenReturn(Optional.empty());

        assertThrows(NoActiveSessionException.class, () -> service.current());
    }

    @Test
    void unknownSessionIsRejected() {
        when(sessionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(SessionNotExits.class, () -> service.get(99L));
    }

    private Session endedSession(long id, String date, LocalDateTime endedAt) {
        Session session = session(id, SessionStatus.Ended);
        session.setScheduledDate(LocalDate.parse(date));
        session.setStartedAt(endedAt.minusHours(10));
        session.setEndedAt(endedAt);
        return session;
    }

    @Test
    void revenueGroupsPaymentsAndTicketsPerSession() throws Exception {
        LocalDateTime lastEnd = LocalDateTime.of(2026, 11, 6, 16, 0);
        Session newest = endedSession(9L, "2026-11-06", lastEnd);
        Session older = endedSession(8L, "2026-11-05", lastEnd.minusDays(1));
        Session quiet = endedSession(6L, "2026-11-03", lastEnd.minusDays(3));
        when(sessionRepository.findByStatus(eq(SessionStatus.Ended), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(newest, older, quiet)));
        List<Object[]> payments = new ArrayList<>();
        payments.add(row(9L, PaymentType.Ticket, PaymentMethod.Cash, 100000L));
        payments.add(row(9L, PaymentType.Ticket, PaymentMethod.Bank, 50000L));
        payments.add(row(9L, PaymentType.Extension, PaymentMethod.Cash, 20000L));
        payments.add(row(9L, PaymentType.OverdueFee, PaymentMethod.Bank, 14000L));
        payments.add(row(9L, PaymentType.Refund, PaymentMethod.Cash, 30000L));
        payments.add(row(8L, PaymentType.Ticket, PaymentMethod.Bank, 90000L));
        payments.add(row(8L, PaymentType.Refund, PaymentMethod.Cash, 90000L));
        when(paymentRepository.sumGroupedBySessionTypeAndMethod(List.of(9L, 8L, 6L))).thenReturn(payments);
        List<Object[]> counts = new ArrayList<>();
        counts.add(row(9L, 5L));
        counts.add(row(8L, 1L));
        when(ticketRepository.countGroupedBySession(List.of(9L, 8L, 6L))).thenReturn(counts);

        List<SessionRevenueResponse> result = service.revenue(null);

        assertEquals(3, result.size());
        assertEquals(List.of(9L, 8L, 6L), result.stream().map(SessionRevenueResponse::session_id).toList());

        SessionRevenueResponse first = result.get(0);
        assertEquals("2026-11-06", first.scheduled_date());
        assertEquals("2026-11-06T06:00:00Z", first.started_at());
        assertEquals("2026-11-06T16:00:00Z", first.ended_at());
        assertEquals(5L, first.ticket_count());
        assertEquals(90000L, first.cash_revenue());
        assertEquals(64000L, first.bank_revenue());
        assertEquals(154000L, first.total_revenue());
        assertEquals(14000L, first.overdue_fee());
        assertEquals(30000L, first.refund_total());

        SessionRevenueResponse second = result.get(1);
        assertEquals(1L, second.ticket_count());
        assertEquals(-90000L, second.cash_revenue());
        assertEquals(90000L, second.bank_revenue());
        assertEquals(0L, second.total_revenue());
        assertEquals(90000L, second.refund_total());

        SessionRevenueResponse third = result.get(2);
        assertEquals(0L, third.ticket_count());
        assertEquals(0L, third.cash_revenue());
        assertEquals(0L, third.bank_revenue());
        assertEquals(0L, third.total_revenue());
        assertEquals(0L, third.overdue_fee());
        assertEquals(0L, third.refund_total());
    }

    @Test
    void revenueMatchesSingleSessionStats() throws Exception {
        Session session = endedSession(7L, "2026-11-04", LocalDateTime.of(2026, 11, 4, 16, 0));
        when(sessionRepository.findById(7L)).thenReturn(Optional.of(session));
        when(sessionRepository.findByStatus(eq(SessionStatus.Ended), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(session)));
        List<Object[]> tickets = new ArrayList<>();
        tickets.add(row(TicketStatus.Completed, 3L));
        tickets.add(row(TicketStatus.Canceled, 1L));
        when(ticketRepository.countGroupedByStatus(7L)).thenReturn(tickets);
        List<Object[]> perSessionTickets = new ArrayList<>();
        perSessionTickets.add(row(7L, 4L));
        when(ticketRepository.countGroupedBySession(List.of(7L))).thenReturn(perSessionTickets);
        List<Object[]> single = new ArrayList<>();
        single.add(row(PaymentType.Ticket, PaymentMethod.Cash, 150000L));
        single.add(row(PaymentType.Extension, PaymentMethod.Bank, 40000L));
        single.add(row(PaymentType.OverdueFee, PaymentMethod.Cash, 12000L));
        single.add(row(PaymentType.Refund, PaymentMethod.Bank, 25000L));
        when(paymentRepository.sumGroupedByTypeAndMethod(7L)).thenReturn(single);
        List<Object[]> grouped = new ArrayList<>();
        grouped.add(row(7L, PaymentType.Ticket, PaymentMethod.Cash, 150000L));
        grouped.add(row(7L, PaymentType.Extension, PaymentMethod.Bank, 40000L));
        grouped.add(row(7L, PaymentType.OverdueFee, PaymentMethod.Cash, 12000L));
        grouped.add(row(7L, PaymentType.Refund, PaymentMethod.Bank, 25000L));
        when(paymentRepository.sumGroupedBySessionTypeAndMethod(List.of(7L))).thenReturn(grouped);

        SessionStatsResponse stats = service.get(7L);
        SessionRevenueResponse revenue = service.revenue(5).get(0);

        assertEquals(stats.ticket_count(), revenue.ticket_count());
        assertEquals(stats.cash_revenue(), revenue.cash_revenue());
        assertEquals(stats.bank_revenue(), revenue.bank_revenue());
        assertEquals(stats.total_revenue(), revenue.total_revenue());
        assertEquals(stats.overdue_fee(), revenue.overdue_fee());
        assertEquals(stats.refund_total(), revenue.refund_total());
        assertEquals(177000L, revenue.total_revenue());
    }

    @Test
    void revenueReadsLatestEndedSessionsWithDefaultLimit() throws Exception {
        when(sessionRepository.findByStatus(eq(SessionStatus.Ended), any(Pageable.class))).thenReturn(new PageImpl<>(new ArrayList<>()));

        service.revenue(null);
        service.revenue(14);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(sessionRepository, times(2)).findByStatus(eq(SessionStatus.Ended), captor.capture());
        Pageable first = captor.getAllValues().get(0);
        Pageable second = captor.getAllValues().get(1);
        assertEquals(0, first.getPageNumber());
        assertEquals(10, first.getPageSize());
        assertEquals(14, second.getPageSize());
        assertEquals(Sort.Direction.DESC, first.getSort().getOrderFor("endedAt").getDirection());
        assertEquals(Sort.Direction.DESC, first.getSort().getOrderFor("id").getDirection());
        assertEquals(List.of("endedAt", "id"), first.getSort().stream().map(Sort.Order::getProperty).toList());
    }

    @Test
    void revenueWithoutEndedSessionsReturnsEmptyListAndSkipsAggregation() throws Exception {
        when(sessionRepository.findByStatus(eq(SessionStatus.Ended), any(Pageable.class))).thenReturn(new PageImpl<>(new ArrayList<>()));

        assertTrue(service.revenue(7).isEmpty());

        verify(paymentRepository, never()).sumGroupedBySessionTypeAndMethod(any());
        verify(ticketRepository, never()).countGroupedBySession(any());
    }

    @Test
    void revenueRejectsLimitOutsideAllowedRange() {
        assertThrows(ValidationException.class, () -> service.revenue(0));
        assertThrows(ValidationException.class, () -> service.revenue(-3));
        assertThrows(ValidationException.class, () -> service.revenue(101));
        verify(sessionRepository, never()).findByStatus(any(), any(Pageable.class));
    }
}
