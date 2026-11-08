package org.group6.childrenelectricvehiclerentalmanagement.Utility.Long;

import org.group6.childrenelectricvehiclerentalmanagement.DTOs.PackageController.UpdatePackageRequest;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.ValidationException;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.RequestValidator;

public class UpdatePackageRequestValidator implements RequestValidator<UpdatePackageRequest> {
    @Override
    public void validate(UpdatePackageRequest request) throws ValidationException {
        if (request == null) {
            throw new ValidationException("body", "Thiếu dữ liệu gửi lên");
        }
        if (request.getAmount() == null && request.getMinutes() == null && request.getIsDefault() == null) {
            throw new ValidationException("body", "Cần gửi ít nhất một trường: amount, minutes hoặc is_default");
        }
        Integer amount = request.getAmount();
        if (amount != null && (amount < 1 || amount > UpdatePackagesRequestValidator.MAX_AMOUNT)) {
            throw new ValidationException("amount",
                    "Số tiền của gói phải từ 1 đến " + UpdatePackagesRequestValidator.MAX_AMOUNT + " VND");
        }
        Integer minutes = request.getMinutes();
        if (minutes != null && (minutes < 1 || minutes > UpdatePackagesRequestValidator.MAX_MINUTES)) {
            throw new ValidationException("minutes",
                    "Số phút của gói phải từ 1 đến " + UpdatePackagesRequestValidator.MAX_MINUTES);
        }
    }
}
