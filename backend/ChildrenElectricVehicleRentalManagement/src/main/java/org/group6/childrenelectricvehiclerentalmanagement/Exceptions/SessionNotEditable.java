package org.group6.childrenelectricvehiclerentalmanagement.Exceptions;

public class SessionNotEditable extends AbstractException {
    public SessionNotEditable() {
        super("Only sessions in 'Waiting' status can be edited", "SESSION_NOT_EDITABLE",
                "Chỉ được sửa phiên chưa bắt đầu", 409);
    }
}
