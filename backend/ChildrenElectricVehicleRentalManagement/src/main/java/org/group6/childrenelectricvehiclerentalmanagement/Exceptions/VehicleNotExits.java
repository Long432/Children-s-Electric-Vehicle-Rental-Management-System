package org.group6.childrenelectricvehiclerentalmanagement.Exceptions;

public class VehicleNotExits extends AbstractException {

    public VehicleNotExits() {
        super("vehicle not exists", "Xe này không tồn tại", 404, "VEHICLE_NOT_FOUND");
    }
}
