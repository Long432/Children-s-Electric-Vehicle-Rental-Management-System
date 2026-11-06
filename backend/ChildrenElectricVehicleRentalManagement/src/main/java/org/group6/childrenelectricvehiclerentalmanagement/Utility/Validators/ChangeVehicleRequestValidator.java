package org.group6.childrenelectricvehiclerentalmanagement.Utility.Validators;

import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Ticket.ChangeVehicleRequest;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.ValidationException;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.RequestValidator;

import java.util.Set;

public class ChangeVehicleRequestValidator implements RequestValidator<ChangeVehicleRequest> {

    private static final Set<String> RELEASED_STATUSES = Set.of("Online", "Repairing");

    @Override
    public void validate(ChangeVehicleRequest request) throws ValidationException {
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
        if (request.old_vehicle_status() == null || !RELEASED_STATUSES.contains(request.old_vehicle_status().trim())) {
            throw new ValidationException("old_vehicle_status", "Trạng thái xe cũ chỉ nhận Online hoặc Repairing");
        }
    }
}
