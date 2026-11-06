package org.group6.childrenelectricvehiclerentalmanagement.Exceptions;

public class DatabaseException extends AbstractException {
    public DatabaseException() {
        super("database exception", "Lỗi server", 500, "DATABASE_ERROR");
    }
}
