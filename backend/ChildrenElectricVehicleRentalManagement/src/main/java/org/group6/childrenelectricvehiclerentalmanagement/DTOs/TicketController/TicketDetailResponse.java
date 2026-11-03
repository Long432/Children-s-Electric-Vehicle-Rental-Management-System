package org.group6.childrenelectricvehiclerentalmanagement.DTOs.TicketController;

import java.util.List;

public record TicketDetailResponse(TicketResponse ticket, List<PaymentResponse> payments,
                                   List<VehicleHistoryResponse> vehicleHistory) {
}
