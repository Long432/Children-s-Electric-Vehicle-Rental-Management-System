package org.group6.childrenelectricvehiclerentalmanagement.Exceptions;

public class SessionNotFound extends AbstractException {
    public SessionNotFound() {
        super("session isn't exits", "Phiên làm không tồn tại", 404);
    }
}
