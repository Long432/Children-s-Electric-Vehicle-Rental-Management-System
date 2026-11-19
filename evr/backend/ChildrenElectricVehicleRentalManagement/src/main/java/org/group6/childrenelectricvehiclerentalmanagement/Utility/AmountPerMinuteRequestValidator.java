package org.group6.childrenelectricvehiclerentalmanagement.Utility;

import org.group6.childrenelectricvehiclerentalmanagement.DTOs.AmountPerMinuteRequest;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.ValidationException;

public class AmountPerMinuteRequestValidator implements RequestValidator<AmountPerMinuteRequest> {
    public static final int MAX_FEE_PER_MINUTE = 1_000_000;

    @Override
    public void validate(AmountPerMinuteRequest request) throws ValidationException {
        if (request == null) {
            throw new ValidationException("body", "Thiếu dữ liệu gửi lên");
        }
        Integer amount = request.getAmountPerMinute();
        if (amount == null) {
            throw new ValidationException("amount_per_minute", "Trường amount_per_minute là bắt buộc");
        }
        if (amount < 0 || amount > MAX_FEE_PER_MINUTE) {
            throw new ValidationException("amount_per_minute",
                    "Phụ phí mỗi phút phải từ 0 đến " + MAX_FEE_PER_MINUTE + " VND");
        }
    }
}
