package org.group6.childrenelectricvehiclerentalmanagement.Utility.Validators;

import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Session.CreateSessionRequest;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.ValidationException;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.RequestValidator;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.SessionTimeRules;

public class CreateSessionRequestValidator implements RequestValidator<CreateSessionRequest> {

    @Override
    public void validate(CreateSessionRequest request) throws ValidationException {
        if (request == null) {
            throw new ValidationException("body", "Thiếu dữ liệu gửi lên");
        }
        SessionTimeRules.parseDate("date", request.date());
        SessionTimeRules.parseTime("time", request.time());
    }
}
