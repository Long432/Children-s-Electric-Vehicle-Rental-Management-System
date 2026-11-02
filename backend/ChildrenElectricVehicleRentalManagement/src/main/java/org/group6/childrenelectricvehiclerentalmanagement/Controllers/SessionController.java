package org.group6.childrenelectricvehiclerentalmanagement.Controllers;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.SessionController.CreateSessionRequest;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.SessionController.SessionBasicResponse;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Long.Requests;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Long.Responses;
import org.group6.childrenelectricvehiclerentalmanagement.Models.Session;
import org.group6.childrenelectricvehiclerentalmanagement.Models.User;
import org.group6.childrenelectricvehiclerentalmanagement.Services.SessionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequestMapping(path = "throws Exception/sessions")
@AllArgsConstructor
public class SessionController extends BaseController {
    private SessionService sessionService;

    @PostMapping
    public ResponseEntity<?> createSession(@RequestBody CreateSessionRequest request, @AuthenticationPrincipal User user) throws Exception {
        log.info("Creating new session with status: Waiting");
        return ResponseEntity.ok(new SessionBasicResponse(sessionService.createSession(request, user)));
    }

    @PostMapping("/start")
    public ResponseEntity<?> startSession(@RequestParam Long sessionId) throws Exception {
        log.info("Starting session with id: {}", sessionId);
        return ResponseEntity.ok(sessionService.startSession(sessionId));
    }

    @PostMapping("/current/end")
    public ResponseEntity<?> endCurrentSession() throws Exception {
        log.info("Ending current session");
        return ResponseEntity.ok(sessionService.endCurrentSession());
    }

    @GetMapping("/current")
    public ResponseEntity<?> getCurrentSession() throws Exception {
        log.info("Getting current session");
        return ResponseEntity.ok(sessionService.getCurrentSession());
    }

    @GetMapping
    public ResponseEntity<?> getAllSessions() throws Exception {
        log.info("Getting all sessions history");
        return ResponseEntity.ok(sessionService.getAllSessions());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getSessionDetail(@PathVariable Long id) throws Exception {
        log.info("Getting session detail with id: {}", id);
        return ResponseEntity.ok(sessionService.getSessionDetail(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> updateSession(@PathVariable Long id, @RequestBody Session session) throws Exception {
        log.info("Updating session with id: {}", id);
        return ResponseEntity.ok(sessionService.updateSession(id, session));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancelSession(@PathVariable Long id) throws Exception {
        log.info("Cancelling session with id: {}", id);
        return ResponseEntity.ok(sessionService.cancelSession(id));
    }

    @PostMapping("/current/vehicles")
    public ResponseEntity<?> addVehicleToCurrentSession(@RequestParam Long vehicleId) throws Exception {
        log.info("Adding vehicle with id: {} to current session", vehicleId);
        return ResponseEntity.ok(sessionService.addVehicleToCurrentSession(vehicleId));
    }

    @DeleteMapping("/current/vehicles/{vehicle_id}")
    public ResponseEntity<?> removeVehicleFromCurrentSession(@PathVariable Long vehicle_id) throws Exception {
        log.info("Removing vehicle with id: {} from current session", vehicle_id);
        return ResponseEntity.ok(sessionService.removeVehicleFromCurrentSession(vehicle_id));
    }

    @GetMapping("/current/stats")
    public ResponseEntity<?> getCurrentStats(@AuthenticationPrincipal User actor) throws Exception {
        Requests.SessionStatsRequest request = new Requests.SessionStatsRequest();
        request.setActorId(actor.getId());
        return ResponseEntity.ok(sessionService.getCurrentStats(request));
    }

    @GetMapping("/{id}/stats")
    public ResponseEntity<Object> getStats(@PathVariable("id") String id, @AuthenticationPrincipal User actor) throws Exception {
        Requests.SessionStatsRequest request = new Requests.SessionStatsRequest();
        request.setActorId(actor.getId());
        request.setSessionId(id);
        return ResponseEntity.ok(sessionService.getStats(request));
    }
}
