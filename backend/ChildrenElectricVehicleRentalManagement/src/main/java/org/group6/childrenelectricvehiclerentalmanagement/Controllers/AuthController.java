package org.group6.childrenelectricvehiclerentalmanagement.Controllers;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.AuthController.*;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.AbstractException;
import org.group6.childrenelectricvehiclerentalmanagement.Models.User;
import org.group6.childrenelectricvehiclerentalmanagement.Services.AuthService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.header.Header;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.function.ServerRequest;

import java.util.Map;

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
        return ResponseEntity.ok().header("Authorization","Bearer "+result.cookieValue()).body(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@AuthenticationPrincipal User actor) throws Exception {
        authService.logout(actor);
        return ResponseEntity.ok("Đăng xuất thành công");
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(@AuthenticationPrincipal User actor) throws Exception {
        return ResponseEntity.ok(authService.getMe(actor));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignUpRequest request) throws Exception {
        return ResponseEntity.ok(new LoginResponse(authService.signUp(request)));
    }
}
