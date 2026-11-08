package org.group6.childrenelectricvehiclerentalmanagement.unit;

import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Long.Requests;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.PaymentMethod;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.PaymentType;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.ReleasedVehicleStatus;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.SessionStatus;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.TicketStatus;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.VehicleStatus;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.*;
import org.group6.childrenelectricvehiclerentalmanagement.Models.Package;
import org.group6.childrenelectricvehiclerentalmanagement.Models.Session;
import org.group6.childrenelectricvehiclerentalmanagement.Models.Settings;
import org.group6.childrenelectricvehiclerentalmanagement.Models.Ticket;
import org.group6.childrenelectricvehiclerentalmanagement.Models.User;
import org.group6.childrenelectricvehiclerentalmanagement.Models.Vehicle;
import org.group6.childrenelectricvehiclerentalmanagement.Models.LinkingObject.TicketVehicleHistory;
import org.group6.childrenelectricvehiclerentalmanagement.Repositories.*;
import org.group6.childrenelectricvehiclerentalmanagement.Services.SettingsService;
import org.group6.childrenelectricvehiclerentalmanagement.Services.TicketService;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.ValidPackage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit test cho TicketService. Các repository và SettingsService được mock.
 * Dựa trên nghiệp vụ quản lý vé (features.md section 2.3, phân tích nghiệp vụ section 5.2-5.6, 6.1).
 */
@ExtendWith(MockitoExtension.class)
class TicketServiceUnitTest {

    @Mock private TicketRepository ticketRepository;
    @Mock private SessionRepository sessionRepository;
    @Mock private SessionVehicleRepository sessionVehicleRepository;
    @Mock private VehicleRepository vehicleRepository;
    @Mock private PaymentRepository paymentRepository;
    @Mock private TicketVehicleHistoryRepository historyRepository;
    @Mock private PackageRepository packageRepository;
    @Mock private UserRepository userRepository;
    @Mock private SettingsService settingsService;
    @InjectMocks private TicketService ticketService;

    private Session session;
    private User actor;
    private Vehicle vehicle;
    private Settings settings;

    @BeforeEach
    void setUp() {
        session = new Session();
        session.setId(100L);
        session.setStatus(SessionStatus.InProgress);
        session.setNextTicketNo(1);

        actor = new User();
        actor.setId(1L);
        actor.setUsername("seller");

        vehicle = new Vehicle("xe-01");
        vehicle.setId(1L);
        vehicle.setStatus(VehicleStatus.Online);

        settings = new Settings();
        settings.setId((short) 1);
        settings.setRefundEnabled(false);
        settings.setExtendEnabled(true);
        settings.setOverdueFeePerMinute(2000);
        settings.setOverdueGraceMinutes(5);
        settings.setMinimumMinutes(10);
    }

    // ==================== createTicket ====================

    @Test
    void createTicket_success() throws Exception {
        Requests.CreateTicketRequest req = new Requests.CreateTicketRequest();
        req.setActorId(1L);
        req.setPhone("0901234567");
        req.setMinutes("30");
        req.setPaymentMethod("Cash");

        when(settingsService.loadSettings()).thenReturn(settings);
        Package pkg = new Package();
        pkg.setAmount(15000);
        pkg.setMinutes(30);
        pkg.setIsDefault(true);
        pkg.setActive(true);
        when(packageRepository.findByActiveTrueOrderByMinutesAsc()).thenReturn(List.of(pkg));
        when(sessionRepository.lockByStatus(SessionStatus.InProgress)).thenReturn(List.of(session));
        when(userRepository.getReferenceById(1L)).thenReturn(actor);
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> {
            Ticket t = inv.getArgument(0);
            t.setId(1L);
            return t;
        });
        when(sessionRepository.save(any(Session.class))).thenReturn(session);
        when(sessionVehicleRepository.findBySessionIdAndRemovedAtIsNull(100L)).thenReturn(List.of());

