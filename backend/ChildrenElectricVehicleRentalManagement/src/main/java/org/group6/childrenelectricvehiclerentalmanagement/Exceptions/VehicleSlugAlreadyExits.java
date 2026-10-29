package org.group6.childrenelectricvehiclerentalmanagement.Exceptions;

public class VehicleSlugAlreadyExits extends AbstractException {
    public VehicleSlugAlreadyExits() {
        super("vehicle slug already exits", "Tên nhận diện này đã được xử dụng", 400);
    }
}
