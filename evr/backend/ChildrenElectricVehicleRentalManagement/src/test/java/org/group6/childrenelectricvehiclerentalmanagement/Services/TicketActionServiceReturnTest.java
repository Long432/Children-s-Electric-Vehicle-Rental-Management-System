package org.group6.childrenelectricvehiclerentalmanagement.Services;

import org.group6.childrenelectricvehiclerentalmanagement.DTOs.SettingsResponse;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Ticket.ExtendTicketRequest;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Ticket.ExtendTicketResponse;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Ticket.ReturnTicketRequest;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Ticket.ReturnTicketResponse;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.PaymentMethod;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.PaymentType;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.ReleasedVehicleStatus;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.SessionStatus;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.TicketStatus;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.VehicleStatus;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.ConflictException;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.NoActiveSessionException;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.TicketNotExits;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.ValidationException;
import org.group6.childrenelectricvehiclerentalmanagement.Models.Payment;
import org.group6.childrenelectricvehiclerentalmanagement.Models.Session;
import org.group6.childrenelectricvehiclerentalmanagement.Models.Ticket;
import org.group6.childrenelectricvehiclerentalmanagement.Models.TicketVehicleHistory;
import org.group6.childrenelectricvehiclerentalmanagement.Models.Vehicle;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class TicketActionServiceReturnTest {

    private static final Instant NOW = Instant.parse("2026-11-05T10:00:00Z");
    private static final LocalDateTime STARTED = LocalDateTime.of(2026, 11, 5, 9, 30);

    private final SesssionRepository sessionRepository = mock(SesssionRepository.class);
    private final TicketRepository ticketRepository = mock(TicketRepository.class);
    private final VehicleRepository vehicleRepository = mock(VehicleRepository.class);
    private final SessionVehicleRepository sessionVehicleRepository = mock(SessionVehicleRepository.class);
    private final TicketVehicleHistoryRepository historyRepository = mock(TicketVehicleHistoryRepository.class);
    private final PaymentRepository paymentRepository = mock(PaymentRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final SettingsService settingsService = mock(SettingsService.class);
    private final AuditLogService auditLogService = mock(AuditLogService.class);
    private final BusinessTime businessTime = new BusinessTime(Clock.fixed(NOW, ZoneOffset.UTC), "Asia/Ho_Chi_Minh");
    private final AuthenticatedUser actor = new AuthenticatedUser(1L, "banhang");
    private TicketActionService service;

    @BeforeEach
    void setUp() {
        service = new TicketActionService(sessionRepository, ticketRepository, vehicleRepository,
                sessionVehicleRepository, historyRepository, paymentRepository, userRepository, settingsService, auditLogService, businessTime, mock(VehicleIncidentRepository.class));
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

    private Vehicle vehicle(long id, String slug, VehicleStatus status) {
        Vehicle vehicle = new Vehicle(slug);
        ReflectionTestUtils.setField(vehicle, "id", id);
        vehicle.setStatus(status);
        return vehicle;
    }

    private Ticket ticket(long id, long sessionId, TicketStatus status, Vehicle vehicle, LocalDateTime due) {
        LocalDateTime queued = LocalDateTime.of(2026, 11, 5, 9, 20);
        Ticket ticket = new Ticket();
        ticket.setId(id);
        ticket.setSessionId(sessionId);
        ticket.setTicketNo(7);
        ticket.setPhone("0912345678");
        ticket.setStatus(status);
        ticket.setPaymentMethod(PaymentMethod.Cash);
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
        ticket.setVehicle(vehicle);
        if (vehicle != null) {
            ticket.setStartedAt(STARTED);
            ticket.setDueAt(due);
        }
        return ticket;
    }

    private void givenTicket(Ticket ticket) {
        when(sessionRepository.findByStatusForUpdate(SessionStatus.InProgress)).thenReturn(Optional.of(activeSession(1L)));
        when(ticketRepository.findByIdForUpdate(ticket.getId())).thenReturn(Optional.of(ticket));
        if (ticket.getVehicle() != null) {
            when(vehicleRepository.findByIdForUpdate(ticket.getVehicle().getId())).thenReturn(Optional.of(ticket.getVehicle()));
        }
    }

    private void givenSettings(boolean extendEnabled) {
        when(settingsService.getSettings()).thenReturn(new SettingsResponse(true, extendEnabled, 2000, 5, 15, List.of(), null));
    }

    private static final LocalDateTime TEN = LocalDateTime.of(2026, 11, 5, 10, 0);

    private ArgumentCaptor<Payment> payments(int expected) {
        ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository, times(expected)).save(captor.capture());
        return captor;
    }

    @Test
    void returnOnTimeFreesTheVehicleAndChargesNothing() throws Exception {
        Vehicle bear = vehicle(3L, "xe-gau-03", VehicleStatus.Inused);
        Ticket ticket = ticket(70L, 1L, TicketStatus.Inused, bear, TEN);
        givenTicket(ticket);
        TicketVehicleHistory open = new TicketVehicleHistory(70L, 3L, STARTED);
        when(historyRepository.findFirstByTicketIdAndVehicleIdAndReleasedAtIsNullOrderByIdDesc(70L, 3L)).thenReturn(Optional.of(open));

        ReturnTicketResponse response = service.returnVehicle(actor, 70L, null);

        assertEquals(TicketStatus.Completed, response.status());
        assertEquals(0, response.overdue_minutes());
        assertEquals(0, response.overdue_fee());
        assertEquals(0, response.refund_amount());
        assertNull(response.overdue_fee_method());
        assertNull(response.refund_method());
        assertNull(response.next_ticket());
        assertEquals("xe-gau-03", response.vehicle().slug());
        assertEquals(VehicleStatus.Online, response.vehicle().status());
        assertEquals(VehicleStatus.Online, bear.getStatus());
        assertEquals(TicketStatus.Completed, ticket.getStatus());
        assertEquals(TEN, ticket.getReturnedAt());
        assertEquals(TEN, open.getReleasedAt());
        assertEquals(ReleasedVehicleStatus.Online, open.getReleasedStatus());
        verify(vehicleRepository).save(bear);
        verify(historyRepository).save(open);
        verifyNoInteractions(paymentRepository);
        verify(auditLogService).log(eq(actor), eq("ticket.return"), eq("ticket"), eq(70L), anyMap());
    }

    @Test
    void returnWithinTheGraceTimeIsNotCharged() throws Exception {
        Vehicle bear = vehicle(3L, "xe-gau-03", VehicleStatus.Inused);
        Ticket ticket = ticket(70L, 1L, TicketStatus.Overdue, bear, TEN.minusMinutes(5));
        givenTicket(ticket);

        ReturnTicketResponse response = service.returnVehicle(actor, 70L, null);

        assertEquals(5, response.overdue_minutes());
        assertEquals(0, response.chargeable_overdue_minutes());
        assertEquals(0, response.overdue_fee());
        assertEquals(5, ticket.getOverdueMinutes());
        verifyNoInteractions(paymentRepository);
    }

    @Test
    void returnLateChargesOnlyTheTimeBeyondTheGrace() throws Exception {
        Vehicle bear = vehicle(3L, "xe-gau-03", VehicleStatus.Inused);
        Ticket ticket = ticket(70L, 1L, TicketStatus.Overdue, bear, TEN.minusMinutes(12));
        givenTicket(ticket);

        ReturnTicketResponse response = service.returnVehicle(actor, 70L, new ReturnTicketRequest("Bank", null));

        assertEquals(12, response.overdue_minutes());
        assertEquals(7, response.chargeable_overdue_minutes());
        assertEquals(14000, response.overdue_fee());
        assertEquals(PaymentMethod.Bank, response.overdue_fee_method());
        assertEquals(0, response.refund_amount());
        assertEquals(12, ticket.getOverdueMinutes());
        assertEquals(14000, ticket.getOverdueFee());
        assertEquals(TicketStatus.Completed, ticket.getStatus());
        Payment payment = payments(1).getValue();
        assertEquals(PaymentType.OverdueFee, payment.getType());
        assertEquals(PaymentMethod.Bank, payment.getMethod());
        assertEquals(14000, payment.getAmount());
        assertEquals(7, payment.getMinutes());
    }

    @Test
    void aPartialLateMinuteCountsAsAWholeMinute() throws Exception {
        Vehicle bear = vehicle(3L, "xe-gau-03", VehicleStatus.Inused);
        Ticket ticket = ticket(70L, 1L, TicketStatus.Overdue, bear, TEN.minusMinutes(6).minusSeconds(20));
        givenTicket(ticket);
        ReturnTicketResponse response = service.returnVehicle(actor, 70L, null);
        assertEquals(7, response.overdue_minutes());
        assertEquals(2, response.chargeable_overdue_minutes());
        assertEquals(4000, response.overdue_fee());
        assertEquals(PaymentMethod.Cash, response.overdue_fee_method());
    }

    @Test
    void returnEarlyRefundsTheUnusedMinutesWhenRefundIsEnabledOnTheTicket() throws Exception {
        Vehicle bear = vehicle(3L, "xe-gau-03", VehicleStatus.Inused);
        Ticket ticket = ticket(70L, 1L, TicketStatus.Inused, bear, TEN.plusMinutes(10));
        givenTicket(ticket);

        ReturnTicketResponse response = service.returnVehicle(actor, 70L, new ReturnTicketRequest(null, "Bank"));

        assertEquals(0, response.overdue_fee());
        assertEquals(16666, response.refund_amount());
        assertEquals(PaymentMethod.Bank, response.refund_method());
        assertEquals(16666, ticket.getRefundAmount());
        assertEquals(50000, ticket.getAmountPaid());
        Payment payment = payments(1).getValue();
        assertEquals(PaymentType.Refund, payment.getType());
        assertEquals(PaymentMethod.Bank, payment.getMethod());
        assertEquals(16666, payment.getAmount());
        assertEquals(10, payment.getMinutes());
    }

    @Test
    void returnEarlyRefundsByTheOriginalMethodByDefaultAndFollowsWhatWasActuallyPaid() throws Exception {
        Vehicle bear = vehicle(3L, "xe-gau-03", VehicleStatus.Inused);
        Ticket ticket = ticket(70L, 1L, TicketStatus.Inused, bear, TEN.plusMinutes(30));
        ticket.setPaymentMethod(PaymentMethod.Bank);
        ticket.setTotalMinutes(60);
        ticket.setAmountPaid(90000);
        givenTicket(ticket);

        ReturnTicketResponse response = service.returnVehicle(actor, 70L, null);

        assertEquals(45000, response.refund_amount());
        assertEquals(PaymentMethod.Bank, response.refund_method());
    }

    @Test
    void returnEarlyDoesNotRefundWhenTheTicketSnapshotHasRefundOff() throws Exception {
        Vehicle bear = vehicle(3L, "xe-gau-03", VehicleStatus.Inused);
        Ticket ticket = ticket(70L, 1L, TicketStatus.Inused, bear, TEN.plusMinutes(10));
        ticket.setRefundEnabled(false);
        givenTicket(ticket);

        ReturnTicketResponse response = service.returnVehicle(actor, 70L, null);

        assertEquals(0, response.refund_amount());
        assertNull(response.refund_method());
        assertEquals(0, ticket.getRefundAmount());
        verifyNoInteractions(paymentRepository);
        verifyNoInteractions(settingsService);
    }

    @Test
    void returnSuggestsTheFirstWaitingTicketInTheQueue() throws Exception {
        Vehicle bear = vehicle(3L, "xe-gau-03", VehicleStatus.Inused);
        Ticket ticket = ticket(70L, 1L, TicketStatus.Inused, bear, TEN);
        givenTicket(ticket);
        Ticket waiting = ticket(71L, 1L, TicketStatus.Waiting, null, null);
        waiting.setTicketNo(9);
        waiting.setPhone("0987654321");
        when(ticketRepository.findFirstBySessionIdAndStatusOrderByQueuedAtAscIdAsc(1L, TicketStatus.Waiting)).thenReturn(Optional.of(waiting));

        ReturnTicketResponse response = service.returnVehicle(actor, 70L, null);

        assertNotNull(response.next_ticket());
        assertEquals(71L, response.next_ticket().id());
        assertEquals(9, response.next_ticket().ticket_no());
        assertEquals("0987654321", response.next_ticket().phone());
        assertEquals(TicketStatus.Waiting, waiting.getStatus());
        assertNull(waiting.getVehicle());
    }

    @Test
    void returnCreatesTheHistoryRowWhenNoOpenOneExists() throws Exception {
        Vehicle bear = vehicle(3L, "xe-gau-03", VehicleStatus.Inused);
        Ticket ticket = ticket(70L, 1L, TicketStatus.Inused, bear, TEN);
        givenTicket(ticket);
        when(historyRepository.findFirstByTicketIdAndVehicleIdAndReleasedAtIsNullOrderByIdDesc(70L, 3L)).thenReturn(Optional.empty());

        service.returnVehicle(actor, 70L, null);

        ArgumentCaptor<TicketVehicleHistory> history = ArgumentCaptor.forClass(TicketVehicleHistory.class);
        verify(historyRepository).save(history.capture());
        assertEquals(STARTED, history.getValue().getAssignedAt());
        assertEquals(TEN, history.getValue().getReleasedAt());
        assertEquals(ReleasedVehicleStatus.Online, history.getValue().getReleasedStatus());
    }

    @Test
    void returnOnlyAcceptsTicketsThatAreRunning() {
        for (TicketStatus status : new TicketStatus[]{TicketStatus.Waiting, TicketStatus.Canceled, TicketStatus.Completed}) {
            Ticket ticket = ticket(70L, 1L, status, null, null);
            givenTicket(ticket);
            ConflictException error = assertThrows(ConflictException.class, () -> service.returnVehicle(actor, 70L, null));
            assertEquals("TICKET_NOT_IN_USE", error.getCode());
            assertEquals(status, ticket.getStatus());
        }
        verify(ticketRepository, never()).save(any(Ticket.class));
        verifyNoInteractions(paymentRepository, historyRepository);
    }

    @Test
    void returnRejectsATicketFromAnotherSession() {
        Vehicle bear = vehicle(3L, "xe-gau-03", VehicleStatus.Inused);
        Ticket ticket = ticket(70L, 2L, TicketStatus.Inused, bear, TEN);
        givenTicket(ticket);
        ConflictException error = assertThrows(ConflictException.class, () -> service.returnVehicle(actor, 70L, null));
        assertEquals("TICKET_NOT_IN_ACTIVE_SESSION", error.getCode());
        assertEquals(VehicleStatus.Inused, bear.getStatus());
    }

    @Test
    void returnRejectsInvalidMethodsBeforeTouchingTheDatabase() {
        assertThrows(ValidationException.class, () -> service.returnVehicle(actor, 70L, new ReturnTicketRequest("Momo", null)));
        assertThrows(ValidationException.class, () -> service.returnVehicle(actor, 70L, new ReturnTicketRequest(null, "momo")));
        verifyNoInteractions(sessionRepository, ticketRepository, paymentRepository);
    }

    @Test
    void returnRequiresAnActiveSessionAndAnExistingTicket() {
        when(sessionRepository.findByStatusForUpdate(SessionStatus.InProgress)).thenReturn(Optional.empty());
        assertThrows(NoActiveSessionException.class, () -> service.returnVehicle(actor, 70L, null));
        when(sessionRepository.findByStatusForUpdate(SessionStatus.InProgress)).thenReturn(Optional.of(activeSession(1L)));
        when(ticketRepository.findByIdForUpdate(99L)).thenReturn(Optional.empty());
        assertThrows(TicketNotExits.class, () -> service.returnVehicle(actor, 99L, null));
        verifyNoInteractions(paymentRepository);
    }
}
