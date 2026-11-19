package org.group6.childrenelectricvehiclerentalmanagement.DTOs.Report;

public record SessionReportRow(
        long session_id,
        String scheduled_date,
        String status,
        String started_at,
        String ended_at,
        long work_minutes,
        UserRef in_charge,
        UserRef created_by,
        UserRef ended_by,
        long ticket_count,
        long canceled_ticket_count,
        long cash_revenue,
        long bank_revenue,
        long total_revenue,
        long overdue_fee,
        long refund_total,
        long incident_count) {
}
