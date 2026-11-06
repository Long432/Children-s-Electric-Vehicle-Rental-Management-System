package org.group6.childrenelectricvehiclerentalmanagement.DTOs.Ticket;

import org.group6.childrenelectricvehiclerentalmanagement.Enum.VehicleStatus;
import org.group6.childrenelectricvehiclerentalmanagement.Models.Vehicle;

public record TicketVehicleResponse(long id, String slug, VehicleStatus status) {

    public static TicketVehicleResponse from(Vehicle vehicle) {
        if (vehicle == null) {
            return null;
        }
        return new TicketVehicleResponse(vehicle.getId(), vehicle.getSlug(), vehicle.getStatus());
    }
}
