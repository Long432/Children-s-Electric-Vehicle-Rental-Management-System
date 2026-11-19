package org.group6.childrenelectricvehiclerentalmanagement.Utility;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BusinessTimeTest {

    private BusinessTime at(String instant) {
        return new BusinessTime(Clock.fixed(Instant.parse(instant), ZoneOffset.UTC), "Asia/Ho_Chi_Minh");
    }

    @Test
    void defaultEndIsTodayAtCloseTime() {
        assertEquals(LocalDateTime.parse("2026-11-05T16:00:00"), at("2026-11-05T10:00:00Z").defaultSessionEndUtc());
    }

    @Test
    void defaultEndFollowsTheBusinessDayNotUtc() {
        assertEquals(LocalDateTime.parse("2026-11-05T16:00:00"), at("2026-11-04T17:30:00Z").defaultSessionEndUtc());
    }

    @Test
    void businessHoursStartAtSixAndStopBeforeTwentyThree() {
        BusinessTime time = at("2026-11-05T10:00:00Z");
        assertFalse(time.isWithinHours(LocalTime.of(5, 59)));
        assertTrue(time.isWithinHours(LocalTime.of(6, 0)));
        assertTrue(time.isWithinHours(LocalTime.of(22, 59)));
        assertFalse(time.isWithinHours(LocalTime.of(23, 0)));
        assertFalse(time.isWithinHours(LocalTime.MIDNIGHT));
    }

    @Test
    void timeAlreadyPassedTodayRollsToTomorrow() {
        assertEquals(LocalDateTime.parse("2026-11-06T02:00:00"), at("2026-11-05T10:00:00Z").nextOccurrenceUtc(LocalTime.of(9, 0)));
    }

    @Test
    void timeLaterTodayStaysToday() {
        assertEquals(LocalDateTime.parse("2026-11-05T15:30:00"), at("2026-11-05T10:00:00Z").nextOccurrenceUtc(LocalTime.of(22, 30)));
    }

    @Test
    void nowLocalIsTruncatedToTheMinuteInTheBusinessZone() {
        assertEquals(LocalDateTime.parse("2026-11-05T17:00:00"), at("2026-11-05T10:00:45Z").nowLocal());
    }

    @Test
    void toUtcConvertsABusinessLocalMomentToUtc() {
        assertEquals(LocalDateTime.parse("2026-11-06T01:30:00"),
                at("2026-11-05T10:00:00Z").toUtc(LocalDate.of(2026, 11, 6), LocalTime.of(8, 30)));
    }

    @Test
    void toUtcCrossesTheDateLineBackwardsForEarlyMorningTimes() {
        assertEquals(LocalDateTime.parse("2026-11-05T17:00:00"),
                at("2026-11-05T10:00:00Z").toUtc(LocalDate.of(2026, 11, 6), LocalTime.MIDNIGHT));
    }

    @Test
    void reachedIsInclusiveAndIgnoresMissingMoments() {
        BusinessTime time = at("2026-11-05T10:00:00Z");
        assertTrue(time.reached(LocalDateTime.parse("2026-11-05T10:00:00")));
        assertTrue(time.reached(LocalDateTime.parse("2026-11-05T09:59:59")));
        assertFalse(time.reached(LocalDateTime.parse("2026-11-05T10:00:01")));
        assertFalse(time.reached(null));
    }

    @Test
    void todayFollowsTheBusinessZoneNotUtc() {
        assertEquals(LocalDate.parse("2026-11-06"), at("2026-11-05T18:00:00Z").today());
    }
}
