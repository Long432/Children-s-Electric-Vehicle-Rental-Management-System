package org.group6.childrenelectricvehiclerentalmanagement.DTOs.Report;

public record ParticipantRow(
        long user_id,
        String username,
        long tickets_created,
        long tickets_returned,
        long tickets_canceled,
        long cash_revenue,
        long bank_revenue,
        long total_revenue) {
}
