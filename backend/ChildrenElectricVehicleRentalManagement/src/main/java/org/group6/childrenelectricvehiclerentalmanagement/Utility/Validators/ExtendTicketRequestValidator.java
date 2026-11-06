package org.group6.childrenelectricvehiclerentalmanagement.Utility.Validators;

import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Ticket.ExtendTicketRequest;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.ValidationException;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.MinutesRequestValidator;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.RequestValidator;

import java.util.Set;

public class ExtendTicketRequestValidator implements RequestValidator<ExtendTicketRequest> {

    private static final Set<String> PAYMENT_METHODS = Set.of("Cash", "Bank");

    @Override
    public void validate(ExtendTicketRequest request) throws ValidationException {
        if (request == null) {
            throw new ValidationException("body", "Thiếu dữ liệu gửi lên");
        }
        if (request.minutes() == null) {
            throw new ValidationException("minutes", "Trường minutes là bắt buộc");
        }
        if (request.minutes() < 1 || request.minutes() > MinutesRequestValidator.MAX_MINUTES) {
            throw new ValidationException("minutes", "Thời gian gia hạn phải từ 1 đến " + MinutesRequestValidator.MAX_MINUTES + " phút");
        }
        if (request.payment_method() == null || !PAYMENT_METHODS.contains(request.payment_method().trim())) {
            throw new ValidationException("payment_method", "Phương thức thanh toán chỉ nhận Cash hoặc Bank", "INVALID_PAYMENT_METHOD");
        }
    }
}
