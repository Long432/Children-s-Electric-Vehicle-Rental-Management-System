package org.group6.childrenelectricvehiclerentalmanagement.Controllers;

import org.group6.childrenelectricvehiclerentalmanagement.DTOs.ApiResponse;
import org.group6.childrenelectricvehiclerentalmanagement.Services.ReportService;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.Security.AuthenticatedUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
public class ReportController extends BaseController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse> summary(@AuthenticationPrincipal AuthenticatedUser actor,
                                               @RequestParam(required = false) String from,
                                               @RequestParam(required = false) String to,
                                               @RequestParam(name = "user_id", required = false) Long userId) throws Exception {
        return ResponseEntity.ok(ApiResponse.success(reportService.summary(actor, from, to, userId)));
    }

    @GetMapping("/accounts")
    public ResponseEntity<ApiResponse> accounts(@AuthenticationPrincipal AuthenticatedUser actor,
                                                @RequestParam(required = false) String from,
                                                @RequestParam(required = false) String to) throws Exception {
        return ResponseEntity.ok(ApiResponse.success(reportService.accounts(actor, from, to)));
    }

    @GetMapping("/sessions")
    public ResponseEntity<ApiResponse> sessions(@AuthenticationPrincipal AuthenticatedUser actor,
                                                @RequestParam(required = false) String from,
                                                @RequestParam(required = false) String to,
                                                @RequestParam(name = "user_id", required = false) Long userId,
                                                @RequestParam(required = false) Integer page,
                                                @RequestParam(required = false) Integer limit) throws Exception {
        return ResponseEntity.ok(ApiResponse.success(reportService.sessions(actor, from, to, userId, page, limit)));
    }

    @GetMapping("/sessions/{id}")
    public ResponseEntity<ApiResponse> session(@AuthenticationPrincipal AuthenticatedUser actor, @PathVariable long id) throws Exception {
        return ResponseEntity.ok(ApiResponse.success(reportService.session(actor, id)));
    }

    @GetMapping("/incidents")
    public ResponseEntity<ApiResponse> incidents(@AuthenticationPrincipal AuthenticatedUser actor,
                                                 @RequestParam(required = false) String from,
                                                 @RequestParam(required = false) String to,
                                                 @RequestParam(name = "user_id", required = false) Long userId,
                                                 @RequestParam(name = "vehicle_id", required = false) Long vehicleId,
                                                 @RequestParam(required = false) Integer limit) throws Exception {
        return ResponseEntity.ok(ApiResponse.success(reportService.incidents(actor, from, to, userId, vehicleId, limit)));
    }

    @GetMapping("/vehicles")
    public ResponseEntity<ApiResponse> vehicles(@AuthenticationPrincipal AuthenticatedUser actor,
                                                @RequestParam(required = false) String from,
                                                @RequestParam(required = false) String to,
                                                @RequestParam(name = "user_id", required = false) Long userId) throws Exception {
        return ResponseEntity.ok(ApiResponse.success(reportService.vehicles(actor, from, to, userId)));
    }
}
