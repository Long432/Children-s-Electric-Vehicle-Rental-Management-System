package org.group6.childrenelectricvehiclerentalmanagement.DTOs.Long;

import java.util.List;
import java.util.Map;

public final class Responses {
    private Responses() {}

    public record ApiSuccess(boolean ok, Object data) {}
    public record ErrorBody(String code, String message) {}
    public record ApiFailure(boolean ok, ErrorBody error) {}
    public record PageResponse<T>(List<T> items, int page, int limit, long total, int totalPages, Long sessionId) {}

    public record PackageResponse(Long id, Integer amount, Integer minutes, Boolean isDefault) {}
    public record SettingsResponse(Boolean refundEnabled, Boolean extendEnabled, Integer overdueFeePerMinute,
                                   Integer overdueGraceMinutes, Integer minimumMinutes,
                                   List<PackageResponse> packages) {}

    public record VehicleBrief(Long id, String slug, String status) {}
    public record TicketResponse(Long id, Integer ticketNo, String phone, String status, Long sessionId,
                                 VehicleBrief vehicle, String paymentMethod, Integer totalMinutes,
                                 Integer amountPaid, String queuedAt, String startedAt, String dueAt,
                                 String returnedAt, Integer remainingMinutes, Integer overdueMinutes,
                                 Integer chargeableOverdueMinutes, Integer overdueFee, Integer refundAmount,
                                 String cancelableAt) {}
    public record CreateTicketResponse(TicketResponse ticket, List<VehicleBrief> suggestedVehicles) {}
    public record TicketListResponse(List<TicketResponse> items, int total, Long sessionId) {}
    public record PaymentResponse(Long id, String type, String method, Integer amount, Integer minutes,
                                  String createdAt) {}
    public record VehicleHistoryResponse(VehicleBrief vehicle, String assignedAt, String releasedAt,
                                         String releasedStatus) {}
    public record TicketDetailResponse(TicketResponse ticket, List<PaymentResponse> payments,
                                       List<VehicleHistoryResponse> vehicleHistory) {}
    public record CancelTicketResponse(Long id, Integer ticketNo, String status, Integer refundAmount,
                                       String refundMethod) {}
    public record ChangeVehicleResponse(TicketResponse ticket, VehicleBrief oldVehicle) {}
    public record ExtendTicketResponse(TicketResponse ticket, Integer extendedMinutes, Integer extensionAmount) {}
    public record NextTicket(Integer ticketNo, String phone) {}
    public record ReturnTicketResponse(Long id, Integer ticketNo, String status, Integer overdueMinutes,
                                       Integer chargeableOverdueMinutes, Integer overdueFee,
                                       Integer refundAmount, VehicleBrief vehicle, NextTicket nextTicket) {}

    public record SessionBrief(Long id, String status, String scheduledDate, String scheduledTime,
                               String plannedEndAt, String startedAt, String endedAt, Boolean isInstant) {}
    public record TicketStats(int total, Map<String, Integer> byStatus, long totalMinutesSold) {}
    public record MethodRevenue(long collected, long refunded, long net) {}
    public record RevenueStats(long ticketAmount, long extensionAmount, long overdueFeeAmount, long refundAmount,
                               long gross, long net, Map<String, MethodRevenue> byMethod) {}
    public record VehicleStats(int total, Map<String, Integer> byStatus) {}
    public record SessionStatsResponse(SessionBrief session, TicketStats tickets, RevenueStats revenue,
                                       VehicleStats vehicles) {}
}
