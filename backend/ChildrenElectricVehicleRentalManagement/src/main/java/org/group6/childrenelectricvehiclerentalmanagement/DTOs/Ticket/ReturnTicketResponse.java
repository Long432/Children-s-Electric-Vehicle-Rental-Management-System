package org.group6.childrenelectricvehiclerentalmanagement.DTOs.Ticket;

import org.group6.childrenelectricvehiclerentalmanagement.Enum.PaymentMethod;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.TicketStatus;

public record ReturnTicketResponse(
        int ticket_no,
        TicketStatus status,
        int overdue_minutes,
        int chargeable_overdue_minutes,
        int overdue_fee,
        PaymentMethod overdue_fee_method,
        int refund_amount,
        PaymentMethod refund_method,
        TicketVehicleResponse vehicle,
        NextTicketResponse next_ticket,
        TicketResponse ticket) {
}
