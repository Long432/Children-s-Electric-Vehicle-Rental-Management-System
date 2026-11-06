package org.group6.childrenelectricvehiclerentalmanagement.Exceptions;

public class SessionNotExits extends AbstractException {

    public SessionNotExits() {
        super("session not exists", "Phiên này không tồn tại", 404, "SESSION_NOT_FOUND");
    }
}
