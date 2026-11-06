package org.group6.childrenelectricvehiclerentalmanagement.Utility.Validators;

import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Ticket.ReturnTicketRequest;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.ValidationException;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.RequestValidator;

import java.util.Set;

public class ReturnTicketRequestValidator implements RequestValidator<ReturnTicketRequest> {

    private static final Set<String> PAYMENT_METHODS = Set.of("Cash", "Bank");

    @Override
    public void validate(ReturnTicketRequest request) throws ValidationException {
        if (request == null) {
            return;
        }
        if (request.payment_method() != null && !PAYMENT_METHODS.contains(request.payment_method().trim())) {
            throw new ValidationException("payment_method", "Phương thức thu phụ phí chỉ nhận Cash hoặc Bank", "INVALID_PAYMENT_METHOD");
        }
        if (request.refund_method() != null && !PAYMENT_METHODS.contains(request.refund_method().trim())) {
            throw new ValidationException("refund_method", "Phương thức hoàn tiền chỉ nhận Cash hoặc Bank", "INVALID_PAYMENT_METHOD");
        }
    }
}
