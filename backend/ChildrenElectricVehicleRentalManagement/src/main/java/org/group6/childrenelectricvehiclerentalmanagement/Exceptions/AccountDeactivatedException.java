package org.group6.childrenelectricvehiclerentalmanagement.Exceptions;

public class AccountDeactivatedException extends AbstractException {

    public AccountDeactivatedException() {
        super("account deactivated", "Tài khoản đã bị vô hiệu hóa", 403, "ACCOUNT_DEACTIVATED");
    }
}
