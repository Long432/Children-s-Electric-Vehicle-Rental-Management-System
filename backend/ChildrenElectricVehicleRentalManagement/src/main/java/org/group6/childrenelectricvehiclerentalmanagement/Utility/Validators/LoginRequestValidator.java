package org.group6.childrenelectricvehiclerentalmanagement.Utility.Validators;

import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Auth.LoginRequest;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.ValidationException;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.RequestValidator;

import java.nio.charset.StandardCharsets;

public class LoginRequestValidator implements RequestValidator<LoginRequest> {

    private static final int MAX_USERNAME_LENGTH = 50;
    private static final int MAX_PASSWORD_BYTES = 72;

    @Override
    public void validate(LoginRequest request) throws ValidationException {
        if (request == null) {
            throw new ValidationException("body", "Thiếu dữ liệu gửi lên");
        }
        if (request.username() == null || request.username().isBlank()) {
            throw new ValidationException("username", "Tên đăng nhập không được để trống");
        }
        if (request.username().trim().length() > MAX_USERNAME_LENGTH) {
            throw new ValidationException("username", "Tên đăng nhập tối đa " + MAX_USERNAME_LENGTH + " ký tự");
        }
        if (request.password() == null || request.password().isEmpty()) {
            throw new ValidationException("password", "Mật khẩu không được để trống");
        }
        String password = request.password();
        if (password.length() > MAX_PASSWORD_BYTES || password.getBytes(StandardCharsets.UTF_8).length > MAX_PASSWORD_BYTES) {
            throw new ValidationException("password", "Mật khẩu tối đa " + MAX_PASSWORD_BYTES + " byte (ký tự có dấu chiếm 2 đến 4 byte)");
        }
    }
}
