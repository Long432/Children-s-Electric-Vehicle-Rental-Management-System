package org.group6.childrenelectricvehiclerentalmanagement.Exceptions;

public class NoPricePackage extends AbstractException {
    public NoPricePackage() {
        super("No default pricing package is configured in the system", "NO_PRICE_PACKAGE", "Hệ thống chưa có gói giá mặc định", 409);
    }
}
