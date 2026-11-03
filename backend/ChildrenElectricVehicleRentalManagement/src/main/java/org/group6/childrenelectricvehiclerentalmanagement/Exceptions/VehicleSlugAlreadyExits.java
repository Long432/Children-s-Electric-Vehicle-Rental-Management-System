package org.group6.childrenelectricvehiclerentalmanagement.Exceptions;

public class VehicleSlugAlreadyExits extends AbstractException {
    public VehicleSlugAlreadyExits() {
        super("Vehicle slug already exists, slug must be unique", "SLUG_TAKEN", "Tên nhận diện này đã được dùng, vui lòng chọn tên khác", 422);
    }
}
