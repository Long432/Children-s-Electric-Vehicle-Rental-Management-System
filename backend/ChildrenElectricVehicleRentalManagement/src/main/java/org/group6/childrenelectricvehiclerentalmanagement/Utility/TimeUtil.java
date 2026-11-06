package org.group6.childrenelectricvehiclerentalmanagement.Utility;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

/** Mọi thời gian trong hệ thống dùng UTC (cột timestamp không múi giờ). */
public final class TimeUtil {
    private TimeUtil() {
    }

    public static LocalDateTime nowUtc() {
        return LocalDateTime.now(ZoneOffset.UTC);
    }

    /** Trả chuỗi ISO 8601 có hậu tố Z, ví dụ 2026-10-27T03:15:00Z. */
    public static String toIsoUtc(LocalDateTime value) {
        if (value == null) {
            return null;
        }
        return value.toInstant(ZoneOffset.UTC).toString();
    }
}
