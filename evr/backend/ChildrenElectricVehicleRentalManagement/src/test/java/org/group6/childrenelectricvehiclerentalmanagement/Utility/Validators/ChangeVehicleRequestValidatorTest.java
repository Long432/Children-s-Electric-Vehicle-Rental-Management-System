package org.group6.childrenelectricvehiclerentalmanagement.Utility.Validators;

import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Ticket.ChangeVehicleRequest;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.ValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ChangeVehicleRequestValidatorTest {

    private final ChangeVehicleRequestValidator validator = new ChangeVehicleRequestValidator();

    @Test
    void validRequestsAreAccepted() {
        assertDoesNotThrow(() -> validator.validate(new ChangeVehicleRequest("xe-gau-02", "Online")));
        assertDoesNotThrow(() -> validator.validate(new ChangeVehicleRequest(" xe-gau-02 ", " Repairing ")));
    }

    @Test
    void missingBodyIsRejected() {
        ValidationException error = assertThrows(ValidationException.class, () -> validator.validate(null));
        assertEquals("body", error.getField());
    }

    @Test
    void blankOrMalformedSlugIsReportedOnTheSlugField() {
        for (String slug : new String[]{null, "  ", "Xe-Gau", "xe gau", "1xe", "xe--gau"}) {
            ValidationException error = assertThrows(ValidationException.class,
                    () -> validator.validate(new ChangeVehicleRequest(slug, "Online")));
            assertEquals("vehicle_slug", error.getField());
        }
    }

    @Test
    void oldVehicleStatusMustBeOnlineOrRepairing() {
        for (String status : new String[]{null, "", "Offline", "Inused", "Deleted", "online", "Bay"}) {
            ValidationException error = assertThrows(ValidationException.class,
                    () -> validator.validate(new ChangeVehicleRequest("xe-gau-02", status)));
            assertEquals("old_vehicle_status", error.getField());
        }
    }
}
