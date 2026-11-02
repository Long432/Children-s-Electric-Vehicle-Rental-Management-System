package org.group6.childrenelectricvehiclerentalmanagement.Exceptions;

public class CollideWithOtherSession extends AbstractException {
    public CollideWithOtherSession() {
        super("request session is collide with other session", "Phiên bạn tạo bị trùng với phiên khác", 400);
    }
}
