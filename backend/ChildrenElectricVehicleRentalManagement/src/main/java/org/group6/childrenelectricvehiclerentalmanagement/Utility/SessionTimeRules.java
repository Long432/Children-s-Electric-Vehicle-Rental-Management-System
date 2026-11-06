package org.group6.childrenelectricvehiclerentalmanagement.Utility;

import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.ValidationException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public final class SessionTimeRules {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private SessionTimeRules() {
    }

    public static LocalDate parseDate(String field, String value) throws ValidationException {
        if (value == null || value.isBlank()) {
            throw new ValidationException(field, "Ngày không được để trống");
        }
        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException e) {
            throw new ValidationException(field, "Ngày phải có dạng yyyy-MM-dd");
        }
    }

    public static LocalTime parseTime(String field, String value) throws ValidationException {
        if (value == null || value.isBlank()) {
            throw new ValidationException(field, "Giờ không được để trống");
        }
        try {
            return LocalTime.parse(value.trim(), TIME_FORMAT);
        } catch (DateTimeParseException e) {
            throw new ValidationException(field, "Giờ phải có dạng HH:mm");
        }
    }
}
