package org.group6.childrenelectricvehiclerentalmanagement.unit;

import org.group6.childrenelectricvehiclerentalmanagement.DTOs.SessionController.AddSessionVehiclesRequest;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.SessionController.CreateSessionRequest;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.SessionController.SessionDetailResponse;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.SessionController.StartSessionRequest;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.SessionController.UpdateSessionRequest;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.VehicleController.vehicleInfoRespond;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.SessionStatus;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.TicketStatus;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.VehicleStatus;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.*;
import org.group6.childrenelectricvehiclerentalmanagement.Models.Session;
import org.group6.childrenelectricvehiclerentalmanagement.Models.User;
import org.group6.childrenelectricvehiclerentalmanagement.Models.Vehicle;
import org.group6.childrenelectricvehiclerentalmanagement.Models.LinkingObject.SessionVehicle;
import org.group6.childrenelectricvehiclerentalmanagement.Repositories.*;
import org.group6.childrenelectricvehiclerentalmanagement.Services.SessionService;
import org.group6.childrenelectricvehiclerentalmanagement.Services.TicketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit test cho SessionService. Tất cả dependencies (repository, service) được mock.
 * Dựa trên nghiệp vụ quản lý phiên làm (features.md section 2.1, phân tích nghiệp vụ section 5.1).
 */
@ExtendWith(MockitoExtension.class)
class SessionServiceUnitTest {

    @Mock private SessionRepository sessionRepository;
    @Mock private TicketRepository ticketRepository;
    @Mock private PaymentRepository paymentRepository;
    @Mock private SessionVehicleRepository sessionVehicleRepository;
    @Mock private TicketService ticketService;
    @Mock private VehicleRepository vehicleRepository;
    @Mock private org.group6.childrenelectricvehiclerentalmanagement.Services.AuditLogService auditLogService;
    @InjectMocks private SessionService sessionService;

    private User actor;
    private Session session;

        @BeforeEach
        void setUp() {
            actor = new User();
            actor.setId(1L);
            actor.setUsername("seller");
            actor.setPasswordHash("hash");

            // Tránh NPE trong assertNoCollision khi mock chưa được thiết lập
            when(sessionRepository.findAllByStatusInAndPlannedEndAtAfter(any(), any())).thenReturn(List.of());

            session = new Session();
            session.setId(10L);
            session.setStatus(SessionStatus.Waiting);
            session.setScheduledDate(LocalDate.of(2026, 11, 10));
            session.setScheduledTime(LocalTime.of(8, 0));
            session.setPlannedEndAt(LocalDateTime.of(2026, 11, 10, 17, 0));
            session.setIsInstant(false);
            session.setNextTicketNo(1);
        }


    // ==================== createSession ====================

    @Test
    void createSession_instant_shouldCreateInProgressSession() throws Exception {
        CreateSessionRequest req = new CreateSessionRequest(null, null, null, null, true);
        when(sessionRepository .existsByStatus(SessionStatus.InProgress)).thenReturn(false);
        when(sessionRepository.saveAndFlush(any(Session.class))).thenAnswer(inv -> {
            Session s = inv.getArgument(0);
            s.setId(1L);
            return s;
        });

        SessionDetailResponse res = sessionService.createSession(req, actor);

        assertNotNull(res);
        verify(sessionRepository).saveAndFlush(any(Session.class));
    }

    @Test
    void createSession_instant_whenAlreadyInProgress_shouldThrowSessionAlreadyInProgress() throws Exception {
        CreateSessionRequest req = new CreateSessionRequest(null, null, null, null, true);
        when(sessionRepository.existsByStatus(SessionStatus.InProgress)).thenReturn(true);

        assertThrows(SessionAlreadyInProgress.class, () -> sessionService.createSession(req, actor));
        verify(sessionRepository, never()).saveAndFlush(any());
    }

    @Test
    void createSession_scheduled_inPast_shouldThrowValidationException() throws Exception {
        LocalDateTime past = LocalDateTime.now().minusMinutes(10);
        CreateSessionRequest req = new CreateSessionRequest(
                past.toLocalDate(), past.toLocalTime(),
                past.plusHours(2).toLocalDate(), past.plusHours(2).toLocalTime(),
                false);
        when(sessionRepository.existsByStatus(SessionStatus.InProgress)).thenReturn(false);

        assertThrows(ValidationException.class, () -> sessionService.createSession(req, actor));
    }

