package org.group6.childrenelectricvehiclerentalmanagement.Utility.Validators;

import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Ticket.TicketListQuery;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.TicketStatus;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.ValidationException;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.RequestValidator;

public class TicketListQueryValidator implements RequestValidator<TicketListQuery> {

    @Override
    public void validate(TicketListQuery query) throws ValidationException {
        if (query == null) {
            throw new ValidationException("query", "Thiếu tham số truy vấn");
        }
        if (query.status() != null && !query.status().isBlank()) {
            try {
                TicketStatus.valueOf(query.status().trim());
            } catch (IllegalArgumentException e) {
                throw new ValidationException("status", "Trạng thái vé không hợp lệ");
            }
        }
        if (query.session_id() != null && query.session_id() < 1) {
            throw new ValidationException("session_id", "Mã phiên không hợp lệ");
        }
        PageRules.check(query.page(), query.limit());
    }
}
