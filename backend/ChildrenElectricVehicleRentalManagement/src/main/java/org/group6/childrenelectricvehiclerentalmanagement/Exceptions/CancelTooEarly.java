package org.group6.childrenelectricvehiclerentalmanagement.Exceptions;

public class CancelTooEarly extends AbstractException {
    public CancelTooEarly(int minutesLeft) {
        super("Ticket cannot be canceled before the required waiting time has elapsed", "CANCEL_WAIT_NOT_ELAPSED",
                "Vé cần chờ thêm " + minutesLeft + " phút nữa mới được hủy", 409);
    }
}
