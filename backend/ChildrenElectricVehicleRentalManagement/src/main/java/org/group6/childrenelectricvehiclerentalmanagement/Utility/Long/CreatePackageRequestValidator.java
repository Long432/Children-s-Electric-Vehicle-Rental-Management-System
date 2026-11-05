package org.group6.childrenelectricvehiclerentalmanagement.Utility.Long;

import org.group6.childrenelectricvehiclerentalmanagement.DTOs.PackageController.CreatePackageRequest;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.ValidationException;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.RequestValidator;

public class CreatePackageRequestValidator implements RequestValidator<CreatePackageRequest> {
    @Override
    public void validate(CreatePackageRequest request) throws ValidationException {
        if (request == null) {
            throw new ValidationException("body", "Thiếu dữ liệu gửi lên");
        }
        Integer amount = request.getAmount();
        if (amount == null) {
            throw new ValidationException("amount", "Trường amount là bắt buộc");
        }
        if (amount < 1 || amount > UpdatePackagesRequestValidator.MAX_AMOUNT) {
            throw new ValidationException("amount",
                    "Số tiền của gói phải từ 1 đến " + UpdatePackagesRequestValidator.MAX_AMOUNT + " VND");
        }
        Integer minutes = request.getMinutes();
        if (minutes == null) {
            throw new ValidationException("minutes", "Trường minutes là bắt buộc");
        }
        if (minutes < 1 || minutes > UpdatePackagesRequestValidator.MAX_MINUTES) {
            throw new ValidationException("minutes",
                    "Số phút của gói phải từ 1 đến " + UpdatePackagesRequestValidator.MAX_MINUTES);
        }
        if (request.getIsDefault() == null) {
            throw new ValidationException("is_default", "Trường is_default là bắt buộc (true hoặc false)");
        }
    }
}
