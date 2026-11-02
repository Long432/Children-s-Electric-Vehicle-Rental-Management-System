package org.group6.childrenelectricvehiclerentalmanagement.Exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatusCode;

@Getter
public class CookieNotValid extends AbstractException {

    public CookieNotValid(String systemLog, String userMessage) {
        super(systemLog, userMessage, 400);
    }
}