package org.group6.childrenelectricvehiclerentalmanagement.Utility;

import org.group6.childrenelectricvehiclerentalmanagement.DTOs.ToggleRequest;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.ValidationException;

public class ToggleRequestValidator implements RequestValidator<ToggleRequest> {
    @Override
    public void validate(ToggleRequest request) throws ValidationException {
        if (request == null) {
            throw new ValidationException("body", "Thiếu dữ liệu gửi lên");
        }
        if (request.getEnabled() == null) {
            throw new ValidationException("enabled", "Trường enabled là bắt buộc (true hoặc false)");
        }
    }
}
