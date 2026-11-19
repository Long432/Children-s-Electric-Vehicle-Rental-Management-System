package org.group6.childrenelectricvehiclerentalmanagement.Utility.Validators;

import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Ticket.AssignTicketRequest;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.ValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AssignTicketRequestValidatorTest {

    private final AssignTicketRequestValidator validator = new AssignTicketRequestValidator();

    @Test
    void validSlugIsAccepted() {
        assertDoesNotThrow(() -> validator.validate(new AssignTicketRequest("xe-gau-01")));
        assertDoesNotThrow(() -> validator.validate(new AssignTicketRequest("  xe-gau-01  ")));
    }

    @Test
    void missingBodyIsRejected() {
        ValidationException error = assertThrows(ValidationException.class, () -> validator.validate(null));
        assertEquals("body", error.getField());
    }

    @Test
    void blankOrMissingSlugIsRejected() {
        ValidationException missing = assertThrows(ValidationException.class, () -> validator.validate(new AssignTicketRequest(null)));
        ValidationException blank = assertThrows(ValidationException.class, () -> validator.validate(new AssignTicketRequest("   ")));
        assertEquals("vehicle_slug", missing.getField());
        assertEquals("vehicle_slug", blank.getField());
    }

    @Test
    void malformedSlugIsReportedOnTheVehicleSlugField() {
        for (String slug : new String[]{"Xe-Gau", "xe gau", "1xe", "xe--gau", "-xe", "xe-", "xe_gau", "x".repeat(51)}) {
            ValidationException error = assertThrows(ValidationException.class, () -> validator.validate(new AssignTicketRequest(slug)));
            assertEquals("vehicle_slug", error.getField());
        }
    }
}
