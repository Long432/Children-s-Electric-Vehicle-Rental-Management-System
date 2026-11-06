package org.group6.childrenelectricvehiclerentalmanagement.Utility.Validators;

import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Ticket.CreateTicketRequest;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.ValidationException;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.MinutesRequestValidator;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.RequestValidator;

import java.util.Set;
import java.util.regex.Pattern;

public class CreateTicketRequestValidator implements RequestValidator<CreateTicketRequest> {

    private static final Pattern PHONE = Pattern.compile("^0\\d{9,10}$");
    private static final Set<String> PAYMENT_METHODS = Set.of("Cash", "Bank");

    @Override
    public void validate(CreateTicketRequest request) throws ValidationException {
        if (request == null) {
            throw new ValidationException("body", "Thiếu dữ liệu gửi lên");
        }
        if (request.phone() == null || request.phone().isBlank()) {
            throw new ValidationException("phone", "Số điện thoại không được để trống");
        }
        if (!PHONE.matcher(request.phone().trim()).matches()) {
            throw new ValidationException("phone", "Số điện thoại phải gồm 10 hoặc 11 chữ số và bắt đầu bằng 0");
        }
        if (request.minutes() == null) {
            throw new ValidationException("minutes", "Trường minutes là bắt buộc");
        }
        if (request.minutes() < 1 || request.minutes() > MinutesRequestValidator.MAX_MINUTES) {
            throw new ValidationException("minutes", "Thời gian thuê phải từ 1 đến " + MinutesRequestValidator.MAX_MINUTES + " phút");
        }
        if (request.payment_method() == null || !PAYMENT_METHODS.contains(request.payment_method().trim())) {
            throw new ValidationException("payment_method", "Phương thức thanh toán chỉ nhận Cash hoặc Bank", "INVALID_PAYMENT_METHOD");
        }
    }
}
