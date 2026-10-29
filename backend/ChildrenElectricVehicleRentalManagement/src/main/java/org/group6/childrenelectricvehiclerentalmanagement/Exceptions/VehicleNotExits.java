package org.group6.childrenelectricvehiclerentalmanagement.Exceptions;

public class VehicleNotExits extends AbstractException {
    public VehicleNotExits() {
        super("vehicle not exits", "Xe này không khả dụng", 404);
    }
}
