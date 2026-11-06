package org.group6.childrenelectricvehiclerentalmanagement.Utility.Validators;

import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Ticket.CancelTicketRequest;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.ValidationException;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.RequestValidator;

import java.util.Set;

public class CancelTicketRequestValidator implements RequestValidator<CancelTicketRequest> {

    private static final Set<String> PAYMENT_METHODS = Set.of("Cash", "Bank");

    @Override
    public void validate(CancelTicketRequest request) throws ValidationException {
        if (request == null || request.refund_method() == null) {
            return;
        }
        if (!PAYMENT_METHODS.contains(request.refund_method().trim())) {
            throw new ValidationException("refund_method", "Phương thức hoàn tiền chỉ nhận Cash hoặc Bank", "INVALID_PAYMENT_METHOD");
        }
    }
}
