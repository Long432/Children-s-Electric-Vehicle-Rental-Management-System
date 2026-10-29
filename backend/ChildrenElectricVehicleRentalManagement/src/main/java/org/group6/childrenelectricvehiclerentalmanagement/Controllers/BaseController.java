package org.group6.childrenelectricvehiclerentalmanagement.Controllers;

import lombok.extern.slf4j.Slf4j;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.AbstractException;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.io.IOException;
import java.util.Map;
import java.util.function.Supplier;

@Slf4j
public abstract class BaseController {
    @ExceptionHandler
    public ResponseEntity<String> handle(Exception ex) {
        log.error("Can't execute request because {}. Stacktrace: {}",ex.getMessage(),ex.getStackTrace());
        return new ResponseEntity<String>("Lỗi server", HttpStatusCode.valueOf(500));
    }

    @ExceptionHandler
    public ResponseEntity<String> handle(AbstractException ex) {
        log.error("Can't execute request because {}.",ex.getSystemLog());
        return new ResponseEntity<String>(ex.getUserMessage(),ex.getStatusCode());
    }
}
