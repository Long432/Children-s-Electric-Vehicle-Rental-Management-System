package org.group6.childrenelectricvehiclerentalmanagement.Exceptions;

public class SessionHasOpenTickets extends AbstractException {
    public SessionHasOpenTickets() {
        super("Session still has tickets in 'Waiting', 'Inused' or 'Overdue' status", "SESSION_HAS_OPEN_TICKETS",
                "Vẫn còn vé chưa xử lý xong (đang chờ, đang chạy hoặc quá hạn)", 409);
    }
}
