package org.group6.childrenelectricvehiclerentalmanagement.Exceptions;

public class VehicleNotExits extends AbstractException {
    public VehicleNotExits() {
        super("Vehicle does not exist or is not available", "VEHICLE_NOT_EXITS", "Xe này không khả dụng", 404);
    }
}