        var res = ticketService.createTicket(req);

        assertNotNull(res);
        assertEquals(1, res.ticket().ticketNo());
        assertEquals(TicketStatus.Waiting, res.ticket().status());
        verify(ticketRepository).save(any(Ticket.class));
        verify(paymentRepository).save(any());
    }

    @Test
    void createTicket_belowMinimumTime_shouldThrow() throws Exception {
        Requests.CreateTicketRequest req = new Requests.CreateTicketRequest();
        req.setActorId(1L);
        req.setPhone("0901234567");
        req.setMinutes("5"); // dưới minimum 10
        req.setPaymentMethod("Cash");

        when(settingsService.loadSettings()).thenReturn(settings);

        assertThrows(BelowMinimumTime.class, () -> ticketService.createTicket(req));
    }

    @Test
    void createTicket_noActiveSession_shouldThrowNoActiveSession() throws Exception {
        Requests.CreateTicketRequest req = new Requests.CreateTicketRequest();
        req.setActorId(1L);
        req.setPhone("0901234567");
        req.setMinutes("30");
        req.setPaymentMethod("Cash");

        when(settingsService.loadSettings()).thenReturn(settings);
        when(sessionRepository.lockByStatus(SessionStatus.InProgress)).thenReturn(List.of());

        assertThrows(NoActiveSession.class, () -> ticketService.createTicket(req));
    }

    @Test
    void createTicket_noDefaultPackage_shouldThrowNoPricePackage() throws Exception {
        Requests.CreateTicketRequest req = new Requests.CreateTicketRequest();
        req.setActorId(1L);
        req.setPhone("0901234567");
        req.setMinutes("30");
        req.setPaymentMethod("Cash");

        when(settingsService.loadSettings()).thenReturn(settings);
        when(packageRepository.findByActiveTrueOrderByMinutesAsc()).thenReturn(List.of());
        when(sessionRepository.lockByStatus(SessionStatus.InProgress)).thenReturn(List.of(session));

        assertThrows(NoPricePackage.class, () -> ticketService.createTicket(req));
    }

    // ==================== assignVehicle ====================

    @Test
    void assignVehicle_success() throws Exception {
        Requests.AssignVehicleRequest req = new Requests.AssignVehicleRequest();
        req.setActorId(1L);
        req.setId("1");
        req.setVehicleSlug("xe-01");

        Ticket ticket = new Ticket();
        ticket.setId(1L);
        ticket.setTicketNo(1);
        ticket.setSession(session);
        ticket.setStatus(TicketStatus.Waiting);
        ticket.setVehicle(null);
        ticket.setPaymentMethod(PaymentMethod.Cash);
        ticket.setTotalMinutes(30);
        ticket.setAmountPaid(15000);
        ticket.setPriceAmount(15000);
        ticket.setPriceMinutes(30);
        ticket.setOverdueFeePerMinute(2000);
        ticket.setOverdueGraceMinutes(5);
        ticket.setRefundEnabled(false);

        when(ticketRepository.lockById(1L)).thenReturn(Optional.of(ticket));
        when(ticket.getSession()).thenReturn(session);
        when(vehicleRepository.lockBySlug("xe-01")).thenReturn(Optional.of(vehicle));
        when(sessionVehicleRepository.existsBySessionIdAndVehicleIdAndRemovedAtIsNull(100L, 1L)).thenReturn(true);
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(vehicle);
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);
        when(historyRepository.save(any(TicketVehicleHistory.class))).thenAnswer(inv -> inv.getArgument(0));

        var res = ticketService.assignVehicle(req);

        assertNotNull(res);
        assertEquals(TicketStatus.Inused, res.status());
        assertEquals(VehicleStatus.Inused, vehicle.getStatus());
    }

    @Test
    void assignVehicle_ticketNotWaiting_shouldThrowTicketNotAssignable() throws Exception {
        Requests.AssignVehicleRequest req = new Requests.AssignVehicleRequest();
        req.setActorId(1L);
        req.setId("1");
        req.setVehicleSlug("xe-01");

        Ticket ticket = new Ticket();
        ticket.setId(1L);
        ticket.setSession(session);
        ticket.setStatus(TicketStatus.Inused);

        when(ticketRepository.lockById(1L)).thenReturn(Optional.of(ticket));
        when(ticket.getSession()).thenReturn(session);

        assertThrows(TicketNotAssignable.class, () -> ticketService.assignVehicle(req));
    }

    @Test
    void assignVehicle_sessionNotActive_shouldThrowSessionNotActive() throws Exception {
        Requests.AssignVehicleRequest req = new Requests.AssignVehicleRequest();
        req.setActorId(1L);
        req.setId("1");
        req.setVehicleSlug("xe-01");

        Ticket ticket = new Ticket();
        ticket.setId(1L);
        ticket.setSession(new Session());
        ticket.getSession().setStatus(SessionStatus.Ended);
        ticket.setStatus(TicketStatus.Waiting);

        when(ticketRepository.lockById(1L)).thenReturn(Optional.of(ticket));
        when(ticket.getSession()).thenReturn(ticket.getSession());

        assertThrows(SessionNotActive.class, () -> ticketService.assignVehicle(req));
    }

    @Test
    void assignVehicle_vehicleNotOnline_shouldThrowVehicleNotAvailable() throws Exception {
        Requests.AssignVehicleRequest req = new Requests.AssignVehicleRequest();
        req.setActorId(1L);
        req.setId("1");
        req.setVehicleSlug("xe-01");

        Ticket ticket = new Ticket();
        ticket.setId(1L);
        ticket.setSession(session);
        ticket.setStatus(TicketStatus.Waiting);

        Vehicle notOnline = new Vehicle("xe-02");
        notOnline.setId(2L);
        notOnline.setStatus(VehicleStatus.Repairing);

        when(ticketRepository.lockById(1L)).thenReturn(Optional.of(ticket));
        when(ticket.getSession()).thenReturn(session);
        when(vehicleRepository.lockBySlug("xe-01")).thenReturn(Optional.of(notOnline));

        assertThrows(VehicleNotAvailable.class, () -> ticketService.assignVehicle(req));
    }

    // ==================== cancelTicket ====================

    @Test
    void cancelTicket_waiting_shouldCancelAndRefund() throws Exception {
        Requests.CancelTicketRequest req = new Requests.CancelTicketRequest();
        req.setActorId(1L);
        req.setId("1");

        Ticket ticket = new Ticket();
        ticket.setId(1L);
        ticket.setTicketNo(1);
        ticket.setSession(session);
        ticket.setStatus(TicketStatus.Waiting);
        ticket.setAmountPaid(15000);
        ticket.setTotalMinutes(30);
        ticket.setPaymentMethod(PaymentMethod.Cash);

        when(ticketRepository.lockById(1L)).thenReturn(Optional.of(ticket));
        when(ticket.getSession()).thenReturn(session);
        when(userRepository.getReferenceById(1L)).thenReturn(actor);
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);
        when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var res = ticketService.cancelTicket(req);

        assertNotNull(res);
        assertEquals(TicketStatus.Canceled, res.status());
        assertEquals(15000, res.refundAmount());
    }

    @Test
    void cancelTicket_notWaiting_shouldThrowTicketNotCancelable() throws Exception {
        Requests.CancelTicketRequest req = new Requests.CancelTicketRequest();
        req.setActorId(1L);
        req.setId("1");

        Ticket ticket = new Ticket();
        ticket.setId(1L);
        ticket.setSession(session);
        ticket.setStatus(TicketStatus.Inused);

        when(ticketRepository.lockById(1L)).thenReturn(Optional.of(ticket));
        when(ticket.getSession()).thenReturn(session);

        assertThrows(TicketNotCancelable.class, () -> ticketService.cancelTicket(req));
    }

    // ==================== changeVehicle ====================

    @Test
    void changeVehicle_success() throws Exception {
        Requests.ChangeVehicleRequest req = new Requests.ChangeVehicleRequest();
        req.setActorId(1L);
        req.setId("1");
        req.setVehicleSlug("xe-02");
        req.setOldVehicleStatus("Online");

        Ticket ticket = new Ticket();
        ticket.setId(1L);
        ticket.setSession(session);
        ticket.setStatus(TicketStatus.Inused);
        ticket.setVehicle(vehicle);

        Vehicle newVehicle = new Vehicle("xe-02");
        newVehicle.setId(2L);
        newVehicle.setStatus(VehicleStatus.Online);

        when(ticketRepository.lockById(1L)).thenReturn(Optional.of(ticket));
        when(ticket.getSession()).thenReturn(session);
        when(vehicleRepository.lockBySlug("xe-02")).thenReturn(Optional.of(newVehicle));
        when(sessionVehicleRepository.existsBySessionIdAndVehicleIdAndRemovedAtIsNull(100L, 2L)).thenReturn(true);
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(inv -> inv.getArgument(0));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);
        when(historyRepository.save(any(TicketVehicleHistory.class))).thenAnswer(inv -> inv.getArgument(0));

        var res = ticketService.changeVehicle(req);

        assertNotNull(res);
        assertEquals(newVehicle, res.ticket().vehicle());
    }

    @Test
    void changeVehicle_sameVehicle_shouldThrowSameVehicle() throws Exception {
        Requests.ChangeVehicleRequest req = new Requests.ChangeVehicleRequest();
        req.setActorId(1L);
        req.setId("1");
        req.setVehicleSlug("xe-01");
        req.setOldVehicleStatus("Online");

        Ticket ticket = new Ticket();
        ticket.setId(1L);
        ticket.setSession(session);
        ticket.setStatus(TicketStatus.Inused);
        ticket.setVehicle(vehicle);

        when(ticketRepository.lockById(1L)).thenReturn(Optional.of(ticket));
        when(ticket.getSession()).thenReturn(session);

        assertThrows(SameVehicle.class, () -> ticketService.changeVehicle(req));
    }

    // ==================== extendTicket ====================

    @Test
    void extendTicket_success() throws Exception {
        Requests.ExtendTicketRequest req = new Requests.ExtendTicketRequest();
        req.setActorId(1L);
        req.setId("1");
        req.setMinutes("30");
        req.setPaymentMethod("Cash");

        Ticket ticket = new Ticket();
        ticket.setId(1L);
        ticket.setSession(session);
        ticket.setStatus(TicketStatus.Inused);
        ticket.setTotalMinutes(30);
        ticket.setAmountPaid(15000);
        ticket.setPriceAmount(15000);
        ticket.setPriceMinutes(30);
        ticket.setDueAt(LocalDateTime.now().plusMinutes(30));
        ticket.setPaymentMethod(PaymentMethod.Cash);

        when(settingsService.loadSettings()).thenReturn(settings);
        when(ticketRepository.lockById(1L)).thenReturn(Optional.of(ticket));
        when(ticket.getSession()).thenReturn(session);
        when(userRepository.getReferenceById(1L)).thenReturn(actor);
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);
        when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var res = ticketService.extendTicket(req);

        assertNotNull(res);
        assertEquals(60, res.ticket().totalMinutes());
    }

    @Test
    void extendTicket_extendDisabled_shouldThrowExtendDisabled() throws Exception {
        Settings disabled = new Settings();
        disabled.setExtendEnabled(false);

        Requests.ExtendTicketRequest req = new Requests.ExtendTicketRequest();
        req.setActorId(1L);
        req.setId("1");
        req.setMinutes("30");
        req.setPaymentMethod("Cash");

        when(settingsService.loadSettings()).thenReturn(disabled);

        assertThrows(ExtendDisabled.class, () -> ticketService.extendTicket(req));
    }

    // ==================== returnTicket ====================

    @Test
    void returnTicket_success_earlyReturnWithRefund() throws Exception {
        settings.setRefundEnabled(true);

        Requests.ReturnTicketRequest req = new Requests.ReturnTicketRequest();
        req.setActorId(1L);
        req.setId("1");

        Ticket ticket = new Ticket();
        ticket.setId(1L);
        ticket.setTicketNo(1);
        ticket.setSession(session);
        ticket.setStatus(TicketStatus.Inused);
        ticket.setVehicle(vehicle);
        ticket.setTotalMinutes(60);
        ticket.setAmountPaid(30000);
        ticket.setPriceAmount(30000);
        ticket.setPriceMinutes(60);
        ticket.setOverdueFeePerMinute(2000);
        ticket.setOverdueGraceMinutes(5);
        ticket.setRefundEnabled(true);
        ticket.setDueAt(LocalDateTime.now().minusMinutes(10)); // còn 10 phút

        when(ticketRepository.lockById(1L)).thenReturn(Optional.of(ticket));
        when(ticket.getSession()).thenReturn(session);
        when(userRepository.getReferenceById(1L)).thenReturn(actor);
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);
        when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(vehicle);
        when(historyRepository.findFirstByTicketIdAndReleasedAtIsNull(1L)).thenReturn(Optional.empty());
        when(ticketRepository.findFirstBySessionIdAndStatusOrderByQueuedAtAscIdAsc(100L, TicketStatus.Waiting))
                .thenReturn(Optional.empty());

        var res = ticketService.returnTicket(req);

        assertNotNull(res);
        assertEquals(TicketStatus.Completed, res.status());
        assertEquals(VehicleStatus.Online, vehicle.getStatus());
    }

    @Test
    void returnTicket_overdue_shouldChargeFee() throws Exception {
        Requests.ReturnTicketRequest req = new Requests.ReturnTicketRequest();
        req.setActorId(1L);
        req.setId("1");

        Ticket ticket = new Ticket();
        ticket.setId(1L);
        ticket.setSession(session);
        ticket.setStatus(TicketStatus.Inused);
        ticket.setVehicle(vehicle);
        ticket.setOverdueFeePerMinute(2000);
        ticket.setOverdueGraceMinutes(5);
        ticket.setDueAt(LocalDateTime.now().minusMinutes(12)); // quá 12 phút

        when(ticketRepository.lockById(1L)).thenReturn(Optional.of(ticket));
        when(ticket.getSession()).thenReturn(session);
        when(userRepository.getReferenceById(1L)).thenReturn(actor);
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);
        when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(vehicle);
        when(historyRepository.findFirstByTicketIdAndReleasedAtIsNull(1L)).thenReturn(Optional.empty());
        when(ticketRepository.findFirstBySessionIdAndStatusOrderByQueuedAtAscIdAsc(100L, TicketStatus.Waiting))
                .thenReturn(Optional.empty());

        var res = ticketService.returnTicket(req);

        assertNotNull(res);
        assertEquals(TicketStatus.Completed, res.status());
        assertTrue(res.overdueMinutes() > 0);
        assertTrue(res.overdueFee() > 0);
    }

    @Test
    void returnTicket_notActive_shouldThrowTicketNotReturnable() throws Exception {
        Requests.ReturnTicketRequest req = new Requests.ReturnTicketRequest();
        req.setActorId(1L);
        req.setId("1");

        Ticket ticket = new Ticket();
        ticket.setId(1L);
        ticket.setSession(session);
        ticket.setStatus(TicketStatus.Waiting); // không phải Inused/Overdue

        when(ticketRepository.lockById(1L)).thenReturn(Optional.of(ticket));
        when(ticket.getSession()).thenReturn(session);

        assertThrows(TicketNotActive.class, () -> ticketService.returnTicket(req));
    }
}
