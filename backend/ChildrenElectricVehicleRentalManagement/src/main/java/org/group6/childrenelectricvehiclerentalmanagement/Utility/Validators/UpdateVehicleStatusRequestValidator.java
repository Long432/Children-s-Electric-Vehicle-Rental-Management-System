package org.group6.childrenelectricvehiclerentalmanagement.Utility.Validators;

import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Vehicle.UpdateVehicleStatusRequest;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.ValidationException;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.RequestValidator;

import java.util.Set;

public class UpdateVehicleStatusRequestValidator implements RequestValidator<UpdateVehicleStatusRequest> {

    private static final Set<String> ALLOWED = Set.of("Online", "Repairing", "Offline");

    @Override
    public void validate(UpdateVehicleStatusRequest request) throws ValidationException {
        if (request == null) {
            throw new ValidationException("body", "Thiếu dữ liệu gửi lên");
        }
        if (request.status() == null || request.status().isBlank()) {
            throw new ValidationException("status", "Trạng thái không được để trống");
        }
        if (!ALLOWED.contains(request.status().trim())) {
            throw new ValidationException("status", "Chỉ được đổi sang trạng thái Online, Repairing hoặc Offline");
        }
    }
}
