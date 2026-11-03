package org.group6.childrenelectricvehiclerentalmanagement.DTOs.TicketController;

public record CancelTicketResponse(Long id, Integer ticketNo, String status, Integer refundAmount,
                                   String refundMethod) {
}
