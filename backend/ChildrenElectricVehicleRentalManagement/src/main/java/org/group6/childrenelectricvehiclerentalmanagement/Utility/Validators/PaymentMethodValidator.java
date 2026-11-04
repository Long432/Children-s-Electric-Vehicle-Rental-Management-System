package org.group6.childrenelectricvehiclerentalmanagement.Utility.Validators;

import org.group6.childrenelectricvehiclerentalmanagement.Enum.PaymentMethod;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.ValidationException;

public final class PaymentMethodValidator {
    private PaymentMethodValidator() {}

    public static PaymentMethod validate(String raw) throws ValidationException {
        if (raw == null || raw.isBlank()) {
            throw new ValidationException("payment_method","payment_method không được để trống (Cash hoặc Bank)");
        }
        for (PaymentMethod method : PaymentMethod.values()) {
            if (method.name().equalsIgnoreCase(raw.trim())) {
                return method;
            }
        }
        throw new ValidationException("payment_method","payment_method chỉ nhận Cash hoặc Bank");
    }

    public static PaymentMethod validateOptional(String raw) throws ValidationException {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return validate(raw);
    }
}
