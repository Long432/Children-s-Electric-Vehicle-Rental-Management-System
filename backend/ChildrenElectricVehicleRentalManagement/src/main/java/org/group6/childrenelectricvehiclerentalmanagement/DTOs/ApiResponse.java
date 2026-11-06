package org.group6.childrenelectricvehiclerentalmanagement.DTOs;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

/**
 * Khung phản hồi chung:
 * thành công { "ok": true, "data": {...} }
 * lỗi        { "ok": false, "error": { "code": "...", "message": "..." } }
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse {
    private final boolean ok;
    private final Object data;
    private final ApiError error;

    private ApiResponse(boolean ok, Object data, ApiError error) {
        this.ok = ok;
        this.data = data;
        this.error = error;
    }

    public static ApiResponse success(Object data) {
        return new ApiResponse(true, data, null);
    }

    public static ApiResponse failure(String code, String message) {
        return new ApiResponse(false, null, new ApiError(code, message));
    }
}
