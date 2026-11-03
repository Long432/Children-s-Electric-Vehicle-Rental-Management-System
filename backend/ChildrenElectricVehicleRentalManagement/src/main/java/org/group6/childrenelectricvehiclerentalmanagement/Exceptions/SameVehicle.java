package org.group6.childrenelectricvehiclerentalmanagement.Exceptions;

public class SameVehicle extends AbstractException {
    public SameVehicle() {
        super("New vehicle is the same as the currently used vehicle, must be different", "SAME_VEHICLE", "Xe mới phải khác xe đang dùng", 409);
    }
}
