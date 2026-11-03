package org.group6.childrenelectricvehiclerentalmanagement.DTOs.TicketController;

import java.util.List;

public record CreateTicketResponse(TicketResponse ticket, List<VehicleBrief> suggestedVehicles) {
}
