package org.group6.childrenelectricvehiclerentalmanagement.Utility.Validators;

import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Ticket.AssignGroupRequest;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.ValidationException;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.RequestValidator;

import java.util.HashSet;
import java.util.Set;

public class AssignGroupRequestValidator implements RequestValidator<AssignGroupRequest> {

    @Override
    public void validate(AssignGroupRequest request) throws ValidationException {
        if (request == null) {
            throw new ValidationException("body", "Thiếu dữ liệu gửi lên");
        }
        if (request.vehicle_slugs() == null || request.vehicle_slugs().isEmpty()) {
            throw new ValidationException("vehicle_slugs", "Cần chọn ít nhất một xe");
        }
        if (request.vehicle_slugs().size() > CreateTicketRequestValidator.MAX_QUANTITY) {
            throw new ValidationException("vehicle_slugs", "Mỗi nhóm tối đa " + CreateTicketRequestValidator.MAX_QUANTITY + " xe");
        }
        Set<String> seen = new HashSet<>();
        for (String slug : request.vehicle_slugs()) {
            try {
                VehicleSlugRules.check(slug);
            } catch (ValidationException e) {
                throw new ValidationException("vehicle_slugs", e.getUserMessage());
            }
            if (!seen.add(slug.trim())) {
                throw new ValidationException("vehicle_slugs", "Danh sách xe bị lặp: " + slug.trim());
            }
        }
    }
}
