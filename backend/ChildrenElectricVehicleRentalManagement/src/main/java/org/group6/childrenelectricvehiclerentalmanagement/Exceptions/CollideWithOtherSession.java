package org.group6.childrenelectricvehiclerentalmanagement.Exceptions;

public class CollideWithOtherSession extends AbstractException {
    public CollideWithOtherSession() {
        super("New session time range collides with an existing active session", "SESSION_COLLIDE", "Phiên bạn tạo bị trùng với phiên khác", 409);
    }
}
