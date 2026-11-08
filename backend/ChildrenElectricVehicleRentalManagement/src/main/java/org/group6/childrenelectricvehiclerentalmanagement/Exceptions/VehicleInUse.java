package org.group6.childrenelectricvehiclerentalmanagement.Exceptions;

public class VehicleInUse extends AbstractException {
    public VehicleInUse(String slug) {
        super("Vehicle '" + slug + "' is in 'Inused' status", "VEHICLE_IN_USE",
                "Xe " + slug + " đang được sử dụng", 409);
    }
}
