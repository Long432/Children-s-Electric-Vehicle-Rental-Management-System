package org.group6.childrenelectricvehiclerentalmanagement.Exceptions;

public class TicketNotReturnable extends AbstractException {
    public TicketNotReturnable() {
        super("Ticket cannot be returned because it is not in 'Inused' status", "TICKET_NOT_RETURNABLE", "Chỉ có thể trả xe khi vé đang được sử dụng", 409);
    }
}
