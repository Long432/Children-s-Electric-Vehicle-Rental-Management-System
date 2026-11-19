package org.group6.childrenelectricvehiclerentalmanagement.DTOs.VehicleController;

import org.group6.childrenelectricvehiclerentalmanagement.Enum.VehicleStatus;
import org.group6.childrenelectricvehiclerentalmanagement.Models.Vehicle;

import java.time.LocalDateTime;

public record vehicleInfoRespond(long id, String vehicle_slug, VehicleStatus vehicle_status, LocalDateTime created_at, LocalDateTime updated_at) {

    public vehicleInfoRespond(Vehicle input) {
        this(input.getId(), input.getSlug(), input.getStatus(), input.getCreatedAt(), input.getUpdatedAt());
    }
}
