package org.group6.childrenelectricvehiclerentalmanagement.Utility.Validators;

import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.ValidationException;

public final class BooleanValidator {
    private BooleanValidator() {}

    public static boolean validate(String field, String raw) throws ValidationException {
        if (raw == null || raw.isBlank()) {
            throw new ValidationException(field,field + " không được để trống");
        }
        String value = raw.trim();
        if (value.equalsIgnoreCase("true")) {
            return true;
        }
        if (value.equalsIgnoreCase("false")) {
            return false;
        }
        throw new ValidationException(field,field + " chỉ nhận true hoặc false");
    }
}
