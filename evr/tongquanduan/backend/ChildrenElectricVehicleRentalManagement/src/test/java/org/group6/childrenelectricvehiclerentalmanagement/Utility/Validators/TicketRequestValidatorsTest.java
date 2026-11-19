package org.group6.childrenelectricvehiclerentalmanagement.Utility.Validators;

import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Session.StartSessionRequest;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Ticket.CreateTicketRequest;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Ticket.TicketSearchQuery;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.ValidationException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TicketRequestValidatorsTest {

    private final CreateTicketRequestValidator create = new CreateTicketRequestValidator();
    private final TicketSearchQueryValidator search = new TicketSearchQueryValidator();
    private final StartSessionRequestValidator start = new StartSessionRequestValidator();

    @Test
    void validTicketRequestPasses() {
        assertDoesNotThrow(() -> create.validate(new CreateTicketRequest("0912345678", 30, "Cash")));
    }

    @Test
    void badPhoneIsRejected() {
        ValidationException error = assertThrows(ValidationException.class,
                () -> create.validate(new CreateTicketRequest("12345", 30, "Cash")));
        assertEquals("phone", error.getField());
    }

    @Test
    void phoneWithSeparatorsAndCountryCodePasses() {
        assertDoesNotThrow(() -> create.validate(new CreateTicketRequest("+84 912-345-678", 30, "Cash")));
    }

    @Test
    void elevenDigitPhoneIsRejected() {
        assertThrows(ValidationException.class, () -> create.validate(new CreateTicketRequest("09123456789", 30, "Cash")));
    }

    @Test
    void minutesOutsideRangeAreRejected() {
        assertThrows(ValidationException.class, () -> create.validate(new CreateTicketRequest("0912345678", 0, "Cash")));
        assertThrows(ValidationException.class, () -> create.validate(new CreateTicketRequest("0912345678", 1441, "Cash")));
        assertThrows(ValidationException.class, () -> create.validate(new CreateTicketRequest("0912345678", null, "Cash")));
    }

    @Test
    void unknownPaymentMethodHasItsOwnCode() {
        ValidationException error = assertThrows(ValidationException.class,
                () -> create.validate(new CreateTicketRequest("0912345678", 30, "Card")));
        assertEquals("INVALID_PAYMENT_METHOD", error.getCode());
    }

    @Test
    void missingBodyIsRejected() {
        assertThrows(ValidationException.class, () -> create.validate(null));
    }

    @Test
    void searchAcceptsDigitsOnly() {
        assertDoesNotThrow(() -> search.validate(new TicketSearchQuery("7", null)));
        assertDoesNotThrow(() -> search.validate(new TicketSearchQuery("0912345678", 3L)));
        assertThrows(ValidationException.class, () -> search.validate(new TicketSearchQuery("abc", null)));
        assertThrows(ValidationException.class, () -> search.validate(new TicketSearchQuery("", null)));
        assertThrows(ValidationException.class, () -> search.validate(new TicketSearchQuery("123456789012", null)));
        assertThrows(ValidationException.class, () -> search.validate(new TicketSearchQuery("09123456789", null)));
        assertDoesNotThrow(() -> search.validate(new TicketSearchQuery("+84 912 345 678", null)));
        assertThrows(ValidationException.class, () -> search.validate(new TicketSearchQuery("7", 0L)));
    }

    @Test
    void searchAcceptsFromOneUpToTenDigits() {
        assertDoesNotThrow(() -> search.validate(new TicketSearchQuery("0912", null)));
        assertDoesNotThrow(() -> search.validate(new TicketSearchQuery(" 7 ", null)));
        assertDoesNotThrow(() -> search.validate(new TicketSearchQuery("0912345678", null)));
    }

    @Test
    void searchRejectsLettersWithUserMessage() {
        ValidationException error = assertThrows(ValidationException.class, () -> search.validate(new TicketSearchQuery("09a2", null)));
        assertEquals("q", error.getField());
        assertEquals("INVALID_INPUT", error.getCode());
    }

    @Test
    void keywordsUpToFourDigitsMatchTicketNumbers() {
        assertTrue(TicketSearchQueryValidator.matchesTicketNo("7"));
        assertTrue(TicketSearchQueryValidator.matchesTicketNo("0912"));
        assertFalse(TicketSearchQueryValidator.matchesTicketNo("09123"));
        assertFalse(TicketSearchQueryValidator.matchesTicketNo(null));
    }

    @Test
    void startRequiresDistinctValidSlugs() {
        assertDoesNotThrow(() -> start.validate(new StartSessionRequest(List.of("xe-gau-01", "xe-gau-02"), null, "22:30")));
        assertThrows(ValidationException.class, () -> start.validate(new StartSessionRequest(List.of(), null, null)));
        assertThrows(ValidationException.class, () -> start.validate(new StartSessionRequest(List.of("xe-gau-01", "xe-gau-01"), null, null)));
        assertThrows(ValidationException.class, () -> start.validate(new StartSessionRequest(List.of("Xe Gau"), null, null)));
        assertThrows(ValidationException.class, () -> start.validate(new StartSessionRequest(List.of("xe-gau-01"), 0L, null)));
        assertThrows(ValidationException.class, () -> start.validate(new StartSessionRequest(List.of("xe-gau-01"), null, "25:99")));
    }
}
