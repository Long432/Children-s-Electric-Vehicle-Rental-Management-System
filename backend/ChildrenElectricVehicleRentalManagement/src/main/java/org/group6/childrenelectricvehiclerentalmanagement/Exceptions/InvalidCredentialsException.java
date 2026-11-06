package org.group6.childrenelectricvehiclerentalmanagement.Exceptions;

public class InvalidCredentialsException extends AbstractException {

    public InvalidCredentialsException() {
        super("invalid credentials", "Sai tên đăng nhập hoặc mật khẩu", 401, "INVALID_CREDENTIALS");
    }
}
