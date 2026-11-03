package org.group6.childrenelectricvehiclerentalmanagement.DTOs.TicketController;

public record VehicleHistoryResponse(VehicleBrief vehicle, String assignedAt, String releasedAt,
                                     String releasedStatus) {
}
