package org.group6.childrenelectricvehiclerentalmanagement.DTOs.AuthController;

/**
 * Thông tin lỗi trong envelop phản hồi chuẩn: {@code { "ok": false, "error": { "code", "message" } } }.
 */
public record ErrorInfo(String code, String message) {
}
