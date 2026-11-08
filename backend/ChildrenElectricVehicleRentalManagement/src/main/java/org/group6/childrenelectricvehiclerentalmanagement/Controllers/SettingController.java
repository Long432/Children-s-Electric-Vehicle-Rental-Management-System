package org.group6.childrenelectricvehiclerentalmanagement.Controllers;

import org.group6.childrenelectricvehiclerentalmanagement.DTOs.SettingsController.AmountPerMinuteRequest;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.SettingsController.MinutesRequest;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.SettingsController.ToggleRequest;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.SettingsController.UpdatePackagesRequest;
import org.group6.childrenelectricvehiclerentalmanagement.Models.User;
import org.group6.childrenelectricvehiclerentalmanagement.Services.SettingsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/settings")
public class SettingController extends BaseController {
    private final SettingsService settingsService;

    public SettingController(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @GetMapping({"", "/"})
    public ResponseEntity<?> getSettings() {
        return ResponseEntity.ok(settingsService.getSettings());
    }

    @PostMapping("/toggle_refund")
    public ResponseEntity<?> toggleRefund(
            @AuthenticationPrincipal User actor,
            @RequestBody(required = false) ToggleRequest request) throws Exception {
        return ResponseEntity.ok(settingsService.toggleRefund(actor, request));
    }

    @PostMapping("/toggle_extend")
    public ResponseEntity<?> toggleExtend(
            @AuthenticationPrincipal User actor,
            @RequestBody(required = false) ToggleRequest request) throws Exception {
        return ResponseEntity.ok(settingsService.toggleExtend(actor, request));
    }

    @PutMapping({"", "/"})
    public ResponseEntity<?> updatePackages(
            @AuthenticationPrincipal User actor,
            @RequestBody(required = false) UpdatePackagesRequest request) throws Exception {
        return ResponseEntity.ok(settingsService.updatePackages(actor, request));
    }

    @PostMapping("/set_overdue_fee")
    public ResponseEntity<?> setOverdueFee(
            @AuthenticationPrincipal User actor,
            @RequestBody(required = false) AmountPerMinuteRequest request) throws Exception {
        return ResponseEntity.ok(settingsService.setOverdueFee(actor, request));
    }

    @PostMapping("/set_overdue_limit")
    public ResponseEntity<?> setOverdueLimit(
            @AuthenticationPrincipal User actor,
            @RequestBody(required = false) MinutesRequest request) throws Exception {
        return ResponseEntity.ok(settingsService.setOverdueLimit(actor, request));
    }

    @PostMapping("/set_minimum_time")
    public ResponseEntity<?> setMinimumTime(
            @AuthenticationPrincipal User actor,
            @RequestBody(required = false) MinutesRequest request) throws Exception {
        return ResponseEntity.ok(settingsService.setMinimumTime(actor, request));
    }

    @PostMapping("/set_cancel_wait")
    public ResponseEntity<?> setCancelWait(
            @AuthenticationPrincipal User actor,
            @RequestBody(required = false) MinutesRequest request) throws Exception {
        return ResponseEntity.ok(settingsService.setCancelWait(actor, request));
    }
}
