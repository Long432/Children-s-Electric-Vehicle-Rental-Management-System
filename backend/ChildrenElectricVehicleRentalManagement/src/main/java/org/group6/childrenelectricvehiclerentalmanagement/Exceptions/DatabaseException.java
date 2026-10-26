package org.group6.childrenelectricvehiclerentalmanagement.Exceptions;

public class DatabaseException extends AbstractException{
    public DatabaseException() {
        super("database exception", "lỗi server", 500);
    }
}
