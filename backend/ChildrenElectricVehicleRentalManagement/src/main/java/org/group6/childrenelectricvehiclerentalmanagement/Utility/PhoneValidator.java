package org.group6.childrenelectricvehiclerentalmanagement.Utility;

import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.ValidationException;
import java.util.regex.Pattern;

public final class PhoneValidator {
    private static final Pattern PATTERN = Pattern.compile("^(\\+84|0)\\d{9,10}$");

    private PhoneValidator() {}

    public static String validate(String raw) throws ValidationException {
        if (raw == null || raw.isBlank()) {
            throw new ValidationException("phone_number","Số điện thoại không được để trống");
        }
        String value = raw.trim().replace(" ", "");
        if (!PATTERN.matcher(value).matches()) {
            throw new ValidationException("phone_number","Số điện thoại không hợp lệ");
        }
        return value;
    }
}
