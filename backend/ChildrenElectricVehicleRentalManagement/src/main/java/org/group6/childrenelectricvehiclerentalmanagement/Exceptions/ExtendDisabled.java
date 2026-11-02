package org.group6.childrenelectricvehiclerentalmanagement.Exceptions;

public class ExtendDisabled extends AbstractException {
    public ExtendDisabled() {
        super("EXTEND_DISABLED", "Tính năng gia hạn đang tắt", 409);
    }
}
