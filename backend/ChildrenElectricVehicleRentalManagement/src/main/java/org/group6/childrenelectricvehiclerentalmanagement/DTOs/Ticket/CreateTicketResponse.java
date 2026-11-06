package org.group6.childrenelectricvehiclerentalmanagement.DTOs.Ticket;

import java.util.List;

public record CreateTicketResponse(
        int ticket_no,
        int amount_paid,
        TicketResponse ticket,
        List<TicketVehicleResponse> suggested_vehicles) {
}
