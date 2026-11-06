package org.group6.childrenelectricvehiclerentalmanagement.DTOs.Ticket;

import org.group6.childrenelectricvehiclerentalmanagement.Models.Ticket;

public record NextTicketResponse(long id, int ticket_no, String phone) {

    public static NextTicketResponse from(Ticket ticket) {
        if (ticket == null) {
            return null;
        }
        return new NextTicketResponse(ticket.getId(), ticket.getTicketNo(), ticket.getPhone());
    }
}
