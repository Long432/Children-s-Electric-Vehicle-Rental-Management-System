package org.group6.childrenelectricvehiclerentalmanagement.Utility;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public final class TimeUtil {
    private TimeUtil() {}

    public static LocalDateTime now() {
        return LocalDateTime.now(ZoneOffset.UTC);
    }

    public static String iso(LocalDateTime value) {
        return value == null ? null : value.toInstant(ZoneOffset.UTC).toString();
    }
}
