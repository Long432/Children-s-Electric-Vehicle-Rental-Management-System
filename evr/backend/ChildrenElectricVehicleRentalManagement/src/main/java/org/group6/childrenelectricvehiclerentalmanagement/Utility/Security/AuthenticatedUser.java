package org.group6.childrenelectricvehiclerentalmanagement.Utility.Security;

import org.group6.childrenelectricvehiclerentalmanagement.Enum.UserRole;

public record AuthenticatedUser(long id, String username, String tokenHash, UserRole role) {

    public AuthenticatedUser(long id, String username, String tokenHash) {
        this(id, username, tokenHash, UserRole.Staff);
    }

    public AuthenticatedUser(long id, String username) {
        this(id, username, null, UserRole.Staff);
    }

    public boolean isOwner() {
        return role == UserRole.Owner;
    }
}
