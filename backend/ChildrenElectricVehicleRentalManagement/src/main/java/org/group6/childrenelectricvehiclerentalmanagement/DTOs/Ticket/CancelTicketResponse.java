package org.group6.childrenelectricvehiclerentalmanagement.DTOs.Ticket;

import org.group6.childrenelectricvehiclerentalmanagement.Enum.PaymentMethod;

public record CancelTicketResponse(int ticket_no, int refund_amount, PaymentMethod refund_method, TicketResponse ticket) {
}
