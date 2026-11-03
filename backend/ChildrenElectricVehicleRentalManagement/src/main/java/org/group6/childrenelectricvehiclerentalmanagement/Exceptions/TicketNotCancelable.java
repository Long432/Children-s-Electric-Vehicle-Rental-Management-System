package org.group6.childrenelectricvehiclerentalmanagement.Exceptions;

public class TicketNotCancelable extends AbstractException {
    public TicketNotCancelable() {
        super("Ticket cannot be canceled because it is not in 'Waiting' status", "TICKET_NOT_CANCELABLE", "Chỉ có thể hủy vé khi vé đang ở trạng thái chờ", 409);
    }
}
