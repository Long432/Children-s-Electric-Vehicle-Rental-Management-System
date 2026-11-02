package org.group6.childrenelectricvehiclerentalmanagement.Exceptions;

public class VehicleNotAvailable extends AbstractException {
    public VehicleNotAvailable() {
        super("VEHICLE_NOT_AVAILABLE", "Xe hiện không sẵn sàng (không ở trạng thái Online)", 409);
    }
}
