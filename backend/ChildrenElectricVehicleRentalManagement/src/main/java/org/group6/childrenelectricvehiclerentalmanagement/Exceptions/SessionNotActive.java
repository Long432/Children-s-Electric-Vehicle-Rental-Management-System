package org.group6.childrenelectricvehiclerentalmanagement.Exceptions;

public class SessionNotActive extends AbstractException {
    public SessionNotActive() {
        super("Session is not in 'InProgress' status, cannot perform ticket operations", "SESSION_NOT_ACTIVE", "Phiên không còn chạy, không thể thao tác với vé", 409);
    }
}
