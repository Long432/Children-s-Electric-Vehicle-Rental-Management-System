package org.group6.childrenelectricvehiclerentalmanagement.Exceptions;

public class SameVehicle extends AbstractException {
    public SameVehicle() {
        super("SAME_VEHICLE", "Xe mới phải khác xe đang dùng", 409);
    }
}
