package org.group6.childrenelectricvehiclerentalmanagement.Exceptions;

public class UnauthorizedException extends AbstractException {
    public UnauthorizedException() {
        super("User is not authenticated or session is invalid", "UNAUTHORIZED", "Bạn chưa đăng nhập, vui lòng đăng nhập lại", 401);
    }
}
