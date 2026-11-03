package org.group6.childrenelectricvehiclerentalmanagement.Exceptions;

public class TicketNotFound extends AbstractException {
    public TicketNotFound() {
        super("Ticket with the given ID was not found in the database", "TICKET_NOT_FOUND", "Không tìm thấy vé", 404);
    }
}
