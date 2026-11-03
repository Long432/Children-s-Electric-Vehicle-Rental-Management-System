package org.group6.childrenelectricvehiclerentalmanagement.Exceptions;

public class InConstruction extends AbstractException {
    public InConstruction() {
        super("This feature is still under development and not yet implemented", "IN_CONSTRUCTION", "Tính năng này chưa có sẵn, vui lòng thử lại sau", 500);
    }
}
