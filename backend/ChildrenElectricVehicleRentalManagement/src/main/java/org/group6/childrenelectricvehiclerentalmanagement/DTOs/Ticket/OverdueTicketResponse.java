package org.group6.childrenelectricvehiclerentalmanagement.DTOs.Ticket;

public record OverdueTicketResponse(
        TicketResponse ticket,
        int overdue_minutes,
        int chargeable_overdue_minutes,
        int estimated_overdue_fee) {
}
