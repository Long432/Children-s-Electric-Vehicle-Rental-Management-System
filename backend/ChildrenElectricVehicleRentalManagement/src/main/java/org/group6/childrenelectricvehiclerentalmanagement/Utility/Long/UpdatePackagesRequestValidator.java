package org.group6.childrenelectricvehiclerentalmanagement.Utility.Long;

import org.group6.childrenelectricvehiclerentalmanagement.DTOs.SettingsController.PackageItemRequest;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.SettingsController.UpdatePackagesRequest;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.ValidationException;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.RequestValidator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UpdatePackagesRequestValidator implements RequestValidator<UpdatePackagesRequest> {
    public static final int MAX_PACKAGES = 20;
    public static final int MAX_AMOUNT = 10_000_000;
    public static final int MAX_MINUTES = 1440;

    @Override
    public void validate(UpdatePackagesRequest request) throws ValidationException {
        if (request == null) {
            throw new ValidationException("body", "Thiếu dữ liệu gửi lên");
        }
        List<PackageItemRequest> packages = request.getPackages();
        if (packages == null || packages.isEmpty()) {
            throw new ValidationException("packages", "Cần ít nhất một gói giá");
        }
        if (packages.size() > MAX_PACKAGES) {
            throw new ValidationException("packages", "Tối đa " + MAX_PACKAGES + " gói giá");
        }

        Set<Integer> seenMinutes = new HashSet<>();
        int defaultCount = 0;
        for (int i = 0; i < packages.size(); i++) {
            PackageItemRequest item = packages.get(i);
            String where = "packages[" + i + "]";
            if (item == null) {
                throw new ValidationException(where, "Gói giá thứ " + (i + 1) + " bị trống");
            }
            if (item.getAmount() == null) {
                throw new ValidationException(where + ".amount", "Gói giá thứ " + (i + 1) + " thiếu amount");
            }
            if (item.getAmount() < 1 || item.getAmount() > MAX_AMOUNT) {
                throw new ValidationException(where + ".amount",
                        "Số tiền của gói thứ " + (i + 1) + " phải từ 1 đến " + MAX_AMOUNT + " VND");
            }
            if (item.getMinutes() == null) {
                throw new ValidationException(where + ".minutes", "Gói giá thứ " + (i + 1) + " thiếu minutes");
            }
            if (item.getMinutes() < 1 || item.getMinutes() > MAX_MINUTES) {
                throw new ValidationException(where + ".minutes",
                        "Số phút của gói thứ " + (i + 1) + " phải từ 1 đến " + MAX_MINUTES);
            }
            if (item.getIsDefault() == null) {
                throw new ValidationException(where + ".is_default",
                        "Gói giá thứ " + (i + 1) + " thiếu is_default (true hoặc false)");
            }
            if (!seenMinutes.add(item.getMinutes())) {
                throw new ValidationException(where + ".minutes",
                        "Có hai gói cùng " + item.getMinutes() + " phút");
            }
            if (item.getIsDefault()) {
                defaultCount++;
            }
        }
        if (defaultCount != 1) {
            throw new ValidationException("packages",
                    "Phải có đúng một gói mặc định (is_default = true), hiện có " + defaultCount);
        }
    }
}
