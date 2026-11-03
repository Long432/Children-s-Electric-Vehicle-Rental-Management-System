package org.group6.childrenelectricvehiclerentalmanagement.Exceptions;

public class BelowMinimumTime extends AbstractException {
    public BelowMinimumTime(String userMessage) {
        super("BELOW_MINIMUM_TIME", "BELOW_MINIMUM_TIME", userMessage, 422);
    }
}
