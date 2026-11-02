package org.group6.childrenelectricvehiclerentalmanagement.Utility;

import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.ValidationException;

public final class IdValidator {
    private IdValidator() {}

    public static Long validate(String field, String raw) throws ValidationException {
        if (raw == null || raw.isBlank()) {
            throw new ValidationException(field, field + " không được để trống");
        }
        long value;
        try {
            value = Long.parseLong(raw.trim());
        } catch (NumberFormatException e) {
            throw new ValidationException(field, field + " phải là số nguyên dương");
        }
        if (value <= 0) {
            throw new ValidationException(field, field + " phải là số nguyên dương");
        }
        return value;
    }

    public static Long validateOptional(String field, String raw) throws ValidationException {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return validate(field, raw);
    }
}
