package org.group6.childrenelectricvehiclerentalmanagement.Utility.Validators;

import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Vehicle.UpdateVehicleSlugRequest;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.ValidationException;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.RequestValidator;

public class UpdateVehicleSlugRequestValidator implements RequestValidator<UpdateVehicleSlugRequest> {

    @Override
    public void validate(UpdateVehicleSlugRequest request) throws ValidationException {
        if (request == null) {
            throw new ValidationException("body", "Thiếu dữ liệu gửi lên");
        }
        VehicleSlugRules.check(request.slug());
    }
}
