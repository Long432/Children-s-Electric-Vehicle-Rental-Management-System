package org.group6.childrenelectricvehiclerentalmanagement.Controllers;

import org.group6.childrenelectricvehiclerentalmanagement.DTOs.ApiResponse;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Auth.LoginRequest;
import org.group6.childrenelectricvehiclerentalmanagement.Services.AuthService;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.Security.AuthenticatedUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController extends BaseController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@RequestBody(required = false) LoginRequest request) throws Exception {
        return ResponseEntity.ok(ApiResponse.success(authService.login(request)));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse> me(@AuthenticationPrincipal AuthenticatedUser actor) throws Exception {
        return ResponseEntity.ok(ApiResponse.success(authService.getProfile(actor)));
    }
}
