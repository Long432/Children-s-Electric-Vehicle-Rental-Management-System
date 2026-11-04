package org.group6.childrenelectricvehiclerentalmanagement.Exceptions;

public class UserNotExitsException extends AbstractException {
    public UserNotExitsException(long id) {
        super("User with ID " + id + " does not exist", "USER_NOT_FOUND", "Người dùng không tồn tại", 404);
    }
    public UserNotExitsException(String username) {
        super("User with username " + username + " does not exist", "USER_NOT_FOUND", "Người dùng không tồn tại", 404);
    }
}
