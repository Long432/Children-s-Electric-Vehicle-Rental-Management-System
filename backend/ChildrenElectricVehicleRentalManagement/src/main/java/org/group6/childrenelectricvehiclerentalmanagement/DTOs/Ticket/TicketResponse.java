package org.group6.childrenelectricvehiclerentalmanagement.DTOs.Ticket;

import org.group6.childrenelectricvehiclerentalmanagement.Enum.PaymentMethod;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.TicketStatus;
import org.group6.childrenelectricvehiclerentalmanagement.Models.Ticket;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.TimeUtil;

public record TicketResponse(
        long id,
        long session_id,
        int ticket_no,
        String phone,
        TicketStatus status,
        PaymentMethod payment_method,
        int total_minutes,
        int amount_paid,
        int price_amount,
        int price_minutes,
        int overdue_fee_per_minute,
        int overdue_grace_minutes,
        boolean refund_enabled,
        String queued_at,
        String started_at,
        String due_at,
        String returned_at,
        int overdue_minutes,
        int overdue_fee,
        int refund_amount,
        String canceled_at,
        TicketVehicleResponse vehicle,
        String created_at,
        String updated_at) {

    public static TicketResponse from(Ticket ticket) {
        return new TicketResponse(
                ticket.getId(),
                ticket.getSessionId(),
                ticket.getTicketNo(),
                ticket.getPhone(),
                ticket.getStatus(),
                ticket.getPaymentMethod(),
                ticket.getTotalMinutes(),
                ticket.getAmountPaid(),
                ticket.getPriceAmount(),
                ticket.getPriceMinutes(),
                ticket.getOverdueFeePerMinute(),
                ticket.getOverdueGraceMinutes(),
                ticket.getRefundEnabled(),
                TimeUtil.toIsoUtc(ticket.getQueuedAt()),
                TimeUtil.toIsoUtc(ticket.getStartedAt()),
                TimeUtil.toIsoUtc(ticket.getDueAt()),
                TimeUtil.toIsoUtc(ticket.getReturnedAt()),
                ticket.getOverdueMinutes(),
                ticket.getOverdueFee(),
                ticket.getRefundAmount(),
                TimeUtil.toIsoUtc(ticket.getCanceledAt()),
                TicketVehicleResponse.from(ticket.getVehicle()),
                TimeUtil.toIsoUtc(ticket.getCreatedAt()),
                TimeUtil.toIsoUtc(ticket.getUpdatedAt()));
    }
}
