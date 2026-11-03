package org.group6.childrenelectricvehiclerentalmanagement.Exceptions;

public class UserException extends AbstractException{
    public UserException(String systemLog, String userMessage) {
        super(systemLog, "USER_ERROR", userMessage, 400);
    }
}
