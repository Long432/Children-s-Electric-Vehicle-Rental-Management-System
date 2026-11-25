package org.group6.childrenelectricvehiclerentalmanagement.Services;

import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Ticket.CancelTicketRequest;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Ticket.CancelTicketResponse;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.PaymentMethod;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.PaymentType;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.SessionStatus;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.TicketStatus;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.ConflictException;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.NoActiveSessionException;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.TicketNotExits;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.ValidationException;
import org.group6.childrenelectricvehiclerentalmanagement.Models.Payment;
import org.group6.childrenelectricvehiclerentalmanagement.Models.Session;
import org.group6.childrenelectricvehiclerentalmanagement.Models.Ticket;
import org.group6.childrenelectricvehiclerentalmanagement.Repositories.PaymentRepository;
import org.group6.childrenelectricvehiclerentalmanagement.Repositories.SessionVehicleRepository;
import org.group6.childrenelectricvehiclerentalmanagement.Repositories.SesssionRepository;
import org.group6.childrenelectricvehiclerentalmanagement.Repositories.TicketRepository;
import org.group6.childrenelectricvehiclerentalmanagement.Repositories.TicketVehicleHistoryRepository;
import org.group6.childrenelectricvehiclerentalmanagement.Repositories.UserRepository;
import org.group6.childrenelectricvehiclerentalmanagement.Repositories.VehicleIncidentRepository;
import org.group6.childrenelectricvehiclerentalmanagement.Repositories.VehicleRepository;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.BusinessTime;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.Security.AuthenticatedUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class TicketActionServiceCancelTest {

    private static final Instant NOW = Instant.parse("2026-11-05T10:00:00Z");

    private final SesssionRepository sessionRepository = mock(SesssionRepository.class);
    private final TicketRepository ticketRepository = mock(TicketRepository.class);
    private final VehicleRepository vehicleRepository = mock(VehicleRepository.class);
    private final SessionVehicleRepository sessionVehicleRepository = mock(SessionVehicleRepository.class);
    private final TicketVehicleHistoryRepository historyRepository = mock(TicketVehicleHistoryRepository.class);
    private final PaymentRepository paymentRepository = mock(PaymentRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final AuditLogService auditLogService = mock(AuditLogService.class);
    private final BusinessTime businessTime = new BusinessTime(Clock.fixed(NOW, ZoneOffset.UTC), "Asia/Ho_Chi_Minh");
    private final AuthenticatedUser actor = new AuthenticatedUser(1L, "banhang");
    private TicketActionService service;

    @BeforeEach
    void setUp() {
        service = new TicketActionService(sessionRepository, ticketRepository, vehicleRepository,
                sessionVehicleRepository, historyRepository, paymentRepository, userRepository, mock(SettingsService.class), auditLogService, businessTime, mock(VehicleIncidentRepository.class));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(call -> call.getArgument(0));
    }

    private Session activeSession(long id) {
        Session session = new Session();
        session.setId(id);
        session.setStatus(SessionStatus.InProgress);
        session.setScheduledDate(LocalDate.of(2026, 11, 5));
        session.setScheduledTime(LocalTime.of(9, 0));
        session.setIsInstant(false);
        session.setNextTicketNo(10);
        session.setCreatedAt(LocalDateTime.of(2026, 11, 5, 1, 0));
        session.setUpdatedAt(LocalDateTime.of(2026, 11, 5, 1, 0));
        return session;
    }

    private Ticket ticket(long id, long sessionId, TicketStatus status, PaymentMethod method) {
        LocalDateTime queued = LocalDateTime.of(2026, 11, 5, 9, 50);
        Ticket ticket = new Ticket();
        ticket.setId(id);
        ticket.setSessionId(sessionId);
        ticket.setTicketNo(7);
        ticket.setPhone("0912345678");
        ticket.setStatus(status);
        ticket.setPaymentMethod(method);
        ticket.setTotalMinutes(30);
        ticket.setAmountPaid(50000);
        ticket.setPriceAmount(50000);
        ticket.setPriceMinutes(30);
        ticket.setOverdueFeePerMinute(2000);
        ticket.setOverdueGraceMinutes(5);
        ticket.setRefundEnabled(true);
        ticket.setQueuedAt(queued);
        ticket.setOverdueMinutes(0);
        ticket.setOverdueFee(0);
        ticket.setRefundAmount(0);
        ticket.setCreatedAt(queued);
        ticket.setUpdatedAt(queued);
        return ticket;
    }

    private void givenTicket(Ticket ticket) {
        when(sessionRepository.findByStatusForUpdate(SessionStatus.InProgress)).thenReturn(Optional.of(activeSession(1L)));
        when(ticketRepository.findByIdForUpdate(ticket.getId())).thenReturn(Optional.of(ticket));
    }

    @Test
    @SuppressWarnings("unchecked")
    void cancelRefundsTheFullAmountByTheOriginalMethod() throws Exception {
        Ticket ticket = ticket(70L, 1L, TicketStatus.Waiting, PaymentMethod.Cash);
        givenTicket(ticket);

        CancelTicketResponse response = service.cancel(actor, 70L, new CancelTicketRequest(null));

        assertEquals(7, response.ticket_no());
        assertEquals(50000, response.refund_amount());
        assertEquals(PaymentMethod.Cash, response.refund_method());
        assertEquals(TicketStatus.Canceled, response.ticket().status());
        assertEquals(50000, response.ticket().refund_amount());
        assertEquals(TicketStatus.Canceled, ticket.getStatus());
        assertEquals(LocalDateTime.of(2026, 11, 5, 10, 0), ticket.getCanceledAt());
        assertEquals(50000, ticket.getAmountPaid());
        assertNull(ticket.getVehicle());

        ArgumentCaptor<Payment> payment = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(payment.capture());
        assertEquals(PaymentType.Refund, payment.getValue().getType());
        assertEquals(PaymentMethod.Cash, payment.getValue().getMethod());
        assertEquals(50000, payment.getValue().getAmount());
        assertEquals(30, payment.getValue().getMinutes());
        assertEquals(70L, payment.getValue().getTicketId());
        assertEquals(1L, payment.getValue().getSessionId());
        assertEquals(LocalDateTime.of(2026, 11, 5, 10, 0), payment.getValue().getCreatedAt());

        ArgumentCaptor<Map<String, Object>> payload = ArgumentCaptor.forClass((Class<Map<String, Object>>) (Class<?>) Map.class);
        verify(auditLogService).log(eq(actor), eq("ticket.cancel"), eq("ticket"), eq(70L), payload.capture());
        assertEquals(7, payload.getValue().get("ticket_no"));
        assertEquals(50000, payload.getValue().get("refund_amount"));
        assertEquals("Cash", payload.getValue().get("refund_method"));
        verifyNoInteractions(vehicleRepository, historyRepository);
    }

    @Test
    void cancelWorksWithoutABodyAndDefaultsToTheOriginalMethod() throws Exception {
        Ticket ticket = ticket(70L, 1L, TicketStatus.Waiting, PaymentMethod.Bank);
        givenTicket(ticket);
        CancelTicketResponse response = service.cancel(actor, 70L, null);
        assertEquals(PaymentMethod.Bank, response.refund_method());
        ArgumentCaptor<Payment> payment = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(payment.capture());
        assertEquals(PaymentMethod.Bank, payment.getValue().getMethod());
    }

    @Test
    void cancelCanRefundByAnotherMethod() throws Exception {
        Ticket ticket = ticket(70L, 1L, TicketStatus.Waiting, PaymentMethod.Bank);
        givenTicket(ticket);
        CancelTicketResponse response = service.cancel(actor, 70L, new CancelTicketRequest(" Cash "));
        assertEquals(PaymentMethod.Cash, response.refund_method());
        ArgumentCaptor<Payment> payment = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(payment.capture());
        assertEquals(PaymentMethod.Cash, payment.getValue().getMethod());
        assertEquals(PaymentMethod.Bank, ticket.getPaymentMethod());
    }

    @Test
    void cancelRejectsAnInvalidRefundMethodBeforeTouchingTheDatabase() {
        assertThrows(ValidationException.class, () -> service.cancel(actor, 70L, new CancelTicketRequest("Momo")));
        verifyNoInteractions(sessionRepository, ticketRepository, paymentRepository);
    }

    @Test
    void cancelRequiresAnActiveSession() {
        when(sessionRepository.findByStatusForUpdate(SessionStatus.InProgress)).thenReturn(Optional.empty());
        assertThrows(NoActiveSessionException.class, () -> service.cancel(actor, 70L, null));
        verifyNoInteractions(ticketRepository, paymentRepository);
    }

    @Test
    void cancelFailsWhenTheTicketDoesNotExist() {
        when(sessionRepository.findByStatusForUpdate(SessionStatus.InProgress)).thenReturn(Optional.of(activeSession(1L)));
        when(ticketRepository.findByIdForUpdate(99L)).thenReturn(Optional.empty());
        assertThrows(TicketNotExits.class, () -> service.cancel(actor, 99L, null));
        verifyNoInteractions(paymentRepository);
    }

    @Test
    void cancelRejectsATicketFromAnotherSession() {
        Ticket ticket = ticket(70L, 2L, TicketStatus.Waiting, PaymentMethod.Cash);
        givenTicket(ticket);
        ConflictException error = assertThrows(ConflictException.class, () -> service.cancel(actor, 70L, null));
        assertEquals("TICKET_NOT_IN_ACTIVE_SESSION", error.getCode());
        assertEquals(TicketStatus.Waiting, ticket.getStatus());
        verifyNoInteractions(paymentRepository);
    }

    @Test
    void cancelOnlyAcceptsWaitingTickets() {
        for (TicketStatus status : new TicketStatus[]{TicketStatus.Inused, TicketStatus.Overdue, TicketStatus.Canceled, TicketStatus.Completed}) {
            Ticket ticket = ticket(70L, 1L, status, PaymentMethod.Cash);
            givenTicket(ticket);
            ConflictException error = assertThrows(ConflictException.class, () -> service.cancel(actor, 70L, null));
            assertEquals("TICKET_NOT_CANCELABLE", error.getCode());
            assertEquals(status, ticket.getStatus());
            assertNull(ticket.getCanceledAt());
            assertEquals(0, ticket.getRefundAmount());
        }
        verify(ticketRepository, never()).save(any(Ticket.class));
        verifyNoInteractions(paymentRepository);
        verify(auditLogService, never()).log(any(), any(), any(), any(), anyMap());
    }
}
