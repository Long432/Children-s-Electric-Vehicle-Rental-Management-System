package org.group6.childrenelectricvehiclerentalmanagement.Exceptions;

public class TicketNotAssignable extends AbstractException {
    public TicketNotAssignable() {
        super("TICKET_NOT_ASSIGNABLE", "Chỉ gán xe được cho vé đang chờ (Waiting)", 409);
    }
}
