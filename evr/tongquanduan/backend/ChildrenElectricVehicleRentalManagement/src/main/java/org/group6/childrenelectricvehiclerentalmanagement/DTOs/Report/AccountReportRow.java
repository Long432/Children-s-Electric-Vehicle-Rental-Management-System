package org.group6.childrenelectricvehiclerentalmanagement.DTOs.Report;

public record AccountReportRow(
        long user_id,
        String username,
        String role,
        String status,
        long session_count,
        long work_minutes,
        long ticket_count,
        long canceled_ticket_count,
        long cash_revenue,
        long bank_revenue,
        long total_revenue,
        long overdue_fee,
        long refund_total,
        long incident_count,
        long revenue_per_hour) {
}
