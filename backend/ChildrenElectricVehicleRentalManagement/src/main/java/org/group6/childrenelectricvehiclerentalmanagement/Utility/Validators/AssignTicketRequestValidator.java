package org.group6.childrenelectricvehiclerentalmanagement.Utility.Validators;

import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Ticket.AssignTicketRequest;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.ValidationException;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.RequestValidator;

public class AssignTicketRequestValidator implements RequestValidator<AssignTicketRequest> {

    @Override
    public void validate(AssignTicketRequest request) throws ValidationException {
        if (request == null) {
            throw new ValidationException("body", "Thiếu dữ liệu gửi lên");
        }
        if (request.vehicle_slug() == null || request.vehicle_slug().isBlank()) {
            throw new ValidationException("vehicle_slug", "Tên nhận diện xe không được để trống");
        }
        try {
            VehicleSlugRules.check(request.vehicle_slug());
        } catch (ValidationException e) {
            throw new ValidationException("vehicle_slug", e.getUserMessage());
        }
    }
}
