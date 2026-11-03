package org.group6.childrenelectricvehiclerentalmanagement.Exceptions;

public class ExtendDisabled extends AbstractException {
    public ExtendDisabled() {
        super("Ticket extension feature is disabled in system settings", "EXTEND_DISABLED", "Tính năng gia hạn vé hiện đang tắt", 409);
    }
}
