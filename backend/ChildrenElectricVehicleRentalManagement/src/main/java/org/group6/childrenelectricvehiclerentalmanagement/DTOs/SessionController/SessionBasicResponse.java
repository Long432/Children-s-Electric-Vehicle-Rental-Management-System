package org.group6.childrenelectricvehiclerentalmanagement.DTOs.SessionController;

import org.group6.childrenelectricvehiclerentalmanagement.Enum.SessionStatus;
import org.group6.childrenelectricvehiclerentalmanagement.Models.Session;

import java.time.LocalDateTime;

public record SessionBasicResponse (Long id, SessionStatus session_status, LocalDateTime sheduled_date,LocalDateTime planned_end_date,Integer next_ticket_no) {
    public SessionBasicResponse(Session session) {
        this(session.getId(), session.getStatus(), LocalDateTime.of(session.getScheduledDate(), session.getScheduledTime()), session.getPlannedEndAt(), session.getNextTicketNo());
    }
}
