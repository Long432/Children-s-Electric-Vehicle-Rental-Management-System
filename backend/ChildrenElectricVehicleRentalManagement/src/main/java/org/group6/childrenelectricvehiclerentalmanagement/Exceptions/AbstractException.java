package org.group6.childrenelectricvehiclerentalmanagement.Exceptions;

import org.springframework.http.HttpStatusCode;

public class AbstractException extends Exception {

    private final String systemLog;
    private final String userMessage;
    private final HttpStatusCode statusCode;
    private final String code;

    public AbstractException(String message, String userMessage, int statusCode) {
        this(message, userMessage, statusCode, "ERROR");
    }

    public AbstractException(String message, String userMessage, int statusCode, String code) {
        super(message);
        this.systemLog = message;
        this.userMessage = userMessage;
        this.statusCode = HttpStatusCode.valueOf(statusCode);
        this.code = code;
    }

    public String getSystemLog() {
        return systemLog;
    }

    public String getUserMessage() {
        return userMessage;
    }

    public HttpStatusCode getStatusCode() {
        return statusCode;
    }

    public String getCode() {
        return code;
    }
}
