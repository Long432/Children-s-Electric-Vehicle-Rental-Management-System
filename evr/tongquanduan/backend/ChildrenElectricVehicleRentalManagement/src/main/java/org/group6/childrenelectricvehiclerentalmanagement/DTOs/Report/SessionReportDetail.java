package org.group6.childrenelectricvehiclerentalmanagement.DTOs.Report;

import java.util.List;

public record SessionReportDetail(
        SessionReportRow session,
        List<ParticipantRow> participants,
        List<IncidentRow> incidents,
        List<TimelineEvent> timeline) {
}
