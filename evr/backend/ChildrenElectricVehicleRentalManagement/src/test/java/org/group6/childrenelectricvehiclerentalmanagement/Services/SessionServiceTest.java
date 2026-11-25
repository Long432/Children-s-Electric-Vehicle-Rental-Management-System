package org.group6.childrenelectricvehiclerentalmanagement.Services;

import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Session.CreateSessionRequest;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Session.SessionDetailResponse;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Session.SessionResponse;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Session.SessionListQuery;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Session.StartSessionRequest;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Session.UpdateSessionRequest;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.PageResponse;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.SessionStatus;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.VehicleStatus;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.ConflictException;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.NoActiveSessionException;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.SessionAlreadyInProgressException;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.SessionNotExits;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.ValidationException;
import org.group6.childrenelectricvehiclerentalmanagement.Models.Session;
import org.group6.childrenelectricvehiclerentalmanagement.Models.SessionVehicle;
import org.group6.childrenelectricvehiclerentalmanagement.Models.Vehicle;
import org.group6.childrenelectricvehiclerentalmanagement.Repositories.SessionVehicleRepository;
import org.group6.childrenelectricvehiclerentalmanagement.Repositories.SesssionRepository;
import org.group6.childrenelectricvehiclerentalmanagement.Repositories.TicketRepository;
import org.group6.childrenelectricvehiclerentalmanagement.Repositories.UserRepository;
import org.group6.childrenelectricvehiclerentalmanagement.Repositories.VehicleRepository;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.BusinessTime;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.Security.AuthenticatedUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SessionServiceTest {

    private static final Instant NOW = Instant.parse("2026-11-05T10:00:00Z");

    private final SesssionRepository sessionRepository = mock(SesssionRepository.class);
    private final VehicleRepository vehicleRepository = mock(VehicleRepository.class);
    private final SessionVehicleRepository sessionVehicleRepository = mock(SessionVehicleRepository.class);
    private final TicketRepository ticketRepository = mock(TicketRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final AuditLogService auditLogService = mock(AuditLogService.class);
    private final BusinessTime businessTime = new BusinessTime(Clock.fixed(NOW, ZoneOffset.UTC), "Asia/Ho_Chi_Minh");
    private final AuthenticatedUser actor = new AuthenticatedUser(1L, "banhang");
    private SessionService service;

    @BeforeEach
    void setUp() {
        service = new SessionService(sessionRepository, vehicleRepository, sessionVehicleRepository,
                ticketRepository, userRepository, auditLogService, businessTime);
        when(sessionRepository.saveAndFlush(any(Session.class))).thenAnswer(call -> assignId(call.getArgument(0)));
        when(sessionRepository.save(any(Session.class))).thenAnswer(call -> assignId(call.getArgument(0)));
    }

    private SessionService serviceAt(String instant) {
        BusinessTime moment = new BusinessTime(Clock.fixed(Instant.parse(instant), ZoneOffset.UTC), "Asia/Ho_Chi_Minh");
        return new SessionService(sessionRepository, vehicleRepository, sessionVehicleRepository,
                ticketRepository, userRepository, auditLogService, moment);
    }

    private Session assignId(Session session) {
        if (session.getId() == null) {
            ReflectionTestUtils.setField(session, "id", 1L);
        }
        return session;
    }

    private Session session(long id, SessionStatus status) {
        Session session = new Session();
        session.setId(id);
        session.setStatus(status);
        session.setScheduledDate(LocalDate.of(2026, 11, 5));
        session.setScheduledTime(LocalTime.of(9, 0));
        session.setIsInstant(false);
        session.setNextTicketNo(1);
        session.setCreatedAt(LocalDateTime.of(2026, 11, 4, 1, 0));
        session.setUpdatedAt(LocalDateTime.of(2026, 11, 4, 1, 0));
        return session;
    }

    private Vehicle vehicle(long id, String slug, VehicleStatus status) {
        Vehicle vehicle = new Vehicle(slug);
        ReflectionTestUtils.setField(vehicle, "id", id);
        if (status != VehicleStatus.Offline) {
            vehicle.setStatus(status);
        }
        return vehicle;
    }

    @Test
    void createRejectsPastDate() {
        ValidationException error = assertThrows(ValidationException.class,
                () -> service.create(actor, new CreateSessionRequest("2026-11-04", "09:00")));
        assertEquals("date", error.getField());
    }

    @Test
    void createStoresWaitingSession() throws Exception {
        SessionResponse response = service.create(actor, new CreateSessionRequest("2026-11-06", "08:30"));
        assertEquals(SessionStatus.Waiting, response.status());
        assertEquals("2026-11-06", response.scheduled_date());
        assertEquals("08:30", response.scheduled_time());
        assertEquals(1, response.next_ticket_no());
        assertFalse(response.is_instant());
    }

    @Test
    void startWithoutSessionIdCreatesInstantSessionEndingAtCloseTime() throws Exception {
        Vehicle vehicle = vehicle(3L, "xe-gau-01", VehicleStatus.Offline);
        when(vehicleRepository.findActiveBySlugsForUpdate(List.of("xe-gau-01"))).thenReturn(List.of(vehicle));
        SessionDetailResponse response = service.start(actor, new StartSessionRequest(List.of("xe-gau-01"), null, null));
        assertEquals(SessionStatus.InProgress, response.status());
        assertTrue(response.is_instant());
        assertEquals("2026-11-05", response.scheduled_date());
        assertEquals("17:00", response.scheduled_time());
        assertEquals("2026-11-05T16:00:00Z", response.planned_end_at());
        assertEquals("2026-11-05T10:00:00Z", response.started_at());
        assertEquals(VehicleStatus.Online, vehicle.getStatus());
        verify(sessionVehicleRepository).save(any(SessionVehicle.class));
    }

    @Test
    void startWaitingSessionUsesGivenEndTime() throws Exception {
        Session waiting = session(5L, SessionStatus.Waiting);
        when(sessionRepository.findByIdForUpdate(5L)).thenReturn(Optional.of(waiting));
        when(vehicleRepository.findActiveBySlugsForUpdate(List.of("xe-gau-01")))
                .thenReturn(List.of(vehicle(3L, "xe-gau-01", VehicleStatus.Online)));
        SessionDetailResponse response = service.start(actor, new StartSessionRequest(List.of("xe-gau-01"), 5L, "22:30"));
        assertEquals(SessionStatus.InProgress, response.status());
        assertFalse(response.is_instant());
        assertEquals("2026-11-05T15:30:00Z", response.planned_end_at());
    }

    @Test
    void startRejectsSessionThatIsNotWaiting() {
        when(sessionRepository.findByIdForUpdate(5L)).thenReturn(Optional.of(session(5L, SessionStatus.Ended)));
        when(vehicleRepository.findActiveBySlugsForUpdate(List.of("xe-gau-01")))
                .thenReturn(List.of(vehicle(3L, "xe-gau-01", VehicleStatus.Offline)));
        ConflictException error = assertThrows(ConflictException.class,
                () -> service.start(actor, new StartSessionRequest(List.of("xe-gau-01"), 5L, null)));
        assertEquals("SESSION_NOT_STARTABLE", error.getCode());
    }

    @Test
    void startRejectsWhenAnotherSessionIsInProgress() {
        when(sessionRepository.existsByStatus(SessionStatus.InProgress)).thenReturn(true);
        assertThrows(SessionAlreadyInProgressException.class,
                () -> service.start(actor, new StartSessionRequest(List.of("xe-gau-01"), null, null)));
        verify(vehicleRepository, never()).findActiveBySlugsForUpdate(anyCollection());
    }

    @Test
    void startRejectsUnknownVehicle() {
        when(vehicleRepository.findActiveBySlugsForUpdate(List.of("xe-gau-01"))).thenReturn(List.of());
        ValidationException error = assertThrows(ValidationException.class,
                () -> service.start(actor, new StartSessionRequest(List.of("xe-gau-01"), null, null)));
        assertEquals("vehicle_slugs", error.getField());
    }

    @Test
    void startRejectsRepairingVehicle() {
        when(vehicleRepository.findActiveBySlugsForUpdate(List.of("xe-gau-01")))
                .thenReturn(List.of(vehicle(3L, "xe-gau-01", VehicleStatus.Repairing)));
        ConflictException error = assertThrows(ConflictException.class,
                () -> service.start(actor, new StartSessionRequest(List.of("xe-gau-01"), null, null)));
        assertEquals("VEHICLE_NOT_AVAILABLE", error.getCode());
    }

    @Test
    void endRequiresActiveSession() {
        when(sessionRepository.findByStatusForUpdate(SessionStatus.InProgress)).thenReturn(Optional.empty());
        assertThrows(NoActiveSessionException.class, () -> service.end(actor));
    }

    @Test
    void endIsBlockedWhileTicketsAreOpen() {
        Session active = session(1L, SessionStatus.InProgress);
        when(sessionRepository.findByStatusForUpdate(SessionStatus.InProgress)).thenReturn(Optional.of(active));
        when(ticketRepository.countBySessionIdAndStatusIn(eq(1L), anyCollection())).thenReturn(2L);
        ConflictException error = assertThrows(ConflictException.class, () -> service.end(actor));
        assertEquals("SESSION_HAS_OPEN_TICKETS", error.getCode());
        assertEquals(SessionStatus.InProgress, active.getStatus());
    }

    @Test
    void endReleasesOnlineVehiclesAndKeepsRepairingOnes() throws Exception {
        Session active = session(1L, SessionStatus.InProgress);
        Vehicle online = vehicle(3L, "xe-gau-01", VehicleStatus.Online);
        Vehicle repairing = vehicle(4L, "xe-gau-02", VehicleStatus.Repairing);
        when(sessionRepository.findByStatusForUpdate(SessionStatus.InProgress)).thenReturn(Optional.of(active));
        when(ticketRepository.countBySessionIdAndStatusIn(eq(1L), anyCollection())).thenReturn(0L);
        when(vehicleRepository.findActiveBySessionIdForUpdate(1L, VehicleStatus.Deleted)).thenReturn(List.of(online, repairing));
        SessionResponse response = service.end(actor);
        assertEquals(SessionStatus.Ended, response.status());
        assertEquals("2026-11-05T10:00:00Z", response.ended_at());
        assertEquals(VehicleStatus.Offline, online.getStatus());
        assertEquals(VehicleStatus.Repairing, repairing.getStatus());
    }

    @Test
    void cancelOnlyWorksOnWaitingSessions() throws Exception {
        Session waiting = session(5L, SessionStatus.Waiting);
        when(sessionRepository.findByIdForUpdate(5L)).thenReturn(Optional.of(waiting));
        assertEquals(SessionStatus.Canceled, service.cancel(actor, 5L).status());

        when(sessionRepository.findByIdForUpdate(6L)).thenReturn(Optional.of(session(6L, SessionStatus.InProgress)));
        ConflictException error = assertThrows(ConflictException.class, () -> service.cancel(actor, 6L));
        assertEquals("SESSION_NOT_EDITABLE", error.getCode());
    }

    @Test
    void createRejectsTimeAlreadyPassedToday() {
        ValidationException error = assertThrows(ValidationException.class,
                () -> service.create(actor, new CreateSessionRequest("2026-11-05", "16:59")));
        assertEquals("time", error.getField());
        verify(sessionRepository, never()).save(any(Session.class));
    }

    @Test
    void createAcceptsTheCurrentMinuteToday() throws Exception {
        SessionResponse response = service.create(actor, new CreateSessionRequest("2026-11-05", "17:00"));
        assertEquals("2026-11-05T10:00:00Z", response.scheduled_at());
    }

    @Test
    void createAcceptsAnEarlierClockTimeOnAFutureDate() throws Exception {
        SessionResponse response = service.create(actor, new CreateSessionRequest("2026-11-06", "08:30"));
        assertEquals("2026-11-06T01:30:00Z", response.scheduled_at());
        assertFalse(response.past_planned_end());
        assertNull(response.started_at());
        assertNull(response.planned_end_at());
    }

    @Test
    void instantSessionReportsEveryMomentAsIsoUtc() throws Exception {
        Vehicle vehicle = vehicle(3L, "xe-gau-01", VehicleStatus.Offline);
        when(vehicleRepository.findActiveBySlugsForUpdate(List.of("xe-gau-01"))).thenReturn(List.of(vehicle));
        SessionDetailResponse response = service.start(actor, new StartSessionRequest(List.of("xe-gau-01"), null, null));
        assertEquals("2026-11-05T10:00:00Z", response.scheduled_at());
        assertEquals("2026-11-05T10:00:00Z", response.started_at());
        assertEquals("2026-11-05T16:00:00Z", response.planned_end_at());
        assertTrue(response.created_at().endsWith("Z"));
        assertTrue(response.updated_at().endsWith("Z"));
        assertFalse(response.past_planned_end());
    }

    @Test
    void startRejectsEndTimeThatHasAlreadyPassedToday() {
        ValidationException error = assertThrows(ValidationException.class,
                () -> service.start(actor, new StartSessionRequest(List.of("xe-gau-01"), null, "16:00")));
        assertEquals("end_time", error.getField());
    }

    @Test
    void startRejectsEndTimeAfterCloseTime() {
        ValidationException error = assertThrows(ValidationException.class,
                () -> service.start(actor, new StartSessionRequest(List.of("xe-gau-01"), null, "23:30")));
        assertEquals("end_time", error.getField());
    }

    @Test
    void startAcceptsEndTimeExactlyAtCloseTime() throws Exception {
        Vehicle vehicle = vehicle(3L, "xe-gau-01", VehicleStatus.Offline);
        when(vehicleRepository.findActiveBySlugsForUpdate(List.of("xe-gau-01"))).thenReturn(List.of(vehicle));
        SessionDetailResponse response = service.start(actor, new StartSessionRequest(List.of("xe-gau-01"), null, "23:00"));
        assertEquals("2026-11-05T16:00:00Z", response.planned_end_at());
    }

    @Test
    void startRejectsBeforeOpeningTime() {
        SessionService early = serviceAt("2026-11-04T22:30:00Z");
        ConflictException error = assertThrows(ConflictException.class,
                () -> early.start(actor, new StartSessionRequest(List.of("xe-gau-01"), null, null)));
        assertEquals("OUTSIDE_BUSINESS_HOURS", error.getCode());
        verify(vehicleRepository, never()).findActiveBySlugsForUpdate(anyCollection());
    }

    @Test
    void startRejectsAtOrAfterCloseTime() {
        SessionService late = serviceAt("2026-11-05T16:00:00Z");
        ConflictException error = assertThrows(ConflictException.class,
                () -> late.start(actor, new StartSessionRequest(List.of("xe-gau-01"), null, null)));
        assertEquals("OUTSIDE_BUSINESS_HOURS", error.getCode());
    }

    @Test
    void startAcceptsAtOpeningTime() throws Exception {
        SessionService opening = serviceAt("2026-11-04T23:00:00Z");
        Vehicle vehicle = vehicle(3L, "xe-gau-01", VehicleStatus.Offline);
        when(vehicleRepository.findActiveBySlugsForUpdate(List.of("xe-gau-01"))).thenReturn(List.of(vehicle));
        SessionDetailResponse response = opening.start(actor, new StartSessionRequest(List.of("xe-gau-01"), null, null));
        assertEquals(SessionStatus.InProgress, response.status());
    }

    @Test
    void createRejectsTimeBeforeOpening() {
        ValidationException error = assertThrows(ValidationException.class,
                () -> service.create(actor, new CreateSessionRequest("2026-11-06", "05:59")));
        assertEquals("time", error.getField());
        verify(sessionRepository, never()).save(any(Session.class));
    }

    @Test
    void createRejectsTimeAtCloseTime() {
        ValidationException error = assertThrows(ValidationException.class,
                () -> service.create(actor, new CreateSessionRequest("2026-11-06", "23:00")));
        assertEquals("time", error.getField());
    }

    @Test
    void createAcceptsOpeningTime() throws Exception {
        SessionResponse response = service.create(actor, new CreateSessionRequest("2026-11-06", "06:00"));
        assertEquals("06:00", response.scheduled_time());
    }

    @Test
    void updateRejectsTimeOutsideBusinessHours() {
        when(sessionRepository.findByIdForUpdate(5L)).thenReturn(Optional.of(session(5L, SessionStatus.Waiting)));
        ValidationException error = assertThrows(ValidationException.class,
                () -> service.update(actor, 5L, new UpdateSessionRequest("2026-11-06", "05:30")));
        assertEquals("time", error.getField());
    }

    @Test
    void startingWaitingSessionKeepsItsScheduledMomentAndIsNotInstant() throws Exception {
        Session waiting = session(5L, SessionStatus.Waiting);
        when(sessionRepository.findByIdForUpdate(5L)).thenReturn(Optional.of(waiting));
        when(vehicleRepository.findActiveBySlugsForUpdate(List.of("xe-gau-01")))
                .thenReturn(List.of(vehicle(3L, "xe-gau-01", VehicleStatus.Online)));
        SessionDetailResponse response = service.start(actor, new StartSessionRequest(List.of("xe-gau-01"), 5L, null));
        assertFalse(response.is_instant());
        assertEquals("09:00", response.scheduled_time());
        assertEquals("2026-11-05T02:00:00Z", response.scheduled_at());
        assertEquals("2026-11-05T10:00:00Z", response.started_at());
    }

    @Test
    void startRejectsCanceledSession() {
        when(sessionRepository.findByIdForUpdate(5L)).thenReturn(Optional.of(session(5L, SessionStatus.Canceled)));
        when(vehicleRepository.findActiveBySlugsForUpdate(List.of("xe-gau-01")))
                .thenReturn(List.of(vehicle(3L, "xe-gau-01", VehicleStatus.Offline)));
        ConflictException error = assertThrows(ConflictException.class,
                () -> service.start(actor, new StartSessionRequest(List.of("xe-gau-01"), 5L, null)));
        assertEquals("SESSION_NOT_STARTABLE", error.getCode());
    }

    @Test
    void startRejectsUnknownWaitingSession() {
        when(sessionRepository.findByIdForUpdate(9L)).thenReturn(Optional.empty());
        assertThrows(SessionNotExits.class,
                () -> service.start(actor, new StartSessionRequest(List.of("xe-gau-01"), 9L, null)));
    }

    @Test
    void currentIsFlaggedOncePlannedEndHasPassed() throws Exception {
        Session active = session(1L, SessionStatus.InProgress);
        active.setStartedAt(LocalDateTime.of(2026, 11, 5, 3, 0));
        active.setPlannedEndAt(LocalDateTime.of(2026, 11, 5, 9, 59));
        when(sessionRepository.findFirstByStatus(SessionStatus.InProgress)).thenReturn(Optional.of(active));
        assertTrue(service.current().past_planned_end());
    }

    @Test
    void currentIsFlaggedExactlyAtPlannedEnd() throws Exception {
        Session active = session(1L, SessionStatus.InProgress);
        active.setStartedAt(LocalDateTime.of(2026, 11, 5, 3, 0));
        active.setPlannedEndAt(LocalDateTime.of(2026, 11, 5, 10, 0));
        when(sessionRepository.findFirstByStatus(SessionStatus.InProgress)).thenReturn(Optional.of(active));
        assertTrue(service.current().past_planned_end());
    }

    @Test
    void currentIsNotFlaggedBeforePlannedEnd() throws Exception {
        Session active = session(1L, SessionStatus.InProgress);
        active.setStartedAt(LocalDateTime.of(2026, 11, 5, 3, 0));
        active.setPlannedEndAt(LocalDateTime.of(2026, 11, 5, 10, 1));
        when(sessionRepository.findFirstByStatus(SessionStatus.InProgress)).thenReturn(Optional.of(active));
        assertFalse(service.current().past_planned_end());
    }

    @Test
    void endedSessionIsNeverFlaggedAsPastPlannedEnd() throws Exception {
        Session ended = session(2L, SessionStatus.Ended);
        ended.setStartedAt(LocalDateTime.of(2026, 11, 4, 3, 0));
        ended.setPlannedEndAt(LocalDateTime.of(2026, 11, 4, 17, 0));
        ended.setEndedAt(LocalDateTime.of(2026, 11, 4, 16, 0));
        when(sessionRepository.findById(2L)).thenReturn(Optional.of(ended));
        assertFalse(service.get(2L).past_planned_end());
    }

    @Test
    void listOfWaitingSessionsCarriesScheduledMoments() throws Exception {
        Session waiting = session(5L, SessionStatus.Waiting);
        when(sessionRepository.findByStatus(eq(SessionStatus.Waiting), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(waiting)));
        PageResponse<SessionResponse> page = service.list(new SessionListQuery("Waiting", null, null));
        assertEquals(1, page.items().size());
        assertEquals("2026-11-05T02:00:00Z", page.items().get(0).scheduled_at());
        assertFalse(page.items().get(0).is_instant());
    }

    @Test
    void updateChangesDateAndTimeOfAWaitingSession() throws Exception {
        Session waiting = session(5L, SessionStatus.Waiting);
        when(sessionRepository.findByIdForUpdate(5L)).thenReturn(Optional.of(waiting));
        SessionResponse response = service.update(actor, 5L, new UpdateSessionRequest("2026-11-07", "08:00"));
        assertEquals("2026-11-07", response.scheduled_date());
        assertEquals("08:00", response.scheduled_time());
        assertEquals("2026-11-07T01:00:00Z", response.scheduled_at());
    }

    @Test
    void updateWithOnlyDateKeepsTheStoredTime() throws Exception {
        Session waiting = session(5L, SessionStatus.Waiting);
        when(sessionRepository.findByIdForUpdate(5L)).thenReturn(Optional.of(waiting));
        SessionResponse response = service.update(actor, 5L, new UpdateSessionRequest("2026-11-06", null));
        assertEquals("2026-11-06", response.scheduled_date());
        assertEquals("09:00", response.scheduled_time());
    }

    @Test
    void updateWithOnlyTimeIsCheckedAgainstTheStoredDate() throws Exception {
        Session waiting = session(5L, SessionStatus.Waiting);
        when(sessionRepository.findByIdForUpdate(5L)).thenReturn(Optional.of(waiting));
        ValidationException error = assertThrows(ValidationException.class,
                () -> service.update(actor, 5L, new UpdateSessionRequest(null, "16:00")));
        assertEquals("time", error.getField());
        assertEquals(LocalTime.of(9, 0), waiting.getScheduledTime());
        SessionResponse response = service.update(actor, 5L, new UpdateSessionRequest(null, "20:00"));
        assertEquals("2026-11-05", response.scheduled_date());
        assertEquals("20:00", response.scheduled_time());
    }

    @Test
    void updateRejectsPastDateAndLeavesTheSessionUntouched() {
        Session waiting = session(5L, SessionStatus.Waiting);
        when(sessionRepository.findByIdForUpdate(5L)).thenReturn(Optional.of(waiting));
        ValidationException error = assertThrows(ValidationException.class,
                () -> service.update(actor, 5L, new UpdateSessionRequest("2026-11-04", null)));
        assertEquals("date", error.getField());
        assertEquals(LocalDate.of(2026, 11, 5), waiting.getScheduledDate());
        verify(sessionRepository, never()).save(any(Session.class));
    }

    @Test
    void updateRequiresADateOrATime() {
        ValidationException error = assertThrows(ValidationException.class,
                () -> service.update(actor, 5L, new UpdateSessionRequest(null, null)));
        assertEquals("body", error.getField());
    }

    @Test
    void updateRejectsSessionThatIsNotWaiting() {
        when(sessionRepository.findByIdForUpdate(6L)).thenReturn(Optional.of(session(6L, SessionStatus.InProgress)));
        ConflictException error = assertThrows(ConflictException.class,
                () -> service.update(actor, 6L, new UpdateSessionRequest("2026-11-06", "08:00")));
        assertEquals("SESSION_NOT_EDITABLE", error.getCode());
    }

    @Test
    void updateRejectsUnknownSession() {
        when(sessionRepository.findByIdForUpdate(9L)).thenReturn(Optional.empty());
        assertThrows(SessionNotExits.class,
                () -> service.update(actor, 9L, new UpdateSessionRequest("2026-11-06", "08:00")));
    }

    @Test
    void cancelRejectsAnAlreadyCanceledSession() {
        when(sessionRepository.findByIdForUpdate(5L)).thenReturn(Optional.of(session(5L, SessionStatus.Canceled)));
        ConflictException error = assertThrows(ConflictException.class, () -> service.cancel(actor, 5L));
        assertEquals("SESSION_NOT_EDITABLE", error.getCode());
    }

    @Test
    void cancelRejectsUnknownSession() {
        when(sessionRepository.findByIdForUpdate(9L)).thenReturn(Optional.empty());
        assertThrows(SessionNotExits.class, () -> service.cancel(actor, 9L));
    }
}
