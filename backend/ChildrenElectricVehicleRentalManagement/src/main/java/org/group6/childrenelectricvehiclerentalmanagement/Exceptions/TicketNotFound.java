package org.group6.childrenelectricvehiclerentalmanagement.Exceptions;

public class TicketNotFound extends AbstractException {
    public TicketNotFound() {
        super("TICKET_NOT_FOUND", "Không tìm thấy vé", 404);
    }
}
