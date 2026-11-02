package org.group6.childrenelectricvehiclerentalmanagement.Exceptions;

public class InvalidPaymentMethod extends AbstractException {
    public InvalidPaymentMethod() {
        super("INVALID_PAYMENT_METHOD", "payment_method chỉ nhận Cash hoặc Bank", 422);
    }
}
