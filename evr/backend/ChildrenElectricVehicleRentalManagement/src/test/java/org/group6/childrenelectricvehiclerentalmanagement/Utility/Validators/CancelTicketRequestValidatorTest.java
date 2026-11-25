package org.group6.childrenelectricvehiclerentalmanagement.Utility.Validators;

import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Ticket.CancelTicketRequest;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.ValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CancelTicketRequestValidatorTest {

    private final CancelTicketRequestValidator validator = new CancelTicketRequestValidator();

    @Test
    void missingBodyOrMissingMethodIsAccepted() {
        assertDoesNotThrow(() -> validator.validate(null));
        assertDoesNotThrow(() -> validator.validate(new CancelTicketRequest(null)));
    }

    @Test
    void cashAndBankAreAccepted() {
        assertDoesNotThrow(() -> validator.validate(new CancelTicketRequest("Cash")));
        assertDoesNotThrow(() -> validator.validate(new CancelTicketRequest(" Bank ")));
    }

    @Test
    void anyOtherMethodIsRejected() {
        for (String method : new String[]{"", "  ", "cash", "BANK", "Momo", "Refund"}) {
            ValidationException error = assertThrows(ValidationException.class,
                    () -> validator.validate(new CancelTicketRequest(method)));
            assertEquals("refund_method", error.getField());
            assertEquals("INVALID_PAYMENT_METHOD", error.getCode());
        }
    }
}
