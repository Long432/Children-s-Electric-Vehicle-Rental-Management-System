package org.group6.childrenelectricvehiclerentalmanagement.Exceptions;

public class UserLoggedOut extends AbstractException {
    public UserLoggedOut() {
        super("User session is logged out or invalid", "USER_LOGGED_OUT", "Bạn đã đăng xuất, vui lòng đăng nhập lại", 403);
    }
}