    @Test
    void createSession_scheduled_endBeforeStart_shouldThrowValidationException() throws Exception {
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = start.minusHours(1);
        CreateSessionRequest req = new CreateSessionRequest(
                start.toLocalDate(), start.toLocalTime(),
                end.toLocalDate(), end.toLocalTime(),
                false);
        when(sessionRepository.existsByStatus(SessionStatus.InProgress)).thenReturn(false);

        assertThrows(ValidationException.class, () -> sessionService.createSession(req, actor));
    }

    @Test
    void createSession_scheduled_valid_shouldCreateWaitingSession() throws Exception {
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = start.plusHours(2);
        CreateSessionRequest req = new CreateSessionRequest(
                start.toLocalDate(), start.toLocalTime(),
                end.toLocalDate(), end.toLocalTime(),
                false);
        when(sessionRepository.existsByStatus(SessionStatus.InProgress)).thenReturn(false);
        when(sessionRepository.saveAndFlush(any(Session.class))).thenAnswer(inv -> {
            Session s = inv.getArgument(0);
            s.setId(1L);
            return s;
        });

        SessionDetailResponse res = sessionService.createSession(req, actor);

        assertNotNull(res);
        verify(sessionRepository).saveAndFlush(any(Session.class));
    }

    // ==================== startSession ====================

    @Test
    void startSession_withSessionId_waiting_shouldStart() throws Exception {
        StartSessionRequest req = new StartSessionRequest(10L, List.of("xe-01"), null);
        when(sessionRepository.findById(10L)).thenReturn(Optional.of(session));
        when(sessionRepository.existsByStatus(SessionStatus.InProgress)).thenReturn(false);
        when(sessionRepository.saveAndFlush(any(Session.class))).thenReturn(session);
        when(vehicleRepository.lockBySlug("xe-01")).thenReturn(Optional.of(new Vehicle("xe-01")));
        when(sessionVehicleRepository.findBySessionIdAndVehicleId(10L, 1L)).thenReturn(Optional.empty());
        when(sessionVehicleRepository.save(any(SessionVehicle.class))).thenAnswer(inv -> inv.getArgument(0));

        SessionDetailResponse res = sessionService.startSession(req, actor);

        assertNotNull(res);
        assertEquals(SessionStatus.InProgress, session.getStatus());
    }

    @Test
    void startSession_withSessionId_alreadyInProgress_shouldThrow() throws Exception {
        session.setStatus(SessionStatus.InProgress);
        StartSessionRequest req = new StartSessionRequest(10L, null, null);
        when(sessionRepository.findById(10L)).thenReturn(Optional.of(session));

        assertThrows(AbstractException.class, () -> sessionService.startSession(req, actor));
    }

    @Test
    void startSession_withoutSessionId_shouldCreateInstant() throws Exception {
        StartSessionRequest req = new StartSessionRequest(null, List.of("xe-01"), null);
        when(sessionRepository.existsByStatus(SessionStatus.InProgress)).thenReturn(false);
        when(sessionRepository.saveAndFlush(any(Session.class))).thenAnswer(inv -> {
            Session s = inv.getArgument(0);
            s.setId(1L);
            s.setStatus(SessionStatus.InProgress);
            return s;
        });
        when(vehicleRepository.lockBySlug("xe-01")).thenReturn(Optional.of(new Vehicle("xe-01")));
        when(sessionVehicleRepository.findBySessionIdAndVehicleId(1L, 1L)).thenReturn(Optional.empty());
        when(sessionVehicleRepository.save(any(SessionVehicle.class))).thenAnswer(inv -> inv.getArgument(0));

        SessionDetailResponse res = sessionService.startSession(req, actor);

        assertNotNull(res);
        verify(sessionRepository).saveAndFlush(any(Session.class));
    }

