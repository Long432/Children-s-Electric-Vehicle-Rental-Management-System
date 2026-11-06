package org.group6.childrenelectricvehiclerentalmanagement.DTOs.Vehicle;

import org.group6.childrenelectricvehiclerentalmanagement.Enum.VehicleStatus;
import org.group6.childrenelectricvehiclerentalmanagement.Models.Vehicle;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.TimeUtil;

public record VehicleResponse(long id, String slug, VehicleStatus status, String created_at, String updated_at) {

    public static VehicleResponse from(Vehicle vehicle) {
        return new VehicleResponse(
                vehicle.getId(),
                vehicle.getSlug(),
                vehicle.getStatus(),
                TimeUtil.toIsoUtc(vehicle.getCreatedAt()),
                TimeUtil.toIsoUtc(vehicle.getUpdatedAt()));
    }
}
