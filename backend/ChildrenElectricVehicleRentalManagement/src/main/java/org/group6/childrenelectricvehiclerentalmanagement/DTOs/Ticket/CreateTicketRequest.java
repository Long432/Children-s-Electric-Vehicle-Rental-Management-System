package org.group6.childrenelectricvehiclerentalmanagement.DTOs.Ticket;

public record CreateTicketRequest(String phone, Integer minutes, String payment_method) {
}
