package org.group6.childrenelectricvehiclerentalmanagement.Exceptions;

public class PackageNotFound extends AbstractException {
    public PackageNotFound() {
        super("Package with the given ID was not found in the database", "PACKAGE_NOT_FOUND", "Không tìm thấy gói giá", 404);
    }
}
