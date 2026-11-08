package org.group6.childrenelectricvehiclerentalmanagement.DTOs.SessionController;

import org.group6.childrenelectricvehiclerentalmanagement.DTOs.VehicleController.vehicleInfoRespond;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.SessionStatus;
import org.group6.childrenelectricvehiclerentalmanagement.Models.Session;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public record SessionDetailResponse(
        Long id,
        SessionStatus session_status,
        LocalDate scheduled_date,
        LocalTime scheduled_time,
        LocalDateTime planned_end_at,
        LocalDateTime started_at,
        LocalDateTime ended_at,
        Boolean is_instant,
        Integer next_ticket_no,
        List<vehicleInfoRespond> vehicles
) {
    public SessionDetailResponse(Session s, List<vehicleInfoRespond> vehicles) {
        this(s.getId(), s.getStatus(), s.getScheduledDate(), s.getScheduledTime(), s.getPlannedEndAt(),
                s.getStartedAt(), s.getEndedAt(), s.getIsInstant(), s.getNextTicketNo(), vehicles);
    }
}
