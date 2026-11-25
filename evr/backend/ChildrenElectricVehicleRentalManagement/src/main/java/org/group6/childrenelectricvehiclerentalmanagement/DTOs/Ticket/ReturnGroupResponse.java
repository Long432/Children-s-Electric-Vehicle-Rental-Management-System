package org.group6.childrenelectricvehiclerentalmanagement.DTOs.Ticket;

import java.util.List;

public record ReturnGroupResponse(
        long group_id,
        int returned_count,
        int overdue_fee,
        int refund_amount,
        List<ReturnTicketResponse> tickets,
        NextTicketResponse next_ticket) {
}
