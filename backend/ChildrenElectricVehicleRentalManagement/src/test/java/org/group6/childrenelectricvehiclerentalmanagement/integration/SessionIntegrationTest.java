package org.group6.childrenelectricvehiclerentalmanagement.integration;

import lombok.extern.slf4j.Slf4j;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.SessionController.AddSessionVehiclesRequest;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.SessionController.CreateSessionRequest;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.SessionController.SessionDetailResponse;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.SessionController.StartSessionRequest;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.SessionController.UpdateSessionRequest;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.PaymentMethod;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.SessionStatus;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.TicketStatus;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.VehicleStatus;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.AbstractException;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.CollideWithOtherSession;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.SessionHasOpenTickets;
import org.group6.childrenelectricvehiclerentalmanagement.Models.LinkingObject.SessionVehicle;
import org.group6.childrenelectricvehiclerentalmanagement.Models.Session;
import org.group6.childrenelectricvehiclerentalmanagement.Services.SessionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestComponent;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test cho SessionService với H2 in-memory database.
 * Luồng kiểm tra: tạo phiên → bắt đầu → kết thúc → hủy → thêm/bớt xe → stats.
 * Mỗi test tự kiểm soát trạng thái session1 để tránh xung đột (chỉ một phiên InProgress tại một thời điểm).
 */
@Slf4j
class SessionIntegrationTest extends AbstractIntegrationTest {

    @Autowired private SessionService sessionService;

    @Test
    void createWaitingSession_success() throws Exception {
        CreateSessionRequest req = new CreateSessionRequest(
                LocalDateTime.now().plusHours(3).toLocalDate(),
                LocalDateTime.now().plusHours(3).toLocalTime(),
                LocalDateTime.now().plusHours(3).plusMinutes(30).toLocalDate(),
                LocalDateTime.now().plusHours(3).plusMinutes(30).toLocalTime()
        );

        SessionDetailResponse res = sessionService.createSession(req, seller);

        assertNotNull(res);
        assertEquals(SessionStatus.Waiting, res.session_status());
    }

    @Test
    void createWaitingSession_viaCreateSession_shouldCreateWaiting() throws Exception {
        // session1 từ setUp đang InProgress, cần kết thúc trước để không xung đột
        session1.setStatus(SessionStatus.Ended);
        sessionRepository.save(session1);

        LocalDateTime start = LocalDateTime.now().plusHours(10);
        LocalDateTime end = start.plusHours(12);
        CreateSessionRequest req = new CreateSessionRequest(
                start.toLocalDate(), start.toLocalTime(),
                end.toLocalDate(), end.toLocalTime());

        SessionDetailResponse res = sessionService.createSession(req, seller);

        assertNotNull(res);
        assertEquals(SessionStatus.Waiting, res.session_status());
    }

    @Test
    void createSession_collideWithExisting_shouldThrow() throws Exception {
        // session1 từ setUp là InProgress từ 8:00 đến 17:00 ngày 10/11/2026
        CreateSessionRequest req = new CreateSessionRequest(
                LocalDateTime.now().plusMinutes(30).toLocalDate(),
                LocalDateTime.now().plusMinutes(30).toLocalTime(),
                LocalDateTime.now().plusHours(2).toLocalDate(),
                LocalDateTime.now().plusHours(2).toLocalTime()
        );

        assertThrows(CollideWithOtherSession.class, () -> sessionService.createSession(req, seller));
    }

    @Test
    void startWaitingSession_shouldBecomeInProgress() throws Exception {
        session1.setStatus(SessionStatus.Waiting);
        sessionRepository.save(session1);
        StartSessionRequest req = new StartSessionRequest(session1.getId(),null, null);

        SessionDetailResponse res = sessionService.startSession(req, seller);

        assertNotNull(res);
        assertEquals(SessionStatus.InProgress, res.session_status());
    }

    @Test
    void startAlreadyInProgressSession_shouldThrow() throws Exception {
        // session1 từ setUp đã InProgress
        StartSessionRequest req = new StartSessionRequest(session1.getId(), null, null);

        assertThrows(AbstractException.class, () -> sessionService.startSession(req, seller));
    }

