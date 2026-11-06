package org.group6.childrenelectricvehiclerentalmanagement.Utility.Validators;

import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Vehicle.CreateVehicleRequest;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.ValidationException;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.RequestValidator;

public class CreateVehicleRequestValidator implements RequestValidator<CreateVehicleRequest> {

    @Override
    public void validate(CreateVehicleRequest request) throws ValidationException {
        if (request == null) {
            throw new ValidationException("body", "Thiếu dữ liệu gửi lên");
        }
        VehicleSlugRules.check(request.slug());
    }
}
