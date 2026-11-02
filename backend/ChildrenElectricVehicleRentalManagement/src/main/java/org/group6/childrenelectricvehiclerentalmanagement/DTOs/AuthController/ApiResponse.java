package org.group6.childrenelectricvehiclerentalmanagement.DTOs.AuthController;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Envelop phản hồi chuẩn của các endpoint auth:
 * - Thành công: {@code { "ok": true, "data": ... }}
 * - Lỗi:      {@code { "ok": false, "error": { "code": "...", "message": "..." } } }
 * Các trường null bị loại bỏ để thân thiện với quy ước ví dụ của tài liệu.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(Boolean ok, T data, ErrorInfo error) {

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null);
    }

    public static <T> ApiResponse<T> error(String code, String message) {
        return new ApiResponse<>(false, null, new ErrorInfo(code, message));
    }
}
