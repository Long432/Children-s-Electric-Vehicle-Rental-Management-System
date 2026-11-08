package org.group6.childrenelectricvehiclerentalmanagement.Utility.Validators;

import org.group6.childrenelectricvehiclerentalmanagement.Enum.StatsGroup;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.ValidationException;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

public final class PackageStatsValidator {
    public static final int DEFAULT_DAYS = 30;
    public static final int DEFAULT_MONTHS = 12;
    public static final int MAX_DAYS = 366;
    public static final int MAX_MONTHS = 60;

    private PackageStatsValidator() {}

    public record Query(StatsGroup group, LocalDate from, LocalDate to) {}

    public static Query validate(String rawGroup, String rawFrom, String rawTo, LocalDate today) throws ValidationException {
        StatsGroup group = parseGroup(rawGroup);
        LocalDate from = parseDate("from", rawFrom);
        LocalDate to = parseDate("to", rawTo);
        if (to == null) {
            to = today;
        }
        if (from == null) {
            from = group == StatsGroup.Day
                    ? to.minusDays(DEFAULT_DAYS - 1L)
                    : to.withDayOfMonth(1).minusMonths(DEFAULT_MONTHS - 1L);
        }
        if (from.isAfter(to)) {
            throw new ValidationException("from", "from không được sau to");
        }
        if (group == StatsGroup.Day) {
            long days = ChronoUnit.DAYS.between(from, to) + 1;
            if (days > MAX_DAYS) {
                throw new ValidationException("from", "Khoảng thống kê theo ngày tối đa " + MAX_DAYS + " ngày");
            }
            return new Query(group, from, to);
        }
        LocalDate firstDay = from.withDayOfMonth(1);
        LocalDate lastDay = to.withDayOfMonth(to.lengthOfMonth());
        long months = ChronoUnit.MONTHS.between(YearMonth.from(firstDay), YearMonth.from(lastDay)) + 1;
        if (months > MAX_MONTHS) {
            throw new ValidationException("from", "Khoảng thống kê theo tháng tối đa " + MAX_MONTHS + " tháng");
        }
        return new Query(group, firstDay, lastDay);
    }

    private static StatsGroup parseGroup(String raw) throws ValidationException {
        if (raw == null || raw.isBlank()) {
            return StatsGroup.Day;
        }
        for (StatsGroup group : StatsGroup.values()) {
            if (group.name().equalsIgnoreCase(raw.trim())) {
                return group;
            }
        }
        throw new ValidationException("group_by", "group_by chỉ nhận day hoặc month");
    }

    private static LocalDate parseDate(String field, String raw) throws ValidationException {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(raw.trim());
        } catch (DateTimeParseException e) {
            throw new ValidationException(field, field + " phải theo định dạng yyyy-MM-dd");
        }
    }
}
