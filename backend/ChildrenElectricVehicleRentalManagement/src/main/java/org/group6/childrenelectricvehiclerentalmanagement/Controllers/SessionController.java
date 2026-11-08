package org.group6.childrenelectricvehiclerentalmanagement.Controllers;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Long.Requests;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.SessionController.AddSessionVehiclesRequest;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.SessionController.CreateSessionRequest;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.SessionController.StartSessionRequest;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.SessionController.UpdateSessionRequest;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.UnauthorizedException;
import org.group6.childrenelectricvehiclerentalmanagement.Models.User;
import org.group6.childrenelectricvehiclerentalmanagement.Services.SessionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(path = "/sessions")
@AllArgsConstructor
public class SessionController extends BaseController {
    private SessionService sessionService;

    /** POST /api/sessions: tạo phiên (Waiting, hoặc InProgress nếu is_instant = true). */
    @PostMapping
    public ResponseEntity<?> createSession(@RequestBody CreateSessionRequest request,
                                           @AuthenticationPrincipal User actor) throws Exception {
        log.info("Creating new session");
        return ResponseEntity.ok(sessionService.createSession(request, actor));
    }

    /** POST /api/sessions/start: bắt đầu phiên Waiting (có session_id) hoặc tạo phiên instant (không có session_id). */
    @PostMapping("/start")
    public ResponseEntity<?> startSession(@RequestBody StartSessionRequest request,
                                          @AuthenticationPrincipal User actor) throws Exception {
        log.info("Starting session with id: {}", request == null ? null : request.session_id());
        return ResponseEntity.ok(sessionService.startSession(request, actor));
    }

    @PostMapping("/current/end")
    public ResponseEntity<?> endCurrentSession(@AuthenticationPrincipal User actor) throws Exception {
        log.info("Ending current session");
        return ResponseEntity.ok(sessionService.endCurrentSession(actor));
    }

    @GetMapping("/current")
    public ResponseEntity<?> getCurrentSession() throws Exception {
        log.info("Getting current session");
        return ResponseEntity.ok(sessionService.getCurrentSession());
    }

    /** GET /api/sessions?status=&page=&limit= */
    @GetMapping
    public ResponseEntity<?> getAllSessions(@RequestParam(required = false) String status,
                                            @RequestParam(required = false) Integer page,
                                            @RequestParam(required = false) Integer limit) throws Exception {
        log.info("Getting sessions history");
        return ResponseEntity.ok(sessionService.listSessions(status, page, limit));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getSessionDetail(@PathVariable Long id) throws Exception {
        log.info("Getting session detail with id: {}", id);
        return ResponseEntity.ok(sessionService.getSessionDetail(id));
    }

    /** PATCH /api/sessions/{id}: gửi nguyên request gồm planned_end_at, scheduled_date, scheduled_time, is_instant. */
    @PatchMapping("/{id}")
    public ResponseEntity<?> updateSession(@PathVariable Long id,
                                           @RequestBody UpdateSessionRequest request,
                                           @AuthenticationPrincipal User actor) throws Exception {
        log.info("Updating session with id: {}", id);
        return ResponseEntity.ok(sessionService.updateSession(id, request, actor));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancelSession(@PathVariable Long id,
                                           @AuthenticationPrincipal User actor) throws Exception {
        log.info("Cancelling session with id: {}", id);
        return ResponseEntity.ok(sessionService.cancelSession(id, actor));
    }

    /** POST /api/sessions/current/vehicles: thêm xe (vehicle_slugs[]) vào phiên đang làm. */
    @PostMapping("/current/vehicles")
    public ResponseEntity<?> addVehiclesToCurrentSession(@RequestBody AddSessionVehiclesRequest request,
                                                         @AuthenticationPrincipal User actor) throws Exception {
        log.info("Adding vehicles to current session");
        return ResponseEntity.ok(sessionService.addVehiclesToCurrentSession(request, actor));
    }

    @DeleteMapping("/current/vehicles/{vehicle_id}")
    public ResponseEntity<?> removeVehicleFromCurrentSession(@PathVariable("vehicle_id") Long vehicleId,
                                                             @AuthenticationPrincipal User actor) throws Exception {
        log.info("Removing vehicle with id: {} from current session", vehicleId);
        return ResponseEntity.ok(sessionService.removeVehicleFromCurrentSession(vehicleId, actor));
    }

    @GetMapping("/current/stats")
    public ResponseEntity<?> getCurrentStats(@AuthenticationPrincipal User actor) throws Exception {
        if (actor == null) throw new UnauthorizedException();
        Requests.SessionStatsRequest request = new Requests.SessionStatsRequest();
        request.setActorId(actor.getId());
        return ResponseEntity.ok(sessionService.getCurrentStats(request));
    }

    @GetMapping("/{id}/stats")
    public ResponseEntity<Object> getStats(@PathVariable("id") String id,
                                           @AuthenticationPrincipal User actor) throws Exception {
        if (actor == null) throw new UnauthorizedException();
        Requests.SessionStatsRequest request = new Requests.SessionStatsRequest();
        request.setActorId(actor.getId());
        request.setSessionId(id);
        return ResponseEntity.ok(sessionService.getStats(request));
    }
}
