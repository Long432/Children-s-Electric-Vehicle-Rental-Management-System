package org.group6.childrenelectricvehiclerentalmanagement.Exceptions;

public class UnauthorizedException extends AbstractException {
    public UnauthorizedException() {
        super("UNAUTHORIZED", "Chưa đăng nhập hoặc phiên đăng nhập không hợp lệ", 401);
    }
}
