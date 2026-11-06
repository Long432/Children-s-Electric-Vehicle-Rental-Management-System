package org.group6.childrenelectricvehiclerentalmanagement.Utility;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

@Component
public class BusinessTime {

    private final Clock clock;
    private final ZoneId zone;

    public BusinessTime(Clock clock, @Value("${app.business-zone:Asia/Ho_Chi_Minh}") String zone) {
        this.clock = clock;
        this.zone = ZoneId.of(zone);
    }

    public LocalDateTime nowUtc() {
        return LocalDateTime.ofInstant(clock.instant(), ZoneOffset.UTC);
    }

    public LocalDate today() {
        return LocalDate.ofInstant(clock.instant(), zone);
    }

    public LocalTime nowLocalTime() {
        return LocalTime.ofInstant(clock.instant(), zone).truncatedTo(ChronoUnit.MINUTES);
    }

    public LocalDateTime nextOccurrenceUtc(LocalTime time) {
        ZonedDateTime now = clock.instant().atZone(zone);
        ZonedDateTime candidate = now.toLocalDate().atTime(time).atZone(zone);
        if (!candidate.isAfter(now)) {
            candidate = now.toLocalDate().plusDays(1).atTime(time).atZone(zone);
        }
        return LocalDateTime.ofInstant(candidate.toInstant(), ZoneOffset.UTC);
    }

    public LocalDateTime defaultSessionEndUtc() {
        return nextOccurrenceUtc(LocalTime.MIDNIGHT);
    }
}
