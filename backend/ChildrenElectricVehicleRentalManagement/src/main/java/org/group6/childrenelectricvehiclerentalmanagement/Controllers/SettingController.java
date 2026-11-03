package org.group6.childrenelectricvehiclerentalmanagement.Controllers;

import org.group6.childrenelectricvehiclerentalmanagement.DTOs.AmountPerMinuteRequest;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.MinutesRequest;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.ToggleRequest;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.UpdatePackagesRequest;
import org.group6.childrenelectricvehiclerentalmanagement.Models.User;
import org.group6.childrenelectricvehiclerentalmanagement.Services.AuthService;
import org.group6.childrenelectricvehiclerentalmanagement.Services.SettingsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 7 endpoint cài đặt. Mỗi endpoint: xác thực cookie -> body (đã thành DTO) -> service.
 * Việc kiểm tra từng field do validator trong package Utility đảm nhận, gọi ở đầu hàm service.
 */
@RestController
@RequestMapping("/settings")
public class SettingController extends BaseController {
    private final AuthService authService;
    private final SettingsService settingsService;

    public SettingController(AuthService authService, SettingsService settingsService) {
        this.authService = authService;
        this.settingsService = settingsService;
    }

    /** GET /api/settings : lấy toàn bộ cài đặt */
    @GetMapping("/")
    public ResponseEntity<?> getSettings() {
        return ResponseEntity.ok(settingsService.getSettings());
    }

    /** POST /api/settings/toggle_refund : bật/tắt hoàn tiền */
    @PostMapping("/toggle_refund")
    public ResponseEntity<?> toggleRefund(@AuthenticationPrincipal User actor, @RequestBody(required = false) ToggleRequest request) throws Exception {
            return ResponseEntity.ok(settingsService.toggleRefund(actor, request));
    }

    /** POST /api/settings/toggle_extend : bật/tắt gia hạn */
    @PostMapping("/toggle_extend")
    public ResponseEntity<?> toggleExtend(@RequestBody(required = false) ToggleRequest request, @AuthenticationPrincipal User actor) throws Exception {
            return ResponseEntity.ok(settingsService.toggleExtend(actor, request));
    }

    /** PUT /api/settings/ : cập nhật các gói giá (thay toàn bộ danh sách) */
    @PutMapping({"", "/"})
    public ResponseEntity<?> updatePackages(@RequestBody(required = false) UpdatePackagesRequest request,@AuthenticationPrincipal User actor) throws Exception {
            return ResponseEntity.ok(settingsService.updatePackages(actor, request));
    }

    /** POST /api/settings/set_overdue_fee : phụ phí quá giờ mỗi phút */
    @PostMapping("/set_overdue_fee")
    public ResponseEntity<?> setOverdueFee(@RequestBody(required = false) AmountPerMinuteRequest request,@AuthenticationPrincipal User actor) throws Exception {
        return ResponseEntity.ok(settingsService.setOverdueFee(actor, request));
    }

    /** POST /api/settings/set_overdue_limit : thời gian ân hạn */
    @PostMapping("/set_overdue_limit")
    public ResponseEntity<?> setOverdueLimit(@RequestBody(required = false) MinutesRequest request, @AuthenticationPrincipal User actor) throws Exception {
        return ResponseEntity.ok(settingsService.setOverdueLimit(actor, request));
    }

    /** POST /api/settings/set_minimum_time : thời gian thuê tối thiểu */
    @PostMapping("/set_minimum_time")
    public ResponseEntity<?> setMinimumTime(@RequestBody(required = false) MinutesRequest request, @AuthenticationPrincipal User actor) throws Exception {
        return ResponseEntity.ok(settingsService.setMinimumTime(actor, request));
    }
}
