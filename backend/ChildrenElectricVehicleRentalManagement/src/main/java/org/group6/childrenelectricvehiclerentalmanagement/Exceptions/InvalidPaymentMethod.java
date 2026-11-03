package org.group6.childrenelectricvehiclerentalmanagement.Exceptions;

public class InvalidPaymentMethod extends AbstractException {
    public InvalidPaymentMethod() {
        super("Payment method must be either 'Cash' or 'Bank'", "INVALID_PAYMENT_METHOD", "Phương thức thanh toán chỉ nhận Cash hoặc Bank", 422);
    }
}
