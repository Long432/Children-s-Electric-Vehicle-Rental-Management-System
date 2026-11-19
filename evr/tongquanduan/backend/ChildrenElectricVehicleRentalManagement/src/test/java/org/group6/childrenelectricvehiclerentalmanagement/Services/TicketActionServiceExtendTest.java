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

class TicketActionServiceExtendTest {

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

    private static final LocalDateTime DUE = LocalDateTime.of(2026, 11, 5, 10, 0);

    @Test
    void extendAddsMinutesAndChargesTheSnapshotRate() throws Exception {
        Vehicle bear = vehicle(3L, "xe-gau-03", VehicleStatus.Inused);
        Ticket ticket = ticket(70L, 1L, TicketStatus.Inused, bear, DUE.plusMinutes(10));
        givenTicket(ticket);
        givenSettings(true);

        ExtendTicketResponse response = service.extend(actor, 70L, new ExtendTicketRequest(15, " Bank "));

        assertEquals(7, response.ticket_no());
        assertEquals(15, response.extended_minutes());
        assertEquals(25000, response.amount_paid());
        assertEquals(PaymentMethod.Bank, response.payment_method());
        assertEquals(45, ticket.getTotalMinutes());
        assertEquals(75000, ticket.getAmountPaid());
        assertEquals(DUE.plusMinutes(25), ticket.getDueAt());
        assertEquals(TicketStatus.Inused, ticket.getStatus());
        assertEquals(PaymentMethod.Cash, ticket.getPaymentMethod());

        ArgumentCaptor<Payment> payment = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(payment.capture());
        assertEquals(PaymentType.Extension, payment.getValue().getType());
        assertEquals(PaymentMethod.Bank, payment.getValue().getMethod());
        assertEquals(25000, payment.getValue().getAmount());
        assertEquals(15, payment.getValue().getMinutes());
        assertEquals(70L, payment.getValue().getTicketId());
        assertEquals(1L, payment.getValue().getSessionId());
        verify(auditLogService).log(eq(actor), eq("ticket.extend"), eq("ticket"), eq(70L), anyMap());
    }

    @Test
    void extendRoundsFractionsUpToWholeDong() throws Exception {
        Vehicle bear = vehicle(3L, "xe-gau-03", VehicleStatus.Inused);
        Ticket ticket = ticket(70L, 1L, TicketStatus.Inused, bear, DUE.plusMinutes(10));
        givenTicket(ticket);
        givenSettings(true);
        ExtendTicketResponse response = service.extend(actor, 70L, new ExtendTicketRequest(7, "Cash"));
        assertEquals(11667, response.amount_paid());
    }

    @Test
    void extendingAnOverdueTicketCountsFromTheOldDueTimeAndReopensItWhenCaughtUp() throws Exception {
        Vehicle bear = vehicle(3L, "xe-gau-03", VehicleStatus.Inused);
        Ticket ticket = ticket(70L, 1L, TicketStatus.Overdue, bear, DUE.minusMinutes(10));
        givenTicket(ticket);
        givenSettings(true);

        service.extend(actor, 70L, new ExtendTicketRequest(30, "Cash"));

        assertEquals(DUE.plusMinutes(20), ticket.getDueAt());
        assertEquals(TicketStatus.Inused, ticket.getStatus());
    }

    @Test
    void extendingAnOverdueTicketThatStaysLateKeepsItOverdue() throws Exception {
        Vehicle bear = vehicle(3L, "xe-gau-03", VehicleStatus.Inused);
        Ticket ticket = ticket(70L, 1L, TicketStatus.Inused, bear, DUE.minusMinutes(30));
        givenTicket(ticket);
        givenSettings(true);

        service.extend(actor, 70L, new ExtendTicketRequest(10, "Cash"));

        assertEquals(DUE.minusMinutes(20), ticket.getDueAt());
        assertEquals(TicketStatus.Overdue, ticket.getStatus());
    }

    @Test
    void extendIsRejectedWhenTheSettingIsOff() {
        Vehicle bear = vehicle(3L, "xe-gau-03", VehicleStatus.Inused);
        Ticket ticket = ticket(70L, 1L, TicketStatus.Inused, bear, DUE);
        givenTicket(ticket);
        givenSettings(false);

        ConflictException error = assertThrows(ConflictException.class,
                () -> service.extend(actor, 70L, new ExtendTicketRequest(15, "Cash")));

        assertEquals("EXTEND_DISABLED", error.getCode());
        assertEquals(30, ticket.getTotalMinutes());
        assertEquals(DUE, ticket.getDueAt());
        verifyNoInteractions(paymentRepository);
    }

    @Test
    void extendOnlyAcceptsTicketsThatAreRunning() {
        for (TicketStatus status : new TicketStatus[]{TicketStatus.Waiting, TicketStatus.Canceled, TicketStatus.Completed}) {
            Ticket ticket = ticket(70L, 1L, status, null, null);
            givenTicket(ticket);
            givenSettings(true);
            ConflictException error = assertThrows(ConflictException.class,
                    () -> service.extend(actor, 70L, new ExtendTicketRequest(15, "Cash")));
            assertEquals("TICKET_NOT_IN_USE", error.getCode());
            assertEquals(30, ticket.getTotalMinutes());
        }
        verify(ticketRepository, never()).save(any(Ticket.class));
        verifyNoInteractions(paymentRepository);
    }

    @Test
    void extendRejectsATicketFromAnotherSession() {
        Vehicle bear = vehicle(3L, "xe-gau-03", VehicleStatus.Inused);
        Ticket ticket = ticket(70L, 2L, TicketStatus.Inused, bear, DUE);
        givenTicket(ticket);
        ConflictException error = assertThrows(ConflictException.class,
                () -> service.extend(actor, 70L, new ExtendTicketRequest(15, "Cash")));
        assertEquals("TICKET_NOT_IN_ACTIVE_SESSION", error.getCode());
        verifyNoInteractions(paymentRepository);
    }

    @Test
    void extendRejectsInvalidInputBeforeTouchingTheDatabase() {
        assertThrows(ValidationException.class, () -> service.extend(actor, 70L, null));
        assertThrows(ValidationException.class, () -> service.extend(actor, 70L, new ExtendTicketRequest(0, "Cash")));
        assertThrows(ValidationException.class, () -> service.extend(actor, 70L, new ExtendTicketRequest(15, "Momo")));
        verifyNoInteractions(sessionRepository, ticketRepository, paymentRepository);
    }

    @Test
    void extendRequiresAnActiveSessionAndAnExistingTicket() {
        when(sessionRepository.findByStatusForUpdate(SessionStatus.InProgress)).thenReturn(Optional.empty());
        assertThrows(NoActiveSessionException.class, () -> service.extend(actor, 70L, new ExtendTicketRequest(15, "Cash")));
        when(sessionRepository.findByStatusForUpdate(SessionStatus.InProgress)).thenReturn(Optional.of(activeSession(1L)));
        when(ticketRepository.findByIdForUpdate(99L)).thenReturn(Optional.empty());
        assertThrows(TicketNotExits.class, () -> service.extend(actor, 99L, new ExtendTicketRequest(15, "Cash")));
        verifyNoInteractions(paymentRepository);
    }
}
