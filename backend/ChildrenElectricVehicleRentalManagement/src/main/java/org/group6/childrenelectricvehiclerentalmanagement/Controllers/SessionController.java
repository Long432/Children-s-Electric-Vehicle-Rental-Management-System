package org.group6.childrenelectricvehiclerentalmanagement.Controllers;

import org.group6.childrenelectricvehiclerentalmanagement.DTOs.ApiResponse;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Session.CreateSessionRequest;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Session.SessionListQuery;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Session.StartSessionRequest;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Session.UpdateSessionRequest;
import org.group6.childrenelectricvehiclerentalmanagement.Services.SessionService;
import org.group6.childrenelectricvehiclerentalmanagement.Services.SessionStatsService;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.Security.AuthenticatedUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sessions")
public class SessionController extends BaseController {

    private final SessionService sessionService;
    private final SessionStatsService sessionStatsService;

    public SessionController(SessionService sessionService, SessionStatsService sessionStatsService) {
        this.sessionService = sessionService;
        this.sessionStatsService = sessionStatsService;
    }

    @PostMapping({"", "/"})
    public ResponseEntity<ApiResponse> create(@AuthenticationPrincipal AuthenticatedUser actor, @RequestBody(required = false) CreateSessionRequest request) throws Exception {
        return ResponseEntity.ok(ApiResponse.success(sessionService.create(actor, request)));
    }

    @PostMapping("/start")
    public ResponseEntity<ApiResponse> start(@AuthenticationPrincipal AuthenticatedUser actor, @RequestBody(required = false) StartSessionRequest request) throws Exception {
        return ResponseEntity.ok(ApiResponse.success(sessionService.start(actor, request)));
    }

    @PostMapping("/current/end")
    public ResponseEntity<ApiResponse> end(@AuthenticationPrincipal AuthenticatedUser actor) throws Exception {
        return ResponseEntity.ok(ApiResponse.success(sessionService.end(actor)));
    }

    @GetMapping("/current")
    public ResponseEntity<ApiResponse> current() throws Exception {
        return ResponseEntity.ok(ApiResponse.success(sessionService.current()));
    }

    @GetMapping("/current/stats")
    public ResponseEntity<ApiResponse> currentStats() throws Exception {
        return ResponseEntity.ok(ApiResponse.success(sessionStatsService.current()));
    }

    @GetMapping({"", "/"})
    public ResponseEntity<ApiResponse> list(@RequestParam(required = false) String status,
                                            @RequestParam(required = false) Integer page,
                                            @RequestParam(required = false) Integer limit) throws Exception {
        return ResponseEntity.ok(ApiResponse.success(sessionService.list(new SessionListQuery(status, page, limit))));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> get(@PathVariable long id) throws Exception {
        return ResponseEntity.ok(ApiResponse.success(sessionService.get(id)));
    }

    @GetMapping("/{id}/stats")
    public ResponseEntity<ApiResponse> stats(@PathVariable long id) throws Exception {
        return ResponseEntity.ok(ApiResponse.success(sessionStatsService.get(id)));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse> update(@AuthenticationPrincipal AuthenticatedUser actor, @PathVariable long id, @RequestBody(required = false) UpdateSessionRequest request) throws Exception {
        return ResponseEntity.ok(ApiResponse.success(sessionService.update(actor, id, request)));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse> cancel(@AuthenticationPrincipal AuthenticatedUser actor, @PathVariable long id) throws Exception {
        return ResponseEntity.ok(ApiResponse.success(sessionService.cancel(actor, id)));
    }
}
