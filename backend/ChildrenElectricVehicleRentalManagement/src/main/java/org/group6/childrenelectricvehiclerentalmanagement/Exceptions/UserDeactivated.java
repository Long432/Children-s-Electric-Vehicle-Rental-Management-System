package org.group6.childrenelectricvehiclerentalmanagement.Exceptions;

public class UserDeactivated extends AbstractException {
    public UserDeactivated() {
        super("user is account is deactivated","USER_DEACTIVATED","Tài khoản bị khóa",403);
    }
}
