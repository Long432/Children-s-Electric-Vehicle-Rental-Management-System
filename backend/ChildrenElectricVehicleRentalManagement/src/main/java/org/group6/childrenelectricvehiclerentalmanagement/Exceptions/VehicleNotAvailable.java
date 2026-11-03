package org.group6.childrenelectricvehiclerentalmanagement.Exceptions;

public class VehicleNotAvailable extends AbstractException {
    public VehicleNotAvailable() {
        super("Vehicle is not in 'Online' status, cannot be assigned or used", "VEHICLE_NOT_AVAILABLE", "Xe hiện không sẵn sàng, vui lòng chọn xe khác", 409);
    }
}
