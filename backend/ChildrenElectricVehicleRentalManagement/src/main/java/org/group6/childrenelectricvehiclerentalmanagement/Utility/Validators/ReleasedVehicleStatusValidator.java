package org.group6.childrenelectricvehiclerentalmanagement.Utility.Validators;

import org.group6.childrenelectricvehiclerentalmanagement.Enum.ReleasedVehicleStatus;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.ValidationException;

public final class ReleasedVehicleStatusValidator {
    private ReleasedVehicleStatusValidator() {}

    public static ReleasedVehicleStatus validate(String raw) throws ValidationException {
        if (raw == null || raw.isBlank()) {
            throw new ValidationException("old_vehicle_status","old_vehicle_status không được để trống (Online hoặc Repairing)");
        }
        for (ReleasedVehicleStatus status : ReleasedVehicleStatus.values()) {
            if (status.name().equalsIgnoreCase(raw.trim())) {
                return status;
            }
        }
        throw new ValidationException("old_vehicle_status","old_vehicle_status chỉ nhận Online hoặc Repairing");
    }
}
