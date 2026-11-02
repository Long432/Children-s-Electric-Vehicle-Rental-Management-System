package org.group6.childrenelectricvehiclerentalmanagement.Utility;

import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.ValidationException;

public final class PageValidator {
    public static final int DEFAULT_LIMIT = 20;
    public static final int MAX_LIMIT = 100;

    private PageValidator() {}

    public static PageParams validate(String rawPage, String rawLimit) throws ValidationException {
        int page = parse("page", rawPage, 1, 1, Integer.MAX_VALUE);
        int limit = parse("limit", rawLimit, DEFAULT_LIMIT, 1, MAX_LIMIT);
        return new PageParams(page, limit);
    }

    private static int parse(String field, String raw, int defaultValue, int min, int max) throws ValidationException {
        if (raw == null || raw.isBlank()) {
            return defaultValue;
        }
        int value;
        try {
            value = Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            throw new ValidationException(field, field + " phải là số nguyên");
        }
        if (value < min || value > max) {
            throw new ValidationException(field, field + " phải nằm trong khoảng " + min + " đến " + max);
        }
        return value;
    }
}
