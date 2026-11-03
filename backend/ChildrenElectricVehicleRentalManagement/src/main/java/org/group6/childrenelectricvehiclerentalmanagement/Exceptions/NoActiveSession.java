package org.group6.childrenelectricvehiclerentalmanagement.Exceptions;

public class NoActiveSession extends AbstractException {
    public NoActiveSession() {
        super("No session in 'InProgress' status found, cannot create ticket", "NO_ACTIVE_SESSION", "Không có phiên đang hoạt động, vui lòng tạo phiên trước", 409);
    }
}
