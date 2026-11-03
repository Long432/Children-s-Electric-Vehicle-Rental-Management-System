package org.group6.childrenelectricvehiclerentalmanagement.Exceptions;

public class VehicleNotInSession extends AbstractException {
    public VehicleNotInSession() {
        super("Vehicle does not belong to the current session", "VEHICLE_NOT_IN_SESSION", "Xe không thuộc phiên hiện tại", 409);
    }
}
