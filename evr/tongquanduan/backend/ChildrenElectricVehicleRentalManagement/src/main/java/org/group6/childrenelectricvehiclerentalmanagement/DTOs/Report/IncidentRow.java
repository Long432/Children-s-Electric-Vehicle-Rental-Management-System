package org.group6.childrenelectricvehiclerentalmanagement.DTOs.Report;

public record IncidentRow(
        long id,
        String created_at,
        long vehicle_id,
        String vehicle_slug,
        Long session_id,
        Long ticket_id,
        Integer ticket_no,
        String source,
        UserRef reported_by,
        UserRef session_in_charge) {
}
