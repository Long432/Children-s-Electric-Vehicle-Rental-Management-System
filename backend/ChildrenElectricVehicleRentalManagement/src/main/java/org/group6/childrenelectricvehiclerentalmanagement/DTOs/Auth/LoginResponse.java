package org.group6.childrenelectricvehiclerentalmanagement.DTOs.Auth;

public record LoginResponse(String token, String expires_at, UserResponse user) {
}
