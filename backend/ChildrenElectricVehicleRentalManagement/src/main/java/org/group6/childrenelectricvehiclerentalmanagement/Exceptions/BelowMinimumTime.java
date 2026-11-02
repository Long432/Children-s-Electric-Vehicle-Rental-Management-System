package org.group6.childrenelectricvehiclerentalmanagement.Exceptions;

public class BelowMinimumTime extends AbstractException {
    public BelowMinimumTime(String message) {
        super("BELOW_MINIMUM_TIME", message, 422);
    }
}
