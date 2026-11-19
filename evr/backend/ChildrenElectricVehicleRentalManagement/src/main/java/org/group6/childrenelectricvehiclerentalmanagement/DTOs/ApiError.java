package org.group6.childrenelectricvehiclerentalmanagement.DTOs;

import lombok.Getter;

/** Phần "error" của phản hồi lỗi. */
@Getter
public class ApiError {
    private final String code;
    private final String message;

    public ApiError(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
