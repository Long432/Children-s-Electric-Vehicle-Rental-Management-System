package org.group6.childrenelectricvehiclerentalmanagement.Services;

import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Ticket.ChangeVehicleRequest;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Ticket.ChangeVehicleResponse;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.PaymentMethod;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.ReleasedVehicleStatus;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

class TicketActionServiceChangeVehicleTest {

    private static final Instant NOW = Instant.parse("2026-11-05T10:00:00Z");
    private static final LocalDateTime STARTED = LocalDateTime.of(2026, 11, 5, 9, 40);
    private static final LocalDateTime DUE = LocalDateTime.of(2026, 11, 5, 10, 10);

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

    private Vehicle vehicle(long id, String slug, VehicleStatus status) {
        Vehicle vehicle = new Vehicle(slug);
        ReflectionTestUtils.setField(vehicle, "id", id);
        vehicle.setStatus(status);
        return vehicle;
    }

    private Ticket ticket(long id, long sessionId, TicketStatus status, Vehicle vehicle) {
        LocalDateTime queued = LocalDateTime.of(2026, 11, 5, 9, 30);
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
            ticket.setDueAt(DUE);
        }
        return ticket;
    }

    private TicketVehicleHistory openHistory(long ticketId, long vehicleId) {
        return new TicketVehicleHistory(ticketId, vehicleId, STARTED);
    }

    private void givenReady(Ticket ticket, Vehicle old, Vehicle replacement) {
        when(sessionRepository.findByStatusForUpdate(SessionStatus.InProgress)).thenReturn(Optional.of(activeSession(1L)));
        when(ticketRepository.findByIdForUpdate(ticket.getId())).thenReturn(Optional.of(ticket));
        when(vehicleRepository.findActiveBySlugsForUpdate(List.of(replacement.getSlug()))).thenReturn(List.of(replacement));
        when(sessionVehicleRepository.existsBySessionIdAndVehicleIdAndRemovedAtIsNull(1L, replacement.getId())).thenReturn(true);
        when(vehicleRepository.findByIdForUpdate(old.getId())).thenReturn(Optional.of(old));
    }

    @Test
    void changeSwapsVehiclesAndKeepsTheClockRunning() throws Exception {
        Vehicle old = vehicle(3L, "xe-gau-01", VehicleStatus.Inused);
        Vehicle replacement = vehicle(4L, "xe-gau-02", VehicleStatus.Online);
        Ticket ticket = ticket(70L, 1L, TicketStatus.Inused, old);
        givenReady(ticket, old, replacement);
        TicketVehicleHistory open = openHistory(70L, 3L);
        when(historyRepository.findFirstByTicketIdAndVehicleIdAndReleasedAtIsNullOrderByIdDesc(70L, 3L)).thenReturn(Optional.of(open));

        ChangeVehicleResponse response = service.changeVehicle(actor, 70L, new ChangeVehicleRequest(" xe-gau-02 ", "Online"));

        assertEquals(VehicleStatus.Online, old.getStatus());
        assertEquals(VehicleStatus.Inused, replacement.getStatus());
        assertEquals(replacement, ticket.getVehicle());
        assertEquals(TicketStatus.Inused, ticket.getStatus());
        assertEquals(STARTED, ticket.getStartedAt());
        assertEquals(DUE, ticket.getDueAt());
        assertEquals("xe-gau-02", response.ticket().vehicle().slug());
        assertEquals("xe-gau-01", response.old_vehicle().slug());
        assertEquals(VehicleStatus.Online, response.old_vehicle().status());
        assertEquals(LocalDateTime.of(2026, 11, 5, 10, 0), open.getReleasedAt());
        assertEquals(ReleasedVehicleStatus.Online, open.getReleasedStatus());
        ArgumentCaptor<TicketVehicleHistory> history = ArgumentCaptor.forClass(TicketVehicleHistory.class);
        verify(historyRepository, times(2)).save(history.capture());
        assertEquals(4L, history.getAllValues().get(1).getVehicleId());
        assertEquals(LocalDateTime.of(2026, 11, 5, 10, 0), history.getAllValues().get(1).getAssignedAt());
        assertNull(history.getAllValues().get(1).getReleasedAt());
        verify(auditLogService).log(any(), eq("ticket.change_vehicle"), eq("ticket"), eq(70L), anyMap());
    }

    @Test
    void oldVehicleCanBeSentToRepair() throws Exception {
        Vehicle old = vehicle(3L, "xe-gau-01", VehicleStatus.Inused);
        Vehicle replacement = vehicle(4L, "xe-gau-02", VehicleStatus.Online);
        Ticket ticket = ticket(70L, 1L, TicketStatus.Inused, old);
        givenReady(ticket, old, replacement);
        TicketVehicleHistory open = openHistory(70L, 3L);
        when(historyRepository.findFirstByTicketIdAndVehicleIdAndReleasedAtIsNullOrderByIdDesc(70L, 3L)).thenReturn(Optional.of(open));

        ChangeVehicleResponse response = service.changeVehicle(actor, 70L, new ChangeVehicleRequest("xe-gau-02", "Repairing"));

        assertEquals(VehicleStatus.Repairing, old.getStatus());
        assertEquals(VehicleStatus.Repairing, response.old_vehicle().status());
        assertEquals(ReleasedVehicleStatus.Repairing, open.getReleasedStatus());
    }

    @Test
    void overdueTicketKeepsItsStatusAndDueTime() throws Exception {
        Vehicle old = vehicle(3L, "xe-gau-01", VehicleStatus.Inused);
        Vehicle replacement = vehicle(4L, "xe-gau-02", VehicleStatus.Online);
        Ticket ticket = ticket(70L, 1L, TicketStatus.Overdue, old);
        givenReady(ticket, old, replacement);
        when(historyRepository.findFirstByTicketIdAndVehicleIdAndReleasedAtIsNullOrderByIdDesc(70L, 3L))
                .thenReturn(Optional.of(openHistory(70L, 3L)));

        service.changeVehicle(actor, 70L, new ChangeVehicleRequest("xe-gau-02", "Online"));

        assertEquals(TicketStatus.Overdue, ticket.getStatus());
        assertEquals(DUE, ticket.getDueAt());
        assertEquals(replacement, ticket.getVehicle());
    }

    @Test
    void missingOpenHistoryRowIsRebuiltFromTheTicketStart() throws Exception {
        Vehicle old = vehicle(3L, "xe-gau-01", VehicleStatus.Inused);
        Vehicle replacement = vehicle(4L, "xe-gau-02", VehicleStatus.Online);
        Ticket ticket = ticket(70L, 1L, TicketStatus.Inused, old);
        givenReady(ticket, old, replacement);
        when(historyRepository.findFirstByTicketIdAndVehicleIdAndReleasedAtIsNullOrderByIdDesc(70L, 3L)).thenReturn(Optional.empty());

        service.changeVehicle(actor, 70L, new ChangeVehicleRequest("xe-gau-02", "Online"));

        ArgumentCaptor<TicketVehicleHistory> history = ArgumentCaptor.forClass(TicketVehicleHistory.class);
        verify(historyRepository, times(2)).save(history.capture());
        TicketVehicleHistory rebuilt = history.getAllValues().get(0);
        assertEquals(3L, rebuilt.getVehicleId());
        assertEquals(STARTED, rebuilt.getAssignedAt());
        assertEquals(LocalDateTime.of(2026, 11, 5, 10, 0), rebuilt.getReleasedAt());
    }

    @Test
    void waitingTicketCannotChangeVehicle() {
        Vehicle replacement = vehicle(4L, "xe-gau-02", VehicleStatus.Online);
        Ticket ticket = ticket(70L, 1L, TicketStatus.Waiting, null);
        when(sessionRepository.findByStatusForUpdate(SessionStatus.InProgress)).thenReturn(Optional.of(activeSession(1L)));
        when(ticketRepository.findByIdForUpdate(70L)).thenReturn(Optional.of(ticket));
        ConflictException error = assertThrows(ConflictException.class,
                () -> service.changeVehicle(actor, 70L, new ChangeVehicleRequest(replacement.getSlug(), "Online")));
        assertEquals("TICKET_NOT_IN_USE", error.getCode());
        verifyNoInteractions(vehicleRepository, historyRepository);
    }

    @Test
    void finishedTicketsCannotChangeVehicle() {
        for (TicketStatus status : new TicketStatus[]{TicketStatus.Completed, TicketStatus.Canceled}) {
            Ticket ticket = ticket(70L, 1L, status, null);
            when(sessionRepository.findByStatusForUpdate(SessionStatus.InProgress)).thenReturn(Optional.of(activeSession(1L)));
            when(ticketRepository.findByIdForUpdate(70L)).thenReturn(Optional.of(ticket));
            ConflictException error = assertThrows(ConflictException.class,
                    () -> service.changeVehicle(actor, 70L, new ChangeVehicleRequest("xe-gau-02", "Online")));
            assertEquals("TICKET_NOT_IN_USE", error.getCode());
        }
    }

    @Test
    void sameVehicleIsRejected() {
        Vehicle old = vehicle(3L, "xe-gau-01", VehicleStatus.Inused);
        Ticket ticket = ticket(70L, 1L, TicketStatus.Inused, old);
        when(sessionRepository.findByStatusForUpdate(SessionStatus.InProgress)).thenReturn(Optional.of(activeSession(1L)));
        when(ticketRepository.findByIdForUpdate(70L)).thenReturn(Optional.of(ticket));
        when(vehicleRepository.findActiveBySlugsForUpdate(List.of("xe-gau-01"))).thenReturn(List.of(old));
        ConflictException error = assertThrows(ConflictException.class,
                () -> service.changeVehicle(actor, 70L, new ChangeVehicleRequest("xe-gau-01", "Online")));
        assertEquals("VEHICLE_UNCHANGED", error.getCode());
        assertEquals(VehicleStatus.Inused, old.getStatus());
    }

    @Test
    void replacementMustBeOnlineAndNothingChangesOtherwise() {
        Vehicle old = vehicle(3L, "xe-gau-01", VehicleStatus.Inused);
        Vehicle replacement = vehicle(4L, "xe-gau-02", VehicleStatus.Repairing);
        Ticket ticket = ticket(70L, 1L, TicketStatus.Inused, old);
        givenReady(ticket, old, replacement);
        ConflictException error = assertThrows(ConflictException.class,
                () -> service.changeVehicle(actor, 70L, new ChangeVehicleRequest("xe-gau-02", "Online")));
        assertEquals("VEHICLE_NOT_AVAILABLE", error.getCode());
        assertEquals(VehicleStatus.Inused, old.getStatus());
        assertEquals(VehicleStatus.Repairing, replacement.getStatus());
        assertEquals(old, ticket.getVehicle());
        verify(historyRepository, never()).save(any());
    }

    @Test
    void replacementMustBelongToTheSession() {
        Vehicle old = vehicle(3L, "xe-gau-01", VehicleStatus.Inused);
        Vehicle replacement = vehicle(4L, "xe-gau-02", VehicleStatus.Online);
        Ticket ticket = ticket(70L, 1L, TicketStatus.Inused, old);
        givenReady(ticket, old, replacement);
        when(sessionVehicleRepository.existsBySessionIdAndVehicleIdAndRemovedAtIsNull(1L, 4L)).thenReturn(false);
        ConflictException error = assertThrows(ConflictException.class,
                () -> service.changeVehicle(actor, 70L, new ChangeVehicleRequest("xe-gau-02", "Online")));
        assertEquals("VEHICLE_NOT_IN_SESSION", error.getCode());
        assertEquals(VehicleStatus.Online, replacement.getStatus());
    }

    @Test
    void unknownVehicleIsRejected() {
        Vehicle old = vehicle(3L, "xe-gau-01", VehicleStatus.Inused);
        Ticket ticket = ticket(70L, 1L, TicketStatus.Inused, old);
        when(sessionRepository.findByStatusForUpdate(SessionStatus.InProgress)).thenReturn(Optional.of(activeSession(1L)));
        when(ticketRepository.findByIdForUpdate(70L)).thenReturn(Optional.of(ticket));
        when(vehicleRepository.findActiveBySlugsForUpdate(List.of("xe-gau-09"))).thenReturn(List.of());
        ValidationException error = assertThrows(ValidationException.class,
                () -> service.changeVehicle(actor, 70L, new ChangeVehicleRequest("xe-gau-09", "Online")));
        assertEquals("vehicle_slug", error.getField());
    }

    @Test
    void ticketFromAnotherSessionIsRejected() {
        Vehicle old = vehicle(3L, "xe-gau-01", VehicleStatus.Inused);
        Ticket ticket = ticket(70L, 2L, TicketStatus.Inused, old);
        when(sessionRepository.findByStatusForUpdate(SessionStatus.InProgress)).thenReturn(Optional.of(activeSession(1L)));
        when(ticketRepository.findByIdForUpdate(70L)).thenReturn(Optional.of(ticket));
        ConflictException error = assertThrows(ConflictException.class,
                () -> service.changeVehicle(actor, 70L, new ChangeVehicleRequest("xe-gau-02", "Online")));
        assertEquals("TICKET_NOT_IN_ACTIVE_SESSION", error.getCode());
    }

    @Test
    void requiresAnActiveSessionAndAnExistingTicket() {
        when(sessionRepository.findByStatusForUpdate(SessionStatus.InProgress)).thenReturn(Optional.empty());
        assertThrows(NoActiveSessionException.class,
                () -> service.changeVehicle(actor, 70L, new ChangeVehicleRequest("xe-gau-02", "Online")));

        when(sessionRepository.findByStatusForUpdate(SessionStatus.InProgress)).thenReturn(Optional.of(activeSession(1L)));
        when(ticketRepository.findByIdForUpdate(71L)).thenReturn(Optional.empty());
        assertThrows(TicketNotExits.class,
                () -> service.changeVehicle(actor, 71L, new ChangeVehicleRequest("xe-gau-02", "Online")));
    }

    @Test
    void invalidRequestIsRejectedBeforeAnyLookup() {
        assertThrows(ValidationException.class, () -> service.changeVehicle(actor, 70L, new ChangeVehicleRequest("xe-gau-02", "Offline")));
        verifyNoInteractions(sessionRepository, ticketRepository, vehicleRepository);
    }
}
