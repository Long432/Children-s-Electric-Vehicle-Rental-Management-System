package org.group6.childrenelectricvehiclerentalmanagement.Exceptions;

public class SessionNotActive extends AbstractException {
    public SessionNotActive() {
        super("SESSION_NOT_ACTIVE", "Phiên của vé này không còn chạy", 409);
    }
}
