package org.group6.childrenelectricvehiclerentalmanagement.DTOs.Ticket;

public record TicketListQuery(String status, Long session_id, Integer page, Integer limit) {
}
