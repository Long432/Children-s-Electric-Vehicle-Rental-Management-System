package org.group6.childrenelectricvehiclerentalmanagement.Services;

import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Ticket.AssignTicketRequest;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Ticket.TicketResponse;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.PaymentMethod;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.SessionStatus;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.TicketStatus;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.VehicleStatus;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.ConflictException;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.NoActiveSessionException;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.TicketNotExits;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.ValidationException;
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
import java.util.Map;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class TicketActionServiceAssignTest {

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

    private Ticket ticket(long id, long sessionId, TicketStatus status) {
        LocalDateTime queued = LocalDateTime.of(2026, 11, 5, 9, 50);
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
        return ticket;
    }

    private Vehicle vehicle(long id, String slug, VehicleStatus status) {
        Vehicle vehicle = new Vehicle(slug);
        ReflectionTestUtils.setField(vehicle, "id", id);
        vehicle.setStatus(status);
        return vehicle;
    }

    private void givenReadyToAssign(Ticket ticket, Vehicle vehicle) {
        when(sessionRepository.findByStatusForUpdate(SessionStatus.InProgress)).thenReturn(Optional.of(activeSession(1L)));
        when(ticketRepository.findByIdForUpdate(ticket.getId())).thenReturn(Optional.of(ticket));
        when(vehicleRepository.findActiveBySlugsForUpdate(List.of(vehicle.getSlug()))).thenReturn(List.of(vehicle));
        when(sessionVehicleRepository.existsBySessionIdAndVehicleIdAndRemovedAtIsNull(1L, vehicle.getId())).thenReturn(true);
    }

    @Test
    @SuppressWarnings("unchecked")
    void assignStartsTheClockAndMarksBothTheTicketAndTheVehicleInused() throws Exception {
        Ticket ticket = ticket(70L, 1L, TicketStatus.Waiting);
        Vehicle vehicle = vehicle(3L, "xe-gau-01", VehicleStatus.Online);
        givenReadyToAssign(ticket, vehicle);

        TicketResponse response = service.assign(actor, 70L, new AssignTicketRequest(" xe-gau-01 "));

        assertEquals(TicketStatus.Inused, response.status());
        assertEquals(LocalDateTime.of(2026, 11, 5, 10, 0), ticket.getStartedAt());
        assertEquals(LocalDateTime.of(2026, 11, 5, 10, 30), ticket.getDueAt());
        assertEquals(VehicleStatus.Inused, vehicle.getStatus());
        assertEquals("xe-gau-01", response.vehicle().slug());
        assertEquals(VehicleStatus.Inused, response.vehicle().status());
        verify(vehicleRepository).save(vehicle);

        ArgumentCaptor<TicketVehicleHistory> history = ArgumentCaptor.forClass(TicketVehicleHistory.class);
        verify(historyRepository).save(history.capture());
        assertEquals(70L, history.getValue().getTicketId());
        assertEquals(3L, history.getValue().getVehicleId());
        assertEquals(LocalDateTime.of(2026, 11, 5, 10, 0), history.getValue().getAssignedAt());
        assertNull(history.getValue().getReleasedAt());
        assertNull(history.getValue().getReleasedStatus());

        ArgumentCaptor<Map<String, Object>> payload = ArgumentCaptor.forClass((Class<Map<String, Object>>) (Class<?>) Map.class);
        verify(auditLogService).log(eq(actor), eq("ticket.assign"), eq("ticket"), eq(70L), payload.capture());
        assertEquals(7, payload.getValue().get("ticket_no"));
        assertEquals("xe-gau-01", payload.getValue().get("vehicle"));
        assertNotNull(payload.getValue().get("due_at"));
    }

    @Test
    void assignRequiresAnActiveSession() {
        when(sessionRepository.findByStatusForUpdate(SessionStatus.InProgress)).thenReturn(Optional.empty());
        assertThrows(NoActiveSessionException.class, () -> service.assign(actor, 70L, new AssignTicketRequest("xe-gau-01")));
        verifyNoInteractions(ticketRepository, vehicleRepository, historyRepository);
    }

    @Test
    void assignRejectsAnInvalidBodyBeforeTouchingTheDatabase() {
        assertThrows(ValidationException.class, () -> service.assign(actor, 70L, null));
        assertThrows(ValidationException.class, () -> service.assign(actor, 70L, new AssignTicketRequest("Xe Gau")));
        verifyNoInteractions(sessionRepository, ticketRepository, vehicleRepository, historyRepository);
    }

    @Test
    void assignFailsWhenTheTicketDoesNotExist() {
        when(sessionRepository.findByStatusForUpdate(SessionStatus.InProgress)).thenReturn(Optional.of(activeSession(1L)));
        when(ticketRepository.findByIdForUpdate(99L)).thenReturn(Optional.empty());
        assertThrows(TicketNotExits.class, () -> service.assign(actor, 99L, new AssignTicketRequest("xe-gau-01")));
        verifyNoInteractions(vehicleRepository, historyRepository);
    }

    @Test
    void assignRejectsATicketFromAnotherSession() {
        Ticket ticket = ticket(70L, 2L, TicketStatus.Waiting);
        when(sessionRepository.findByStatusForUpdate(SessionStatus.InProgress)).thenReturn(Optional.of(activeSession(1L)));
        when(ticketRepository.findByIdForUpdate(70L)).thenReturn(Optional.of(ticket));
        ConflictException error = assertThrows(ConflictException.class,
                () -> service.assign(actor, 70L, new AssignTicketRequest("xe-gau-01")));
        assertEquals("TICKET_NOT_IN_ACTIVE_SESSION", error.getCode());
        assertEquals(TicketStatus.Waiting, ticket.getStatus());
        verifyNoInteractions(vehicleRepository, historyRepository);
    }

    @Test
    void assignOnlyAcceptsWaitingTickets() {
        for (TicketStatus status : new TicketStatus[]{TicketStatus.Inused, TicketStatus.Overdue, TicketStatus.Canceled, TicketStatus.Completed}) {
            Ticket ticket = ticket(70L, 1L, status);
            when(sessionRepository.findByStatusForUpdate(SessionStatus.InProgress)).thenReturn(Optional.of(activeSession(1L)));
            when(ticketRepository.findByIdForUpdate(70L)).thenReturn(Optional.of(ticket));
            ConflictException error = assertThrows(ConflictException.class,
                    () -> service.assign(actor, 70L, new AssignTicketRequest("xe-gau-01")));
            assertEquals("TICKET_NOT_WAITING", error.getCode());
            assertEquals(status, ticket.getStatus());
        }
        verifyNoInteractions(vehicleRepository, historyRepository);
    }

    @Test
    void assignRejectsAnUnknownVehicle() {
        Ticket ticket = ticket(70L, 1L, TicketStatus.Waiting);
        when(sessionRepository.findByStatusForUpdate(SessionStatus.InProgress)).thenReturn(Optional.of(activeSession(1L)));
        when(ticketRepository.findByIdForUpdate(70L)).thenReturn(Optional.of(ticket));
        when(vehicleRepository.findActiveBySlugsForUpdate(List.of("xe-ma"))).thenReturn(List.of());
        ValidationException error = assertThrows(ValidationException.class,
                () -> service.assign(actor, 70L, new AssignTicketRequest("xe-ma")));
        assertEquals("vehicle_slug", error.getField());
        assertEquals(TicketStatus.Waiting, ticket.getStatus());
        verifyNoInteractions(historyRepository);
    }

    @Test
    void assignRejectsAVehicleOutsideTheSession() {
        Ticket ticket = ticket(70L, 1L, TicketStatus.Waiting);
        Vehicle vehicle = vehicle(3L, "xe-gau-01", VehicleStatus.Online);
        givenReadyToAssign(ticket, vehicle);
        when(sessionVehicleRepository.existsBySessionIdAndVehicleIdAndRemovedAtIsNull(1L, 3L)).thenReturn(false);
        ConflictException error = assertThrows(ConflictException.class,
                () -> service.assign(actor, 70L, new AssignTicketRequest("xe-gau-01")));
        assertEquals("VEHICLE_NOT_IN_SESSION", error.getCode());
        assertEquals(VehicleStatus.Online, vehicle.getStatus());
        assertEquals(TicketStatus.Waiting, ticket.getStatus());
        verifyNoInteractions(historyRepository);
    }

    @Test
    void assignOnlyAcceptsOnlineVehicles() {
        for (VehicleStatus status : new VehicleStatus[]{VehicleStatus.Offline, VehicleStatus.Repairing, VehicleStatus.Inused}) {
            Ticket ticket = ticket(70L, 1L, TicketStatus.Waiting);
            Vehicle vehicle = vehicle(3L, "xe-gau-01", status);
            givenReadyToAssign(ticket, vehicle);
            ConflictException error = assertThrows(ConflictException.class,
                    () -> service.assign(actor, 70L, new AssignTicketRequest("xe-gau-01")));
            assertEquals("VEHICLE_NOT_AVAILABLE", error.getCode());
            assertEquals(status, vehicle.getStatus());
            assertEquals(TicketStatus.Waiting, ticket.getStatus());
        }
        verify(ticketRepository, never()).save(any(Ticket.class));
        verifyNoInteractions(historyRepository);
        verify(auditLogService, never()).log(any(), any(), any(), any(), anyMap());
    }
}
