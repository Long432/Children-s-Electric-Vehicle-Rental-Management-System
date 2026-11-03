package org.group6.childrenelectricvehiclerentalmanagement.Exceptions;

public class DatabaseException extends AbstractException {
    public DatabaseException() {
        super("Database operation failed, please contact system administrator", "DATABASE_ERROR", "Có lỗi từ hệ thống, vui lòng thử lại sau", 500);
    }
}
