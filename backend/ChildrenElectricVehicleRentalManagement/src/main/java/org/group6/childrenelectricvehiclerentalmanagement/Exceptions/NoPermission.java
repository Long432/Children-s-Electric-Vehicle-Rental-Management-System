package org.group6.childrenelectricvehiclerentalmanagement.Exceptions;

public class NoPermission extends AbstractException {
    public NoPermission() {
        super("NO_PERMISSION", "Bạn không có quyền", 403);
    }
}
