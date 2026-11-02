package org.group6.childrenelectricvehiclerentalmanagement.Controllers;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.AuthController.ApiResponse;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.AuthController.LoginRequest;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.AuthController.LoginResponse;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.AuthController.LoginResult;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.AuthController.MeResponse;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.AbstractException;
import org.group6.childrenelectricvehiclerentalmanagement.Models.User;
import org.group6.childrenelectricvehiclerentalmanagement.Services.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequestMapping(path = "/auth")
@AllArgsConstructor
public class AuthController extends BaseController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) throws Exception {
        LoginResult result = authService.login(request);
        LoginResponse response = new LoginResponse(result.user().id(), result.user().username(), result.user().status());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@AuthenticationPrincipal User actor) throws Exception {
        authService.logout(actor);
        return ResponseEntity.ok(ApiResponse.ok("Đăng xuất thành công"));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(@AuthenticationPrincipal User actor) throws Exception {
        MeResponse response = new MeResponse(actor.getId(), actor.getUsername(), actor.getStatus());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
