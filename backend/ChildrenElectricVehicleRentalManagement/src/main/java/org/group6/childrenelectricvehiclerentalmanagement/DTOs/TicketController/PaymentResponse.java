package org.group6.childrenelectricvehiclerentalmanagement.DTOs.TicketController;

public record PaymentResponse(Long id, String type, String method, Integer amount, Integer minutes,
                              String createdAt) {
}
