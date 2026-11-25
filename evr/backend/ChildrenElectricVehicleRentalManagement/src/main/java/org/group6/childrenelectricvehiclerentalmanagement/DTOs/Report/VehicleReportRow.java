package org.group6.childrenelectricvehiclerentalmanagement.DTOs.Report;

public record VehicleReportRow(
        long vehicle_id,
        String slug,
        String status,
        long incident_count,
        String last_incident_at) {
}
