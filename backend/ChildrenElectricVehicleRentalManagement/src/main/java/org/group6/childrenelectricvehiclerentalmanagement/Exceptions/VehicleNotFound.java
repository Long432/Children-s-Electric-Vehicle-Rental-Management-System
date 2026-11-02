package org.group6.childrenelectricvehiclerentalmanagement.Exceptions;

public class VehicleNotFound extends AbstractException {
    public VehicleNotFound() {
        super("VEHICLE_NOT_FOUND", "Không tìm thấy xe", 404);
    }
}
