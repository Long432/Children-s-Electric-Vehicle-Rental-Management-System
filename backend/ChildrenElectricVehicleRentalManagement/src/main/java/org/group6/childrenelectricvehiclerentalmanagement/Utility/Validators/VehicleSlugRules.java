package org.group6.childrenelectricvehiclerentalmanagement.Utility.Validators;

import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.ValidationException;

import java.util.regex.Pattern;

public final class VehicleSlugRules {

    private static final int MAX_LENGTH = 50;
    private static final Pattern ALLOWED = Pattern.compile("^[a-z][a-z0-9]*(-[a-z0-9]+)*$");

    private VehicleSlugRules() {
    }

    public static void check(String slug) throws ValidationException {
        if (slug == null || slug.isBlank()) {
            throw new ValidationException("slug", "Tên nhận diện không được để trống");
        }
        String value = slug.trim();
        if (value.length() > MAX_LENGTH) {
            throw new ValidationException("slug", "Tên nhận diện tối đa " + MAX_LENGTH + " ký tự");
        }
        if (!ALLOWED.matcher(value).matches()) {
            throw new ValidationException("slug", "Tên nhận diện chỉ gồm chữ thường không dấu, số và dấu gạch ngang, bắt đầu bằng chữ cái, không có hai dấu gạch ngang liền nhau");
        }
    }
}
