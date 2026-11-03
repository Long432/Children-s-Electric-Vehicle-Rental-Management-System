package org.group6.childrenelectricvehiclerentalmanagement.Exceptions;

public class TicketNotActive extends AbstractException {
    public TicketNotActive() {
        super("Ticket is not active (not in 'Inused' status), action is not allowed", "TICKET_NOT_ACTIVE", "Chỉ có thể thao tác khi vé đang được sử dụng", 409);
    }
}
