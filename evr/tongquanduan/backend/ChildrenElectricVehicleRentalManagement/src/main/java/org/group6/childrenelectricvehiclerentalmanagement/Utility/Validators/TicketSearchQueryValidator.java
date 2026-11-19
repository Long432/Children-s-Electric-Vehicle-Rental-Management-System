package org.group6.childrenelectricvehiclerentalmanagement.Utility.Validators;

import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Ticket.TicketSearchQuery;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.ValidationException;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.RequestValidator;

import java.util.regex.Pattern;

public class TicketSearchQueryValidator implements RequestValidator<TicketSearchQuery> {

    public static final int MAX_TICKET_NO_DIGITS = 4;
    public static final int MAX_QUERY_DIGITS = PhoneRules.LENGTH;

    private static final Pattern DIGITS = Pattern.compile("^\\d+$");

    public static boolean matchesTicketNo(String value) {
        return value != null && value.length() <= MAX_TICKET_NO_DIGITS;
    }

    @Override
    public void validate(TicketSearchQuery query) throws ValidationException {
        if (query == null) {
            throw new ValidationException("query", "Thiếu tham số truy vấn");
        }
        if (query.q() == null || query.q().isBlank()) {
            throw new ValidationException("q", "Từ khóa tìm kiếm không được để trống");
        }
        String value = PhoneRules.normalize(query.q());
        if (!DIGITS.matcher(value).matches()) {
            throw new ValidationException("q", "Chỉ tìm theo mã vé hoặc số điện thoại, từ khóa chỉ gồm chữ số");
        }
        if (value.length() > MAX_QUERY_DIGITS) {
            throw new ValidationException("q", "Từ khóa tối đa " + MAX_QUERY_DIGITS + " chữ số");
        }
        if (query.session_id() != null && query.session_id() < 1) {
            throw new ValidationException("session_id", "Mã phiên không hợp lệ");
        }
    }
}
