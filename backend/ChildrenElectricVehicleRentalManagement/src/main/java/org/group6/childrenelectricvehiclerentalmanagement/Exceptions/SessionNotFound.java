package org.group6.childrenelectricvehiclerentalmanagement.Exceptions;

public class SessionNotFound extends AbstractException {
    public SessionNotFound() {
        super("Session with the given ID was not found in the database", "SESSION_NOT_FOUND", "Phiên làm không tồn tại", 404);
    }
}
