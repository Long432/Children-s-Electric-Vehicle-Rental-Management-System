package org.group6.childrenelectricvehiclerentalmanagement.Utility.Validators;

import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Ticket.ExtendTicketRequest;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.ValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ExtendTicketRequestValidatorTest {

    private final ExtendTicketRequestValidator validator = new ExtendTicketRequestValidator();

    @Test
    void validRequestIsAccepted() {
        assertDoesNotThrow(() -> validator.validate(new ExtendTicketRequest(15, "Cash")));
        assertDoesNotThrow(() -> validator.validate(new ExtendTicketRequest(1440, " Bank ")));
    }

    @Test
    void missingBodyOrMinutesIsRejected() {
        assertEquals("body", assertThrows(ValidationException.class, () -> validator.validate(null)).getField());
        assertEquals("minutes", assertThrows(ValidationException.class,
                () -> validator.validate(new ExtendTicketRequest(null, "Cash"))).getField());
    }

    @Test
    void minutesOutOfRangeAreRejected() {
        for (int minutes : new int[]{-5, 0, 1441}) {
            ValidationException error = assertThrows(ValidationException.class,
                    () -> validator.validate(new ExtendTicketRequest(minutes, "Cash")));
            assertEquals("minutes", error.getField());
        }
    }

    @Test
    void paymentMethodIsRequiredAndMustBeCashOrBank() {
        for (String method : new String[]{null, "", "cash", "Momo"}) {
            ValidationException error = assertThrows(ValidationException.class,
                    () -> validator.validate(new ExtendTicketRequest(15, method)));
            assertEquals("payment_method", error.getField());
            assertEquals("INVALID_PAYMENT_METHOD", error.getCode());
        }
    }
}
