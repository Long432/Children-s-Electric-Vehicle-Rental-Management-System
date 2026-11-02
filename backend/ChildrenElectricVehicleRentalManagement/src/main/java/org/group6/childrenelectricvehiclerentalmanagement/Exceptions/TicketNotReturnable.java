package org.group6.childrenelectricvehiclerentalmanagement.Exceptions;

public class TicketNotReturnable extends AbstractException {
    public TicketNotReturnable() {
        super("TICKET_NOT_RETURNABLE", "Chỉ trả xe được cho vé đang sử dụng", 409);
    }
}
