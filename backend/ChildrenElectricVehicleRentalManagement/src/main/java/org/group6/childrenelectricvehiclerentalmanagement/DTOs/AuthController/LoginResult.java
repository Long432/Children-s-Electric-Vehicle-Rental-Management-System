package org.group6.childrenelectricvehiclerentalmanagement.DTOs.AuthController;

/**
 * Kết quả nội bộ của đăng nhập: dữ liệu phản hồi plus giá trị cookie gốc và thời hạn,
 * dùng để controller gán header {@code Set-Cookie} (giá trị gốc, không phải bản băm).
 */
public record LoginResult(LoginResponse user, String cookieValue, int maxAgeSeconds) {
}
