package org.group6.childrenelectricvehiclerentalmanagement.DTOs.TicketController;

import java.util.List;

public record TicketListResponse(List<TicketResponse> items, int total, Long sessionId) {
}
