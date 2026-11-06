package org.group6.childrenelectricvehiclerentalmanagement.Exceptions;

public class NoActiveSessionException extends AbstractException {

    public NoActiveSessionException() {
        super("no session in progress", "Hiện chưa có phiên nào đang làm", 409, "NO_ACTIVE_SESSION");
    }
}
