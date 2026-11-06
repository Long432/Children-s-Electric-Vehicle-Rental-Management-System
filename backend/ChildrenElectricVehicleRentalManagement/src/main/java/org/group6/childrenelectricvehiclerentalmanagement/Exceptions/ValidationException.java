package org.group6.childrenelectricvehiclerentalmanagement.Exceptions;

/** Dữ liệu request không hợp lệ (HTTP 422). */
public class ValidationException extends AbstractException {
    private final String field;

    public ValidationException(String field, String userMessage) {
        super("validation failed on field '" + field + "': " + userMessage, userMessage, 422, "INVALID_INPUT");
        this.field = field;
    }

    public ValidationException(String field, String userMessage, String code) {
        super("validation failed on field '" + field + "': " + userMessage, userMessage, 422, code);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
