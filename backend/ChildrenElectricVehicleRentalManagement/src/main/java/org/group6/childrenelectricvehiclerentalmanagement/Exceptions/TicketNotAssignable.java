package org.group6.childrenelectricvehiclerentalmanagement.Exceptions;

public class TicketNotAssignable extends AbstractException {
    public TicketNotAssignable() {
        super("Vehicle cannot be assigned to ticket because ticket is not in 'Waiting' status", "TICKET_NOT_ASSIGNABLE", "Chỉ có thể gán xe cho vé khi vé đang ở trạng thái chờ", 409);
    }
}
