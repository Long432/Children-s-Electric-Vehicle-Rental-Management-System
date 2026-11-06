package org.group6.childrenelectricvehiclerentalmanagement.Utility.Validators;

import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Session.StartSessionRequest;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.ValidationException;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.RequestValidator;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.SessionTimeRules;

import java.util.HashSet;
import java.util.Set;

public class StartSessionRequestValidator implements RequestValidator<StartSessionRequest> {

    private static final int MAX_VEHICLES = 200;

    @Override
    public void validate(StartSessionRequest request) throws ValidationException {
        if (request == null) {
            throw new ValidationException("body", "Thiếu dữ liệu gửi lên");
        }
        if (request.vehicle_slugs() == null || request.vehicle_slugs().isEmpty()) {
            throw new ValidationException("vehicle_slugs", "Cần chọn ít nhất một xe");
        }
        if (request.vehicle_slugs().size() > MAX_VEHICLES) {
            throw new ValidationException("vehicle_slugs", "Mỗi phiên tối đa " + MAX_VEHICLES + " xe");
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
        if (request.session_id() != null && request.session_id() < 1) {
            throw new ValidationException("session_id", "Mã phiên không hợp lệ");
        }
        if (request.end_time() != null && !request.end_time().isBlank()) {
            SessionTimeRules.parseTime("end_time", request.end_time());
        }
    }
}
