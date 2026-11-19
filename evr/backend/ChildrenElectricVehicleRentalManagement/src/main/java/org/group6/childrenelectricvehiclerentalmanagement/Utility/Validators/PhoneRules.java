package org.group6.childrenelectricvehiclerentalmanagement.Utility.Validators;

import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.ValidationException;

import java.util.regex.Pattern;

public final class PhoneRules {

    public static final int LENGTH = 10;

    private static final Pattern SEPARATORS = Pattern.compile("[\\s.\\-]");
    private static final Pattern DIGITS = Pattern.compile("^\\d+$");
    private static final Pattern VALID = Pattern.compile("^0[35789]\\d{8}$");

    private PhoneRules() {
    }

    public static String normalize(String raw) {
        if (raw == null) {
            return "";
        }
        String value = SEPARATORS.matcher(raw).replaceAll("");
        if (value.startsWith("+84")) {
            return "0" + value.substring(3);
        }
        if (value.startsWith("84") && value.length() == LENGTH + 1) {
            return "0" + value.substring(2);
        }
        return value;
    }

    public static String check(String raw) throws ValidationException {
        String value = normalize(raw);
        if (value.isEmpty()) {
            throw new ValidationException("phone", "Số điện thoại không được để trống");
        }
        if (!DIGITS.matcher(value).matches()) {
            throw new ValidationException("phone", "Số điện thoại chỉ được chứa chữ số");
        }
        if (value.length() != LENGTH) {
            throw new ValidationException("phone", "Số điện thoại phải gồm đúng " + LENGTH + " chữ số");
        }
        if (!VALID.matcher(value).matches()) {
            throw new ValidationException("phone", "Số điện thoại phải bắt đầu bằng 03, 05, 07, 08 hoặc 09");
        }
        return value;
    }
}
