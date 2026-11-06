package org.group6.childrenelectricvehiclerentalmanagement.Exceptions;

public class VehicleSlugAlreadyExits extends AbstractException {

    public VehicleSlugAlreadyExits() {
        super("vehicle slug already exists", "Tên nhận diện này đã được sử dụng", 422, "SLUG_TAKEN");
    }
}
