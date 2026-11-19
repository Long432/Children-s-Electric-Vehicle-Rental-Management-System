package org.group6.childrenelectricvehiclerentalmanagement.DTOs.Auth;

import org.group6.childrenelectricvehiclerentalmanagement.Enum.UserRole;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.UserStatus;
import org.group6.childrenelectricvehiclerentalmanagement.Models.User;

public record UserResponse(long id, String username, UserStatus status, UserRole role) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getStatus(), user.getRole() == null ? UserRole.Staff : user.getRole());
    }
}
