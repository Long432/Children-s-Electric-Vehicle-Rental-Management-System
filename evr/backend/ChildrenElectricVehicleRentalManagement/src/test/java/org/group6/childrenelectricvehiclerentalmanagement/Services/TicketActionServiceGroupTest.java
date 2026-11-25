package org.group6.childrenelectricvehiclerentalmanagement.Services;

import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Ticket.AssignGroupRequest;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Ticket.AssignGroupResponse;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Ticket.CancelGroupResponse;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Ticket.CancelTicketRequest;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Ticket.ReturnGroupResponse;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.PaymentMethod;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.SessionStatus;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.TicketStatus;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.VehicleStatus;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.ConflictException;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.TicketNotExits;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.ValidationException;
import org.group6.childrenelectricvehiclerentalmanagement.Models.Payment;
import org.group6.childrenelectricvehiclerentalmanagement.Models.Session;
import org.group6.childrenelectricvehiclerentalmanagement.Models.Ticket;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class TicketActionServiceGroupTest {

    private static final Instant NOW = Instant.parse("2026-11-05T10:00:00Z");
    private static final LocalDateTime STARTED = LocalDateTime.of(2026, 11, 5, 9, 30);
    private static final LocalDateTime TEN = LocalDateTime.of(2026, 11, 5, 10, 0);
    private static final long GROUP_ID = 70L;

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

    private Ticket groupTicket(long id, int no, TicketStatus status, Vehicle vehicle) {
        LocalDateTime queued = LocalDateTime.of(2026, 11, 5, 9, 20);
        Ticket ticket = new Ticket();
        ticket.setId(id);
        ticket.setSessionId(1L);
        ticket.setTicketNo(no);
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
        ticket.setGroupId(GROUP_ID);
        ticket.setGroupSize(3);
        if (vehicle != null) {
            ticket.setVehicle(vehicle);
            ticket.setStartedAt(STARTED);
            ticket.setDueAt(TEN);
        }
        return ticket;
    }

    private void givenGroup(Ticket... tickets) {
        when(sessionRepository.findByStatusForUpdate(SessionStatus.InProgress)).thenReturn(Optional.of(activeSession(1L)));
        when(ticketRepository.findByGroupIdOrderByTicketNoAsc(GROUP_ID)).thenReturn(Arrays.asList(tickets));
        for (Ticket ticket : tickets) {
            when(ticketRepository.findByIdForUpdate(ticket.getId())).thenReturn(Optional.of(ticket));
            if (ticket.getVehicle() != null) {
                when(vehicleRepository.findByIdForUpdate(ticket.getVehicle().getId())).thenReturn(Optional.of(ticket.getVehicle()));
            }
        }
    }

    private void givenFreeVehicle(Vehicle vehicle) {
        when(vehicleRepository.findActiveBySlugsForUpdate(List.of(vehicle.getSlug()))).thenReturn(List.of(vehicle));
        when(sessionVehicleRepository.existsBySessionIdAndVehicleIdAndRemovedAtIsNull(1L, vehicle.getId())).thenReturn(true);
    }

    @Test
    void assignGroupGivesEachWaitingTicketItsOwnVehicle() throws Exception {
        Ticket first = groupTicket(70L, 7, TicketStatus.Waiting, null);
        Ticket second = groupTicket(71L, 8, TicketStatus.Waiting, null);
        Vehicle bear = vehicle(3L, "xe-gau-01", VehicleStatus.Online);
        Vehicle cat = vehicle(4L, "xe-meo-02", VehicleStatus.Online);
        givenGroup(first, second);
        givenFreeVehicle(bear);
        givenFreeVehicle(cat);

        AssignGroupResponse response = service.assignGroup(actor, GROUP_ID, new AssignGroupRequest(List.of(" xe-gau-01 ", "xe-meo-02")));

        assertEquals(GROUP_ID, response.group_id());
        assertEquals(2, response.tickets().size());
        assertEquals(TicketStatus.Inused, first.getStatus());
        assertEquals(TicketStatus.Inused, second.getStatus());
        assertEquals("xe-gau-01", first.getVehicle().getSlug());
        assertEquals("xe-meo-02", second.getVehicle().getSlug());
        assertEquals(VehicleStatus.Inused, bear.getStatus());
        assertEquals(VehicleStatus.Inused, cat.getStatus());
        assertEquals(LocalDateTime.of(2026, 11, 5, 10, 30), first.getDueAt());
        assertEquals(LocalDateTime.of(2026, 11, 5, 10, 30), second.getDueAt());
    }

    @Test
    void assignGroupLeavesTicketsThatAlreadyHaveAVehicleAlone() throws Exception {
        Vehicle running = vehicle(2L, "xe-ca-00", VehicleStatus.Inused);
        Ticket started = groupTicket(70L, 7, TicketStatus.Inused, running);
        Ticket second = groupTicket(71L, 8, TicketStatus.Waiting, null);
        Ticket third = groupTicket(72L, 9, TicketStatus.Waiting, null);
        Vehicle bear = vehicle(3L, "xe-gau-01", VehicleStatus.Online);
        Vehicle cat = vehicle(4L, "xe-meo-02", VehicleStatus.Online);
        givenGroup(started, second, third);
        givenFreeVehicle(bear);
        givenFreeVehicle(cat);

        AssignGroupResponse response = service.assignGroup(actor, GROUP_ID, new AssignGroupRequest(List.of("xe-gau-01", "xe-meo-02")));

        assertEquals(2, response.tickets().size());
        assertEquals(TicketStatus.Inused, started.getStatus());
        assertEquals("xe-ca-00", started.getVehicle().getSlug());
        assertEquals("xe-gau-01", second.getVehicle().getSlug());
        assertEquals("xe-meo-02", third.getVehicle().getSlug());
    }

    @Test
    void assignGroupNeedsExactlyOneVehiclePerWaitingTicket() {
        Ticket first = groupTicket(70L, 7, TicketStatus.Waiting, null);
        Ticket second = groupTicket(71L, 8, TicketStatus.Waiting, null);
        givenGroup(first, second);

        ValidationException error = assertThrows(ValidationException.class,
                () -> service.assignGroup(actor, GROUP_ID, new AssignGroupRequest(List.of("xe-gau-01"))));

        assertEquals("vehicle_slugs", error.getField());
        assertEquals(TicketStatus.Waiting, first.getStatus());
        assertEquals(TicketStatus.Waiting, second.getStatus());
        verifyNoInteractions(vehicleRepository, historyRepository);
    }

    @Test
    void assignGroupFailsWhenNoTicketIsWaiting() {
        Vehicle running = vehicle(2L, "xe-ca-00", VehicleStatus.Inused);
        givenGroup(groupTicket(70L, 7, TicketStatus.Inused, running));

        ConflictException error = assertThrows(ConflictException.class,
                () -> service.assignGroup(actor, GROUP_ID, new AssignGroupRequest(List.of("xe-gau-01"))));

        assertEquals("GROUP_NOTHING_TO_ASSIGN", error.getCode());
    }

    @Test
    void assignGroupFailsWhenTheGroupDoesNotExist() {
        when(ticketRepository.findByGroupIdOrderByTicketNoAsc(99L)).thenReturn(List.of());

        assertThrows(TicketNotExits.class,
                () -> service.assignGroup(actor, 99L, new AssignGroupRequest(List.of("xe-gau-01"))));
    }

    @Test
    void assignGroupRejectsAnInvalidBodyBeforeTouchingTheDatabase() {
        assertThrows(ValidationException.class, () -> service.assignGroup(actor, GROUP_ID, null));
        assertThrows(ValidationException.class, () -> service.assignGroup(actor, GROUP_ID, new AssignGroupRequest(List.of())));
        assertThrows(ValidationException.class, () -> service.assignGroup(actor, GROUP_ID, new AssignGroupRequest(List.of("xe-gau-01", "xe-gau-01"))));
        verifyNoInteractions(sessionRepository, ticketRepository, vehicleRepository, historyRepository);
    }

    @Test
    void cancelGroupRefundsEveryWaitingTicketAndSkipsTheOthers() throws Exception {
        Vehicle running = vehicle(2L, "xe-ca-00", VehicleStatus.Inused);
        Ticket started = groupTicket(70L, 7, TicketStatus.Inused, running);
        Ticket second = groupTicket(71L, 8, TicketStatus.Waiting, null);
        Ticket third = groupTicket(72L, 9, TicketStatus.Waiting, null);
        givenGroup(started, second, third);

        CancelGroupResponse response = service.cancelGroup(actor, GROUP_ID, new CancelTicketRequest(null));

        assertEquals(GROUP_ID, response.group_id());
        assertEquals(2, response.canceled_count());
        assertEquals(100000, response.refund_amount());
        assertEquals(TicketStatus.Inused, started.getStatus());
        assertEquals(TicketStatus.Canceled, second.getStatus());
        assertEquals(TicketStatus.Canceled, third.getStatus());
        verify(paymentRepository, times(2)).save(any(Payment.class));
    }

    @Test
    void cancelGroupFailsWhenNoTicketIsWaiting() {
        Vehicle running = vehicle(2L, "xe-ca-00", VehicleStatus.Inused);
        givenGroup(groupTicket(70L, 7, TicketStatus.Inused, running));

        ConflictException error = assertThrows(ConflictException.class,
                () -> service.cancelGroup(actor, GROUP_ID, null));

        assertEquals("GROUP_NOTHING_TO_CANCEL", error.getCode());
        verifyNoInteractions(paymentRepository);
    }

    @Test
    void returnGroupFreesEveryRunningVehicle() throws Exception {
        Vehicle bear = vehicle(3L, "xe-gau-01", VehicleStatus.Inused);
        Vehicle cat = vehicle(4L, "xe-meo-02", VehicleStatus.Inused);
        Ticket first = groupTicket(70L, 7, TicketStatus.Inused, bear);
        Ticket second = groupTicket(71L, 8, TicketStatus.Inused, cat);
        Ticket waiting = groupTicket(72L, 9, TicketStatus.Waiting, null);
        givenGroup(first, second, waiting);

        ReturnGroupResponse response = service.returnGroup(actor, GROUP_ID, null);

        assertEquals(GROUP_ID, response.group_id());
        assertEquals(2, response.returned_count());
        assertEquals(0, response.overdue_fee());
        assertEquals(0, response.refund_amount());
        assertEquals(TicketStatus.Completed, first.getStatus());
        assertEquals(TicketStatus.Completed, second.getStatus());
        assertEquals(TicketStatus.Waiting, waiting.getStatus());
        assertEquals(VehicleStatus.Online, bear.getStatus());
        assertEquals(VehicleStatus.Online, cat.getStatus());
    }

    @Test
    void returnGroupFailsWhenNoTicketIsRunning() {
        givenGroup(groupTicket(70L, 7, TicketStatus.Waiting, null));

        ConflictException error = assertThrows(ConflictException.class,
                () -> service.returnGroup(actor, GROUP_ID, null));

        assertEquals("GROUP_NOTHING_TO_RETURN", error.getCode());
    }
}
