package org.group6.childrenelectricvehiclerentalmanagement.Exceptions;

public class BelowMinimumTimeException extends ValidationException {

    public BelowMinimumTimeException(int minimumMinutes) {
        super("minutes", "Thời gian thuê tối thiểu là " + minimumMinutes + " phút", "BELOW_MINIMUM_TIME");
    }
}
