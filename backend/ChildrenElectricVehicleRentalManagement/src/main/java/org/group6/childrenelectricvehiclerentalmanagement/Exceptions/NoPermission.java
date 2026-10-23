package org.group6.childrenelectricvehiclerentalmanagement.Exceptions;

public class NoPermission extends AbstractException{
    public NoPermission() {
        super("no permission", "Bạn không có quyền", 403);
    }
}
