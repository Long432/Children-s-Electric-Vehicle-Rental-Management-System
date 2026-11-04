package org.group6.childrenelectricvehiclerentalmanagement.Utility.Validators;

import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Long.Requests;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.ValidationException;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.ValidPackage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class PackageListValidator {
    private static final int MAX_PACKAGES = 20;

    private PackageListValidator() {}

    public static List<ValidPackage> validate(List<Requests.PackageItem> items) throws ValidationException {
        if (items == null || items.isEmpty()) {
            throw new ValidationException("packages","packages không được để trống");
        }
        if (items.size() > MAX_PACKAGES) {
            throw new ValidationException("packages","packages tối đa " + MAX_PACKAGES + " gói");
        }
        List<ValidPackage> result = new ArrayList<>();
        Set<Integer> seenMinutes = new HashSet<>();
        int defaults = 0;
        for (Requests.PackageItem item : items) {
            if (item == null) {
                throw new ValidationException("packages","Mỗi gói giá phải có amount, minutes, is_default");
            }
            int amount = AmountValidator.validatePositive("amount", item.getAmount());
            int minutes = MinutesValidator.validatePositive("minutes", item.getMinutes());
            boolean isDefault = BooleanValidator.validate("is_default", item.getIsDefault());
            if (!seenMinutes.add(minutes)) {
                throw new ValidationException("packages","Hai gói giá không được trùng số phút (" + minutes + ")");
            }
            if (isDefault) {
                defaults++;
            }
            result.add(new ValidPackage(amount, minutes, isDefault));
        }
        if (defaults != 1) {
            throw new ValidationException("packages","Phải có đúng 1 gói mặc định (is_default = true)");
        }
        return result;
    }
}
