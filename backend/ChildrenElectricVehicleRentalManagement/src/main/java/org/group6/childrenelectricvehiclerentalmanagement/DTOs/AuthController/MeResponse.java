package org.group6.childrenelectricvehiclerentalmanagement.DTOs.AuthController;

import org.group6.childrenelectricvehiclerentalmanagement.Enum.UserStatus;

/**
 * Thông tin phiên hiện tại (GET /api/auth/me). Chỉ chứa các trường an toàn.
 */
public record MeResponse(Long id, String username, UserStatus status) {
}
