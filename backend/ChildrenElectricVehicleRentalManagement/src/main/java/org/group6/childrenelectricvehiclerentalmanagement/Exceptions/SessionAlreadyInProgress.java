package org.group6.childrenelectricvehiclerentalmanagement.Exceptions;

public class SessionAlreadyInProgress extends AbstractException {
    public SessionAlreadyInProgress() {
        super("Another session is already in 'InProgress' status", "SESSION_ALREADY_IN_PROGRESS",
                "Đang có một phiên khác đang làm", 409);
    }
}
