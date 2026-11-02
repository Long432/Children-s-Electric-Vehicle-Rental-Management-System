package org.group6.childrenelectricvehiclerentalmanagement.Exceptions;

public class TicketNotActive extends AbstractException {
    public TicketNotActive() {
        super("TICKET_NOT_ACTIVE", "Chỉ thao tác được cho vé đang sử dụng", 409);
    }
}
