package org.group6.childrenelectricvehiclerentalmanagement.DTOs.AuthController;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Thông tin đăng nhập tài khoản.
 * Trường định danh dùng đúng tên field model {@code User} đang dùng: {@code username}.
 */
public record LoginRequest(
        @NotBlank(message = "Tên tài khoản không được để trống")
        @Size(min = 3, max = 20, message = "Tên tài khoản từ 3 đến 20 ký tự")
        String username,

        @NotBlank(message = "Mật khẩu không được để trống")
        @Size(min = 8, max = 64, message = "Mật khẩu từ 8 đến 64 ký tự")
        String password
) {
}
