package org.group6.childrenelectricvehiclerentalmanagement.Repositories;

import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Report.IncidentRow;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Report.ParticipantRow;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Report.TimelineEvent;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Report.VehicleReportRow;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface ReportQueries {

    record SessionRow(
            long id,
            LocalDate scheduledDate,
            String status,
            LocalDateTime startedAt,
            LocalDateTime endedAt,
            long createdById,
            String createdByName,
            long startedById,
            String startedByName,
            Long endedById,
            String endedByName,
            long ticketCount,
            long canceledCount,
            long cash,
            long bank,
            long overdueFee,
            long refundTotal,
            long incidentCount) {
    }

    record UserRow(long id, String username, String role, String status) {
    }

    List<SessionRow> sessions(LocalDate from, LocalDate to, Long startedBy, Long sessionId);

    List<UserRow> users(Long onlyUserId);

    List<ParticipantRow> participants(long sessionId);

    List<IncidentRow> incidents(LocalDateTime fromUtc, LocalDateTime toUtc, Long scopeUserId, Long vehicleId, Long sessionId, int limit);

    List<TimelineEvent> timeline(long sessionId, int limit);

    List<VehicleReportRow> vehicles(LocalDateTime fromUtc, LocalDateTime toUtc, Long scopeUserId);
}
