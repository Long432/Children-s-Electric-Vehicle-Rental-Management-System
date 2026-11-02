package org.group6.childrenelectricvehiclerentalmanagement.Utility;

import org.group6.childrenelectricvehiclerentalmanagement.Enum.TicketStatus;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.ValidationException;

public final class TicketStatusValidator {
    private TicketStatusValidator() {}

    public static TicketStatus validateOptional(String raw) throws ValidationException {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        for (TicketStatus status : TicketStatus.values()) {
            if (status.name().equalsIgnoreCase(raw.trim())) {
                return status;
            }
        }
        throw new ValidationException("ticket_status","status không hợp lệ (Waiting, Inused, Overdue, Canceled, Completed)");
    }
}
