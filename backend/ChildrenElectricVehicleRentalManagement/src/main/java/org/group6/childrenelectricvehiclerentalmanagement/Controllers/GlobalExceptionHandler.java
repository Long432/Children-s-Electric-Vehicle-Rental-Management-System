package org.group6.childrenelectricvehiclerentalmanagement.Controllers;

import org.group6.childrenelectricvehiclerentalmanagement.DTOs.ApiResponse;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.AbstractException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AbstractException.class)
    public ResponseEntity<ApiResponse> handleApplicationException(AbstractException exception) {
        log.warn("Yêu cầu bị từ chối [{}]: {}", exception.getCode(), exception.getSystemLog());
        return ResponseEntity.status(exception.getStatusCode())
                .body(ApiResponse.failure(exception.getCode(), exception.getUserMessage()));
    }

    @ExceptionHandler({MissingServletRequestParameterException.class, ServletRequestBindingException.class})
    public ResponseEntity<ApiResponse> handleMissingInput(Exception exception) {
        log.warn("Yêu cầu thiếu hoặc sai tham số: {}", exception.getMessage());
        return ResponseEntity.status(400)
                .body(ApiResponse.failure("BAD_REQUEST", "Yêu cầu thiếu hoặc sai tham số"));
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ApiResponse> handleMalformedInput(Exception exception) {
        log.warn("Dữ liệu gửi lên không hợp lệ: {}", exception.getMessage());
        return ResponseEntity.status(422)
                .body(ApiResponse.failure("INVALID_INPUT", "Dữ liệu gửi lên không hợp lệ"));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse> handleMethodNotAllowed(HttpRequestMethodNotSupportedException exception) {
        log.warn("Phương thức không được hỗ trợ: {}", exception.getMessage());
        return ResponseEntity.status(405)
                .headers(exception.getHeaders())
                .body(ApiResponse.failure("METHOD_NOT_ALLOWED", "Phương thức không được hỗ trợ cho đường dẫn này"));
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiResponse> handleUnsupportedMediaType(HttpMediaTypeNotSupportedException exception) {
        log.warn("Kiểu dữ liệu không được hỗ trợ: {}", exception.getMessage());
        return ResponseEntity.status(415)
                .headers(exception.getHeaders())
                .body(ApiResponse.failure("UNSUPPORTED_MEDIA_TYPE", "Chỉ hỗ trợ dữ liệu dạng JSON"));
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<ApiResponse> handleNotAcceptable(HttpMediaTypeNotAcceptableException exception) {
        log.warn("Kiểu phản hồi không đáp ứng được: {}", exception.getMessage());
        return ResponseEntity.status(406)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.failure("NOT_ACCEPTABLE", "Chỉ có thể trả dữ liệu dạng JSON"));
    }

    @ExceptionHandler({NoResourceFoundException.class, NoHandlerFoundException.class})
    public ResponseEntity<ApiResponse> handleNotFound(Exception exception) {
        log.warn("Không tìm thấy đường dẫn: {}", exception.getMessage());
        return ResponseEntity.status(404)
                .body(ApiResponse.failure("NOT_FOUND", "Đường dẫn không tồn tại"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleUnexpected(Exception exception) {
        log.error("Lỗi không lường trước", exception);
        return ResponseEntity.status(500)
                .body(ApiResponse.failure("INTERNAL_ERROR", "Lỗi server"));
    }
}
