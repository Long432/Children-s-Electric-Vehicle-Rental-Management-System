package org.group6.childrenelectricvehiclerentalmanagement.DTOs.Session;

import org.group6.childrenelectricvehiclerentalmanagement.Models.Session;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.TimeUtil;

public record SessionRevenueResponse(
        long session_id,
        String scheduled_date,
        String started_at,
        String ended_at,
        long ticket_count,
        long cash_revenue,
        long bank_revenue,
        long total_revenue,
        long overdue_fee,
        long refund_total) {

    public static SessionRevenueResponse from(Session session, long ticketCount, long cash, long bank, long overdueFee, long refundTotal) {
        return new SessionRevenueResponse(
                session.getId(),
                session.getScheduledDate().toString(),
                TimeUtil.toIsoUtc(session.getStartedAt()),
                TimeUtil.toIsoUtc(session.getEndedAt()),
                ticketCount,
                cash,
                bank,
                cash + bank,
                overdueFee,
                refundTotal);
    }
}
