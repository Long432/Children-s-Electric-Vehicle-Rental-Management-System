package org.group6.childrenelectricvehiclerentalmanagement.Exceptions;

public class AbstractException extends Exception{
    private String systemLog;
    private String userMessage;
    private int statusCode;

    public AbstractException(String message, String userMessage, int statusCode) {
        super(message);
        this.systemLog = message;
        this.userMessage = userMessage;
        this.statusCode = statusCode;
    }
}
