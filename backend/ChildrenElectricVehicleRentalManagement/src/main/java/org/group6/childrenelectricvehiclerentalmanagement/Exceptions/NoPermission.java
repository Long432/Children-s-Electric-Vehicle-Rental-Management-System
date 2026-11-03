package org.group6.childrenelectricvehiclerentalmanagement.Exceptions;

public class NoPermission extends AbstractException {
    public NoPermission() {
        super("User does not have required permission to perform this action", "NO_PERMISSION", "Bạn không có quyền thực hiện thao tác này", 403);
    }
}
