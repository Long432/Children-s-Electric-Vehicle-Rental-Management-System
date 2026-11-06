package org.group6.childrenelectricvehiclerentalmanagement.Exceptions;

public class ConflictException extends AbstractException {

    public ConflictException(String code, String systemLog, String userMessage) {
        super(systemLog, userMessage, 409, code);
    }
}
