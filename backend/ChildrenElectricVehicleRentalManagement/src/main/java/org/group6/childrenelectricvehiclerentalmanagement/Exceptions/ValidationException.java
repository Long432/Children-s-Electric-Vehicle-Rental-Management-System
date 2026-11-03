package org.group6.childrenelectricvehiclerentalmanagement.Exceptions;

import lombok.Getter;

/** Dữ liệu request không hợp lệ (HTTP 422). */
@Getter
public class ValidationException extends AbstractException {
    private final String field;

    public ValidationException(String field, String userMessage) {
        super("Request field '" + field + "' is invalid: " + userMessage, "VALIDATION_ERROR", userMessage, 422);
        this.field = field;
    }

}
