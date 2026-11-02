package org.group6.childrenelectricvehiclerentalmanagement.Utility;

import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.ValidationException;
import java.util.regex.Pattern;

public final class SearchQueryValidator {
    private static final Pattern PATTERN = Pattern.compile("^\\+?\\d{1,13}$");

    private SearchQueryValidator() {}

    public static String validate(String raw) throws ValidationException {
        if (raw == null || raw.isBlank()) {
            throw new ValidationException("Query","q không được để trống");
        }
        String value = raw.trim();
        if (!PATTERN.matcher(value).matches()) {
            throw new ValidationException("Query","q chỉ gồm chữ số (số điện thoại hoặc mã vé)");
        }
        return value;
    }
}
