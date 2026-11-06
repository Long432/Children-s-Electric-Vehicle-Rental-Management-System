package org.group6.childrenelectricvehiclerentalmanagement.Exceptions;

/** Cookie thiếu, lệch, hết hạn hoặc tài khoản không còn Online. */
public class UnauthorizedException extends AbstractException {
    public UnauthorizedException() {
        super("unauthorized", "Chưa đăng nhập hoặc phiên đã hết hạn", 401, "UNAUTHORIZED");
    }
}