    @Test
    void startInstantSessionId_shouldCreateNewInProgress() throws Exception {
        // Kết thúc session1 từ setUp trước
        session1.setStatus(SessionStatus.Ended);
        sessionRepository.save(session1);

        LocalDateTime start = LocalDateTime.of(2026, 11, 14, 8, 0);
        LocalDateTime end = LocalDateTime.of(2026, 11, 14, 20, 30);
        CreateSessionRequest createReq = new CreateSessionRequest(
                start.toLocalDate(), start.toLocalTime(),
                end.toLocalDate(), end.toLocalTime());

        SessionDetailResponse createRes = sessionService.createSession(createReq,seller);
        StartSessionRequest req = new StartSessionRequest(createRes.id(), List.of("xe-01"),createRes.planned_end_at());
        SessionDetailResponse res = sessionService.startSession(req, seller);

        assertNotNull(res);
        assertEquals(SessionStatus.InProgress, res.session_status());
    }

    @Test
    void endCurrentSession_noOpenTickets_shouldEnd() throws Exception {
        // session1 từ setUp là InProgress, không có ticket
        SessionDetailResponse res = sessionService.endCurrentSession(seller);

        assertNotNull(res);
        assertEquals(SessionStatus.Ended, res.session_status());
        assertEquals(VehicleStatus.Offline, vehicle1.getStatus());
    }

    @Test
    void endCurrentSession_withOpenTickets_shouldThrow() throws Exception {
        // session1 từ setUp là InProgress, tạo ticket Waiting
        createWaitingTicket(session1, "0901234567", 30, PaymentMethod.Cash);

        assertThrows(SessionHasOpenTickets.class, () -> sessionService.endCurrentSession(seller));
    }

    @Test
    void cancelWaitingSession_shouldBeCanceled() throws Exception {
        Session waiting = createWaitingSession();

        SessionDetailResponse res = sessionService.cancelSession(waiting.getId(), seller);

        assertNotNull(res);
        assertEquals(SessionStatus.Canceled, res.session_status());
    }

    @Test
    void cancelInProgressSession_shouldThrow() throws Exception {
        // session1 từ setUp đã InProgress
        assertThrows(AbstractException.class, () -> sessionService.cancelSession(session1.getId(), seller));
    }

    @Test
    void removeVehicleFromCurrentSession_success() throws Exception {
        // session1 từ setUp đã InProgress, bớt xe trực tiếp
        SessionDetailResponse res = sessionService.removeVehicleFromCurrentSession(vehicle1.getId(), seller);

        assertNotNull(res);
    }

    @Test
    void addVehicleToCurrentSession_success() throws Exception {
        // session1 từ setUp đã InProgress, thêm xe trực tiếp
        AddSessionVehiclesRequest req = new AddSessionVehiclesRequest(List.of("xe-03"));

        SessionDetailResponse res = sessionService.addVehiclesToCurrentSession(req, seller);

        assertNotNull(res);
    }

    @Test
    void getSessionDetail_success() throws Exception {
        SessionDetailResponse res = sessionService.getSessionDetail(session1.getId());

        assertNotNull(res);
        assertEquals(session1.getId(), res.id());
    }

    @Test
    void updateWaitingSession_success() throws Exception {
        Session waiting = createWaitingSession();

        UpdateSessionRequest req = new UpdateSessionRequest(
                LocalDateTime.now().plusHours(7),
                LocalDateTime.now().plusHours(6).toLocalDate(),
                LocalDateTime.now().plusHours(6).toLocalTime(),
                false
        );

        SessionDetailResponse res = sessionService.updateSession(waiting.getId(), req, seller);

        assertNotNull(res);
    }

    @Test
    void listSessions_success() throws Exception {
        var res = sessionService.listSessions(null, 1, 20);

        assertNotNull(res);
        assertTrue(!res.items().isEmpty());
    }
}
