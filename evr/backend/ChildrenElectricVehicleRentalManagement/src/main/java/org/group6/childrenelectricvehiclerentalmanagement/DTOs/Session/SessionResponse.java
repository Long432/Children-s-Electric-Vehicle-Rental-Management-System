package org.group6.childrenelectricvehiclerentalmanagement.DTOs.Session;

import org.group6.childrenelectricvehiclerentalmanagement.Enum.SessionStatus;
import org.group6.childrenelectricvehiclerentalmanagement.Models.Session;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.TimeUtil;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record SessionResponse(
        long id,
        SessionStatus status,
        String scheduled_date,
        String scheduled_time,
        String scheduled_at,
        String planned_end_at,
        String started_at,
        String ended_at,
        boolean is_instant,
        boolean past_planned_end,
        int next_ticket_no,
        String created_at,
        String updated_at) {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    public static SessionResponse from(Session session, LocalDateTime scheduledAt, boolean pastPlannedEnd) {
        return new SessionResponse(
                session.getId(),
                session.getStatus(),
                session.getScheduledDate().toString(),
                session.getScheduledTime().format(TIME_FORMAT),
                TimeUtil.toIsoUtc(scheduledAt),
                TimeUtil.toIsoUtc(session.getPlannedEndAt()),
                TimeUtil.toIsoUtc(session.getStartedAt()),
                TimeUtil.toIsoUtc(session.getEndedAt()),
                session.getIsInstant(),
                pastPlannedEnd,
                session.getNextTicketNo(),
                TimeUtil.toIsoUtc(session.getCreatedAt()),
                TimeUtil.toIsoUtc(session.getUpdatedAt()));
    }
}
