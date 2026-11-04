package org.group6.childrenelectricvehiclerentalmanagement.Utility.Validators;

import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.ValidationException;
import java.util.regex.Pattern;

public final class VehicleSlugValidator {
    private static final Pattern PATTERN = Pattern.compile("^[a-z0-9]+(-[a-z0-9]+)*$");

    private VehicleSlugValidator() {}

    public static String validate(String raw) throws ValidationException {
        if (raw == null || raw.isBlank()) {
            throw new ValidationException("vehicle_slug","vehicle_slug không được để trống");
        }
        String value = raw.trim();
        if (value.length() > 100 || !PATTERN.matcher(value).matches()) {
            throw new ValidationException("vehicle_slug","vehicle_slug chỉ gồm chữ thường không dấu, số và dấu gạch ngang");
        }
        return value;
    }
}
