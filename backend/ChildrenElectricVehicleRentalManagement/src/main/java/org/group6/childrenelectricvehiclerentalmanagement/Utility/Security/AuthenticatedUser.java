package org.group6.childrenelectricvehiclerentalmanagement.Utility.Security;

public record AuthenticatedUser(long id, String username, String tokenHash) {

    public AuthenticatedUser(long id, String username) {
        this(id, username, null);
    }
}
