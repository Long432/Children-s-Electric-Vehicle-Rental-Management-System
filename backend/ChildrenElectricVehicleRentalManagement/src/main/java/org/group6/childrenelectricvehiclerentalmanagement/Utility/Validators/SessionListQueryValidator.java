package org.group6.childrenelectricvehiclerentalmanagement.Utility.Validators;

import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Session.SessionListQuery;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.SessionStatus;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.ValidationException;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.RequestValidator;

public class SessionListQueryValidator implements RequestValidator<SessionListQuery> {

    @Override
    public void validate(SessionListQuery query) throws ValidationException {
        if (query == null) {
            throw new ValidationException("query", "Thiếu tham số truy vấn");
        }
        if (query.status() != null && !query.status().isBlank()) {
            try {
                SessionStatus.valueOf(query.status().trim());
            } catch (IllegalArgumentException e) {
                throw new ValidationException("status", "Trạng thái phiên không hợp lệ");
            }
        }
        PageRules.check(query.page(), query.limit());
    }
}
