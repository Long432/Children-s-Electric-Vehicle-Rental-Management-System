package org.group6.childrenelectricvehiclerentalmanagement.Exceptions;

import lombok.Getter;

/** Dữ liệu request không hợp lệ (HTTP 422). */
@Getter
public class ValidationException extends AbstractException {
    private final String field;

    public ValidationException(String field, String userMessage) {
        super("validation failed on field '" + field + "': " + userMessage, userMessage, 422);
        this.field = field;
    }

}
