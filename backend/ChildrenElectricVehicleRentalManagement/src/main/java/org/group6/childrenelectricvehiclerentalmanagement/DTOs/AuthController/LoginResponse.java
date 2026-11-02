package org.group6.childrenelectricvehiclerentalmanagement.DTOs.AuthController;

import org.group6.childrenelectricvehiclerentalmanagement.Enum.UserStatus;

/**
 * Thông tin người dùng trả về sau đăng nhập (không chứa mật khẩu, bản băm cookie hay trường nhạy cảm).
 */
public record LoginResponse(Long id, String username, UserStatus status) {
}
