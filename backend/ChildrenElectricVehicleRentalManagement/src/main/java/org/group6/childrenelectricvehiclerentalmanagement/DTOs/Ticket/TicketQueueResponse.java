package org.group6.childrenelectricvehiclerentalmanagement.DTOs.Ticket;

import java.util.List;

public record TicketQueueResponse(Long session_id, int total, List<QueueTicketResponse> items) {
}
