package org.group6.childrenelectricvehiclerentalmanagement.Exceptions;

public class TicketNotCancelable extends AbstractException {
    public TicketNotCancelable() {
        super("TICKET_NOT_CANCELABLE", "Chỉ hủy được vé đang chờ (Waiting)", 409);
    }
}
