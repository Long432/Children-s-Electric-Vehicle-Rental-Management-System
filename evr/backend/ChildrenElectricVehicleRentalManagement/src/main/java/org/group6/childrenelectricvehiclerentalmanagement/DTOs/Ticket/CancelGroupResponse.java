package org.group6.childrenelectricvehiclerentalmanagement.DTOs.Ticket;

import java.util.List;

public record CancelGroupResponse(long group_id, int canceled_count, int refund_amount, List<CancelTicketResponse> tickets) {
}
