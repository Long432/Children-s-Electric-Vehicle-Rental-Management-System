package org.group6.childrenelectricvehiclerentalmanagement.DTOs.Ticket;

import org.group6.childrenelectricvehiclerentalmanagement.Enum.PaymentMethod;

public record ExtendTicketResponse(
        int ticket_no,
        int extended_minutes,
        int amount_paid,
        PaymentMethod payment_method,
        TicketResponse ticket) {
}
