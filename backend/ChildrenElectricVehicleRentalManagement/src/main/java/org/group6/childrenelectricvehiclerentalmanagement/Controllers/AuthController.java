package org.group6.childrenelectricvehiclerentalmanagement.Controllers;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.AuthController.ApiResponse;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.AuthController.ErrorInfo;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.AuthController.LoginRequest;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.AuthController.LoginResponse;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.AuthController.LoginResult;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.AuthController.MeResponse;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.AbstractException;
import org.group6.childrenelectricvehiclerentalmanagement.Services.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletResponse;
import java.util.stream.Collectors;

/**
 * Endpoint auth:
 *   POST /api/auth/login            (công khai)
 *   GET  /api/auth/me               (bắt buộc cookie)
 *   POST /api/settings/logout       (bắt buộc cookie)
 *
 * Lưu ý: {@code AuthController} không còn mapping cấp lớp để các method có thể dùng
 * path đầy đủ, do đó {@code logout} trỏ về {@code /api/settings/logout} theo tài liệu,
 * trong khi {@code login}/{@code me} vẫn giữ {@code /api/auth/*} (kèm context-path {@code /api}).
 * Không log mật khẩu, giá trị cookie hay bản băm cookie.
 */
@Slf4j
@Controller
@AllArgsConstructor
public class AuthController extends BaseController {
}
