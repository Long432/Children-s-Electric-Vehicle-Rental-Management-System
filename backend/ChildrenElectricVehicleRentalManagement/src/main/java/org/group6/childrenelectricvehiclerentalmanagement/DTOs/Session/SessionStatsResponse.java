package org.group6.childrenelectricvehiclerentalmanagement.DTOs.Session;

import org.group6.childrenelectricvehiclerentalmanagement.Enum.SessionStatus;

import java.util.Map;

public record SessionStatsResponse(
        long session_id,
        SessionStatus status,
        long ticket_count,
        Map<String, Long> ticket_counts,
        long cash_revenue,
        long bank_revenue,
        long total_revenue,
        long ticket_revenue,
        long overdue_fee,
        long refund_total) {
}
