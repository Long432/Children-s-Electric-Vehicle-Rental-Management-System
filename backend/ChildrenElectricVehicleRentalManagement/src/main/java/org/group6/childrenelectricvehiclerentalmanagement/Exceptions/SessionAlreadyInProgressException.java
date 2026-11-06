package org.group6.childrenelectricvehiclerentalmanagement.Exceptions;

public class SessionAlreadyInProgressException extends AbstractException {

    public SessionAlreadyInProgressException() {
        super("session already in progress", "Đã có phiên đang làm, hãy kết thúc phiên đó trước", 409, "SESSION_ALREADY_IN_PROGRESS");
    }
}
