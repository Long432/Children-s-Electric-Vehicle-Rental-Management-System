package org.group6.childrenelectricvehiclerentalmanagement.Exceptions;

public class NoActiveSession extends AbstractException {
    public NoActiveSession() {
        super("no active session", "Không có phiên đang làm", 400);
    }
}
