package org.group6.childrenelectricvehiclerentalmanagement.Exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatusCode;

@Getter
public class AbstractException extends Exception{
    private final String systemLog;
    private final String userMessage;
    private final HttpStatusCode statusCode;
    // userMessage sẽ là tin nhắn đầy đủ trả về cho người dùng còn systemLog sẽ là tin nhắn nối liền với "không thể thực hiện yêu cầu do"

    public AbstractException(String message, String userMessage, int statusCode) {
        super(message);
        this.systemLog = message;
        this.userMessage = userMessage;
        this.statusCode = HttpStatusCode.valueOf(statusCode);
    }

}
