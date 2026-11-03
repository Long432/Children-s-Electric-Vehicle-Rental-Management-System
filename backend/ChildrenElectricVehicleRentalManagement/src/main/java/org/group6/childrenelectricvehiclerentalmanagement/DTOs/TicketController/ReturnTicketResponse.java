package org.group6.childrenelectricvehiclerentalmanagement.DTOs.TicketController;

public record ReturnTicketResponse(Long id, Integer ticketNo, String status, Integer overdueMinutes,
                                   Integer chargeableOverdueMinutes, Integer overdueFee,
                                   Integer refundAmount, VehicleBrief vehicle, NextTicket nextTicket) {
}
