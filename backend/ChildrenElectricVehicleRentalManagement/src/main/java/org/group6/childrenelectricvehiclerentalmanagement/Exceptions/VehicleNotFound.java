package org.group6.childrenelectricvehiclerentalmanagement.Exceptions;

public class VehicleNotFound extends AbstractException {
    public VehicleNotFound() {
        super("Vehicle with the given ID was not found in the database", "VEHICLE_NOT_FOUND", "Không tìm thấy xe", 404);
    }
}
