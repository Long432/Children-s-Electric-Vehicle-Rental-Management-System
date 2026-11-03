package org.group6.childrenelectricvehiclerentalmanagement.Exceptions;

public class CookieNotValid extends AbstractException {
    public CookieNotValid(String systemLog, String userMessage) {
        super(systemLog, "COOKIE_NOT_VALID", userMessage, 401);
    }
}