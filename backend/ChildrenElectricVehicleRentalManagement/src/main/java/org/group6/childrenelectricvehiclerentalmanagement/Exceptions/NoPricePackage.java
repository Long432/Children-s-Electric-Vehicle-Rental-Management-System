package org.group6.childrenelectricvehiclerentalmanagement.Exceptions;

public class NoPricePackage extends AbstractException {
    public NoPricePackage() {
        super("NO_PRICE_PACKAGE", "Chưa cấu hình gói giá mặc định", 409);
    }
}
