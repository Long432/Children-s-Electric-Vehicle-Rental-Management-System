package org.group6.childrenelectricvehiclerentalmanagement.DTOs.TicketController;

public record TicketResponse(Long id, Integer ticketNo, String phone, String status, Long sessionId,
                             VehicleBrief vehicle, String paymentMethod, Integer totalMinutes,
                             Integer amountPaid, String queuedAt, String startedAt, String dueAt,
                             String returnedAt, Integer remainingMinutes, Integer overdueMinutes,
                             Integer chargeableOverdueMinutes, Integer overdueFee, Integer refundAmount) {
}
