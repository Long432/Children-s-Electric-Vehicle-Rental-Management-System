package org.group6.childrenelectricvehiclerentalmanagement.DTOs.Report;

public record DailyRevenue(
        String date,
        long session_count,
        long ticket_count,
        long cash_revenue,
        long bank_revenue,
        long total_revenue) {
}
