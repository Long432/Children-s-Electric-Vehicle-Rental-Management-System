package org.group6.childrenelectricvehiclerentalmanagement.Utility;

import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.ValidationException;

public final class MinutesValidator {
    public static final int MAX_MINUTES = 1440;

    private MinutesValidator() {}

    public static int validatePositive(String field, String raw) throws ValidationException {
        return parse(field, raw, 1);
    }

    public static int validateNonNegative(String field, String raw) throws ValidationException {
        return parse(field, raw, 0);
    }

    private static int parse(String field, String raw, int min) throws ValidationException {
        if (raw == null || raw.isBlank()) {
            throw new ValidationException(field, field + " không được để trống");
        }
        int value;
        try {
            value = Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            throw new ValidationException(field, field + " phải là số nguyên");
        }
        if (value < min || value > MAX_MINUTES) {
            throw new ValidationException(field, field + " phải nằm trong khoảng " + min + " đến " + MAX_MINUTES + " phút");
        }
        return value;
    }
}
