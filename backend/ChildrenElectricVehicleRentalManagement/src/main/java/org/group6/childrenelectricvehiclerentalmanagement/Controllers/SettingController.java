package org.group6.childrenelectricvehiclerentalmanagement.Controllers;

import org.group6.childrenelectricvehiclerentalmanagement.DTOs.AmountPerMinuteRequest;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.ApiResponse;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.MinutesRequest;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.ToggleRequest;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.UpdatePackagesRequest;
import org.group6.childrenelectricvehiclerentalmanagement.Services.AuthService;
import org.group6.childrenelectricvehiclerentalmanagement.Services.SettingsService;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.Security.AuthenticatedUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/settings")
public class SettingController extends BaseController {

    private final AuthService authService;
    private final SettingsService settingsService;

    public SettingController(AuthService authService, SettingsService settingsService) {
        this.authService = authService;
        this.settingsService = settingsService;
    }

    @GetMapping({"", "/"})
    public ResponseEntity<ApiResponse> getSettings() {
        return ResponseEntity.ok(ApiResponse.success(settingsService.getSettings()));
    }

    @PutMapping({"", "/"})
    public ResponseEntity<ApiResponse> updatePackages(@AuthenticationPrincipal AuthenticatedUser actor, @RequestBody(required = false) UpdatePackagesRequest request) throws Exception {
        return ResponseEntity.ok(ApiResponse.success(settingsService.updatePackages(actor, request)));
    }

    @PostMapping("/toggle_refund")
    public ResponseEntity<ApiResponse> toggleRefund(@AuthenticationPrincipal AuthenticatedUser actor, @RequestBody(required = false) ToggleRequest request) throws Exception {
        return ResponseEntity.ok(ApiResponse.success(settingsService.toggleRefund(actor, request)));
    }

    @PostMapping("/toggle_extend")
    public ResponseEntity<ApiResponse> toggleExtend(@AuthenticationPrincipal AuthenticatedUser actor, @RequestBody(required = false) ToggleRequest request) throws Exception {
        return ResponseEntity.ok(ApiResponse.success(settingsService.toggleExtend(actor, request)));
    }

    @PostMapping("/set_overdue_fee")
    public ResponseEntity<ApiResponse> setOverdueFee(@AuthenticationPrincipal AuthenticatedUser actor, @RequestBody(required = false) AmountPerMinuteRequest request) throws Exception {
        return ResponseEntity.ok(ApiResponse.success(settingsService.setOverdueFee(actor, request)));
    }

    @PostMapping("/set_overdue_limit")
    public ResponseEntity<ApiResponse> setOverdueLimit(@AuthenticationPrincipal AuthenticatedUser actor, @RequestBody(required = false) MinutesRequest request) throws Exception {
        return ResponseEntity.ok(ApiResponse.success(settingsService.setOverdueLimit(actor, request)));
    }

    @PostMapping("/set_minimum_time")
    public ResponseEntity<ApiResponse> setMinimumTime(@AuthenticationPrincipal AuthenticatedUser actor, @RequestBody(required = false) MinutesRequest request) throws Exception {
        return ResponseEntity.ok(ApiResponse.success(settingsService.setMinimumTime(actor, request)));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout(@AuthenticationPrincipal AuthenticatedUser actor) throws Exception {
        authService.logout(actor);
        return ResponseEntity.ok(ApiResponse.success(Map.of()));
    }
}
