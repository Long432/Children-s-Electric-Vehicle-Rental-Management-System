package org.group6.childrenelectricvehiclerentalmanagement.DTOs.TicketController;

public record ExtendTicketResponse(TicketResponse ticket, Integer extendedMinutes, Integer extensionAmount) {
}
