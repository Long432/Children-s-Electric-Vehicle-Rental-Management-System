package org.group6.childrenelectricvehiclerentalmanagement.Exceptions;

public class TicketNotExits extends AbstractException {

    public TicketNotExits() {
        super("ticket not exists", "Vé này không tồn tại", 404, "TICKET_NOT_FOUND");
    }
}