    @Test
    void startSession_sessionNotFound_shouldThrowSessionNotFound() throws Exception {
        StartSessionRequest req = new StartSessionRequest(999L, null, null);
        when(sessionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(SessionNotFound.class, () -> sessionService.startSession(req, actor));
    }

    // ==================== endCurrentSession ====================

    @Test
    void endCurrentSession_withOpenTickets_shouldThrowSessionHasOpenTickets() throws Exception {
        when(sessionRepository.lockByStatus(SessionStatus.InProgress)).thenReturn(List.of(session));
        when(ticketRepository.existsBySession_IdAndStatusIn(10L,
                List.of(TicketStatus.Waiting, TicketStatus.Inused, TicketStatus.Overdue))).thenReturn(true);

        assertThrows(SessionHasOpenTickets.class, () -> sessionService.endCurrentSession(actor));
    }

    @Test
    void endCurrentSession_vehicleInUse_shouldThrowVehicleInUse() throws Exception {
        when(sessionRepository.lockByStatus(SessionStatus.InProgress)).thenReturn(List.of(session));
        when(ticketRepository.existsBySession_IdAndStatusIn(10L,
                List.of(TicketStatus.Waiting, TicketStatus.Inused, TicketStatus.Overdue))).thenReturn(false);

        Vehicle vehicle = new Vehicle("xe-01");
        vehicle.setId(1L);
        vehicle.setStatus(VehicleStatus.Inused);
        SessionVehicle sv = new SessionVehicle();
        sv.setVehicle(vehicle);
        when(sessionVehicleRepository.findBySessionIdAndRemovedAtIsNull(10L)).thenReturn(List.of(sv));

        assertThrows(VehicleInUse.class, () -> sessionService.endCurrentSession(actor));
    }

    @Test
    void endCurrentSession_success_shouldEndSessionAndSetVehiclesOffline() throws Exception {
        when(sessionRepository.lockByStatus(SessionStatus.InProgress)).thenReturn(List.of(session));
        when(ticketRepository.existsBySession_IdAndStatusIn(10L,
                List.of(TicketStatus.Waiting, TicketStatus.Inused, TicketStatus.Overdue))).thenReturn(false);

        Vehicle vehicle = new Vehicle("xe-01");
        vehicle.setId(1L);
        vehicle.setStatus(VehicleStatus.Online);
        SessionVehicle sv = new SessionVehicle();
        sv.setVehicle(vehicle);
        when(sessionVehicleRepository.findBySessionIdAndRemovedAtIsNull(10L)).thenReturn(List.of(sv));
        when(sessionRepository.save(any(Session.class))).thenReturn(session);

        SessionDetailResponse res = sessionService.endCurrentSession(actor);

        assertNotNull(res);
        assertEquals(SessionStatus.Ended, session.getStatus());
        assertEquals(VehicleStatus.Offline, vehicle.getStatus());
    }

    // ==================== cancelSession ====================

    @Test
    void cancelSession_waiting_shouldCancel() throws Exception {
        when(sessionRepository.findById(10L)).thenReturn(Optional.of(session));
        when(sessionRepository.saveAndFlush(any(Session.class))).thenReturn(session);

        SessionDetailResponse res = sessionService.cancelSession(10L, actor);

        assertNotNull(res);
        assertEquals(SessionStatus.Canceled, session.getStatus());
    }

    @Test
    void cancelSession_inProgress_shouldThrow() throws Exception {
        session.setStatus(SessionStatus.InProgress);
        when(sessionRepository.findById(10L)).thenReturn(Optional.of(session));

        assertThrows(AbstractException.class, () -> sessionService.cancelSession(10L, actor));
    }

    @Test
    void cancelSession_ended_shouldThrow() throws Exception {
        session.setStatus(SessionStatus.Ended);
        when(sessionRepository.findById(10L)).thenReturn(Optional.of(session));

        assertThrows(AbstractException.class, () -> sessionService.cancelSession(10L, actor));
    }

    @Test
    void cancelSession_canceled_shouldThrow() throws Exception {
        session.setStatus(SessionStatus.Canceled);
        when(sessionRepository.findById(10L)).thenReturn(Optional.of(session));

        assertThrows(AbstractException.class, () -> sessionService.cancelSession(10L, actor));
    }

    @Test
    void cancelSession_notFound_shouldThrowSessionNotFound() throws Exception {
        when(sessionRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(SessionNotFound.class, () -> sessionService.cancelSession(10L, actor));
    }

    // ==================== addVehiclesToCurrentSession ====================

    @Test
    void addVehiclesToCurrentSession_success() throws Exception {
        when(sessionRepository.lockByStatus(SessionStatus.InProgress)).thenReturn(List.of(session));
        when(vehicleRepository.lockBySlug("xe-01")).thenReturn(Optional.of(new Vehicle("xe-01")));
        when(sessionVehicleRepository.findBySessionIdAndVehicleId(10L, 1L)).thenReturn(Optional.empty());
        when(sessionVehicleRepository.save(any(SessionVehicle.class))).thenAnswer(inv -> inv.getArgument(0));

        AddSessionVehiclesRequest req = new AddSessionVehiclesRequest(List.of("xe-01"));

        SessionDetailResponse res = sessionService.addVehiclesToCurrentSession(req, actor);

        assertNotNull(res);
    }

    @Test
    void addVehiclesToCurrentSession_emptySlugs_shouldThrow() throws Exception {
        when(sessionRepository.lockByStatus(SessionStatus.InProgress)).thenReturn(List.of(session));

        AddSessionVehiclesRequest req = new AddSessionVehiclesRequest(List.of());
        assertThrows(ValidationException.class, () -> sessionService.addVehiclesToCurrentSession(req, actor));
    }

    // ==================== removeVehicleFromCurrentSession ====================

    @Test
    void removeVehicleFromCurrentSession_success() throws Exception {
        when(sessionRepository.lockByStatus(SessionStatus.InProgress)).thenReturn(List.of(session));

        Vehicle vehicle = new Vehicle("xe-01");
        vehicle.setId(1L);
        vehicle.setStatus(VehicleStatus.Online);
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        SessionVehicle link = new SessionVehicle();
        link.setVehicle(vehicle);
        when(sessionVehicleRepository.findBySessionIdAndVehicleId(10L, 1L)).thenReturn(Optional.of(link));
        when(vehicleRepository.lockBySlug("xe-01")).thenReturn(Optional.of(vehicle));
        when(sessionVehicleRepository.save(any(SessionVehicle.class))).thenAnswer(inv -> inv.getArgument(0));

        SessionDetailResponse res = sessionService.removeVehicleFromCurrentSession(1L, actor);

        assertNotNull(res);
    }

    @Test
    void removeVehicleFromCurrentSession_vehicleInUse_shouldThrow() throws Exception {
        when(sessionRepository.lockByStatus(SessionStatus.InProgress)).thenReturn(List.of(session));

        Vehicle vehicle = new Vehicle("xe-01");
        vehicle.setId(1L);
        vehicle.setStatus(VehicleStatus.Inused);
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        SessionVehicle link = new SessionVehicle();
        link.setVehicle(vehicle);
        when(sessionVehicleRepository.findBySessionIdAndVehicleId(10L, 1L)).thenReturn(Optional.of(link));
        when(vehicleRepository.lockBySlug("xe-01")).thenReturn(Optional.of(vehicle));

        assertThrows(VehicleInUse.class, () -> sessionService.removeVehicleFromCurrentSession(1L, actor));
    }

    // ==================== getSessionDetail ====================

    @Test
    void getSessionDetail_success() throws Exception {
        when(sessionRepository.findById(10L)).thenReturn(Optional.of(session));
        when(sessionVehicleRepository.findBySessionIdAndRemovedAtIsNull(10L)).thenReturn(List.of());

        SessionDetailResponse res = sessionService.getSessionDetail(10L);

        assertNotNull(res);
    }

    @Test
    void getSessionDetail_notFound_shouldThrowSessionNotFound() throws Exception {
        when(sessionRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(SessionNotFound.class, () -> sessionService.getSessionDetail(10L));
    }

    // ==================== updateSession ====================

    @Test
    void updateSession_waiting_shouldUpdate() throws Exception {
        when(sessionRepository.findById(10L)).thenReturn(Optional.of(session));
        when(sessionRepository.save(any(Session.class))).thenReturn(session);

        UpdateSessionRequest req = new UpdateSessionRequest(
                LocalDateTime.of(2026, 11, 10, 18, 0),
                LocalDate.of(2026, 11, 10), LocalTime.of(9, 0), false);

        SessionDetailResponse res = sessionService.updateSession(10L, req, actor);

        assertNotNull(res);
    }

    @Test
    void updateSession_notWaiting_shouldThrowSessionNotEditable() throws Exception {
        session.setStatus(SessionStatus.InProgress);
        when(sessionRepository.findById(10L)).thenReturn(Optional.of(session));

        UpdateSessionRequest req = new UpdateSessionRequest(
                LocalDateTime.of(2026, 11, 10, 18, 0),
                LocalDate.of(2026, 11, 10), LocalTime.of(9, 0), false);

        assertThrows(SessionNotEditable.class, () -> sessionService.updateSession(10L, req, actor));
    }

    @Test
    void updateSession_instantTrue_shouldThrowValidationException() throws Exception {
        when(sessionRepository.findById(10L)).thenReturn(Optional.of(session));

        UpdateSessionRequest req = new UpdateSessionRequest(
                LocalDateTime.of(2026, 11, 10, 18, 0),
                LocalDate.of(2026, 11, 10), LocalTime.of(9, 0), true);

        assertThrows(ValidationException.class, () -> sessionService.updateSession(10L, req, actor));
    }
}
