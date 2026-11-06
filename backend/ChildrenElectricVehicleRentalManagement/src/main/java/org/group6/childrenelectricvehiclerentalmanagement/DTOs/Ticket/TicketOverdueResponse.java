package org.group6.childrenelectricvehiclerentalmanagement.DTOs.Ticket;

import java.util.List;

public record TicketOverdueResponse(Long session_id, int total, List<OverdueTicketResponse> items) {
}
