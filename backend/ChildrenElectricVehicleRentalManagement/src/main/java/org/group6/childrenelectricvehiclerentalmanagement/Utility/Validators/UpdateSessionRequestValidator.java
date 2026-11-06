package org.group6.childrenelectricvehiclerentalmanagement.Utility.Validators;

import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Session.UpdateSessionRequest;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.ValidationException;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.RequestValidator;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.SessionTimeRules;

public class UpdateSessionRequestValidator implements RequestValidator<UpdateSessionRequest> {

    @Override
    public void validate(UpdateSessionRequest request) throws ValidationException {
        if (request == null) {
            throw new ValidationException("body", "Thiếu dữ liệu gửi lên");
        }
        if (request.date() == null && request.time() == null) {
            throw new ValidationException("body", "Cần cung cấp ngày hoặc giờ cần sửa");
        }
        if (request.date() != null) {
            SessionTimeRules.parseDate("date", request.date());
        }
        if (request.time() != null) {
            SessionTimeRules.parseTime("time", request.time());
        }
    }
}
