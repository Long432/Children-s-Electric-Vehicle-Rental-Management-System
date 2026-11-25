package org.group6.childrenelectricvehiclerentalmanagement.DTOs.Ticket;

import java.util.List;

public record AssignGroupResponse(long group_id, List<TicketResponse> tickets) {
}
