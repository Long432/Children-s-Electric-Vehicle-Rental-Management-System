package org.group6.childrenelectricvehiclerentalmanagement.Exceptions;

public class UserNotExitsException extends AbstractException {
    public UserNotExitsException(long id) {
        super("người dùng id "+id+" không tồn tại", "Người dùng không khả thi", 404);
    }
}
