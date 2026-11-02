package org.group6.childrenelectricvehiclerentalmanagement.Exceptions;

public class DatabaseException extends AbstractException {
    public DatabaseException() {
        super("DATABASE_ERROR", "lỗi server", 500);
    }
}
