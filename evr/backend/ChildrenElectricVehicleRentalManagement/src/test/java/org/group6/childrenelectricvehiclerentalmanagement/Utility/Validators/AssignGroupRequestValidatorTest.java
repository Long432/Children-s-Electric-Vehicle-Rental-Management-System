package org.group6.childrenelectricvehiclerentalmanagement.Utility.Validators;

import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Ticket.AssignGroupRequest;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.ValidationException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AssignGroupRequestValidatorTest {

    private final AssignGroupRequestValidator validator = new AssignGroupRequestValidator();

    @Test
    void validListPasses() {
        assertDoesNotThrow(() -> validator.validate(new AssignGroupRequest(List.of("xe-gau-01", " xe-meo-02 "))));
    }

    @Test
    void missingBodyOrEmptyListIsRejected() {
        assertThrows(ValidationException.class, () -> validator.validate(null));
        assertThrows(ValidationException.class, () -> validator.validate(new AssignGroupRequest(null)));
        assertThrows(ValidationException.class, () -> validator.validate(new AssignGroupRequest(List.of())));
    }

    @Test
    void duplicateVehiclesAreRejected() {
        ValidationException error = assertThrows(ValidationException.class,
                () -> validator.validate(new AssignGroupRequest(List.of("xe-gau-01", " xe-gau-01"))));
        assertEquals("vehicle_slugs", error.getField());
    }

    @Test
    void badSlugIsRejected() {
        assertThrows(ValidationException.class, () -> validator.validate(new AssignGroupRequest(List.of("Xe Gau"))));
    }

    @Test
    void tooManyVehiclesAreRejected() {
        List<String> slugs = List.of("xe-a", "xe-b", "xe-c", "xe-d", "xe-e", "xe-f");
        assertThrows(ValidationException.class, () -> validator.validate(new AssignGroupRequest(slugs)));
    }
}
