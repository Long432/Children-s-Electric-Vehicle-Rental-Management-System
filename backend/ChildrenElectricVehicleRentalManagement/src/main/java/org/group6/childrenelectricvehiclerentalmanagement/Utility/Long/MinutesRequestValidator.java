package org.group6.childrenelectricvehiclerentalmanagement.Utility;

import org.group6.childrenelectricvehiclerentalmanagement.DTOs.MinutesRequest;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.ValidationException;

/** Kiểm tra trường minutes trong khoảng [min, max]; dùng chung cho nhiều API với nhãn và giới hạn khác nhau. */
public class MinutesRequestValidator implements RequestValidator<MinutesRequest> {
    public static final int MAX_MINUTES = 1440;

    private final String label;
    private final int min;
    private final int max;

    public MinutesRequestValidator(String label, int min, int max) {
        this.label = label;
        this.min = min;
        this.max = max;
    }

    @Override
    public void validate(MinutesRequest request) throws ValidationException {
        if (request == null) {
            throw new ValidationException("body", "Thiếu dữ liệu gửi lên");
        }
        Integer minutes = request.getMinutes();
        if (minutes == null) {
            throw new ValidationException("minutes", "Trường minutes là bắt buộc");
        }
        if (minutes < min || minutes > max) {
            throw new ValidationException("minutes", label + " phải từ " + min + " đến " + max + " phút");
        }
    }
}
