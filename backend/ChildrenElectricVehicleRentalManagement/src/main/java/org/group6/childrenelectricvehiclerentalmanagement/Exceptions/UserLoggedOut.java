package org.group6.childrenelectricvehiclerentalmanagement.Exceptions;

public class UserLoggedOut extends AbstractException {
    public UserLoggedOut() {
        super("user logged out", "Bạn đã đăng xuất", 403);
    }
}
