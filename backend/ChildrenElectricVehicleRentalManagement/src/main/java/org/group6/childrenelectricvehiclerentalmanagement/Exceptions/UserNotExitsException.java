package org.group6.childrenelectricvehiclerentalmanagement.Exceptions;

public class UserNotExitsException extends AbstractException {
    public UserNotExitsException(long id) {
        super("user with id "+id+" is not exits", "Người dùng không tồn tại", 404);
    }
}
