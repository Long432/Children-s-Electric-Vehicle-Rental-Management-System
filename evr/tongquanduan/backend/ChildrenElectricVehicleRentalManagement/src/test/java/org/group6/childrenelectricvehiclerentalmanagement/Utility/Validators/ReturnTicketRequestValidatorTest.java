package org.group6.childrenelectricvehiclerentalmanagement.Utility.Validators;

import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Ticket.ReturnTicketRequest;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.ValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReturnTicketRequestValidatorTest {

    private final ReturnTicketRequestValidator validator = new ReturnTicketRequestValidator();

    @Test
    void missingBodyAndMissingMethodsAreAccepted() {
        assertDoesNotThrow(() -> validator.validate(null));
        assertDoesNotThrow(() -> validator.validate(new ReturnTicketRequest(null, null)));
    }

    @Test
    void cashAndBankAreAccepted() {
        assertDoesNotThrow(() -> validator.validate(new ReturnTicketRequest("Cash", " Bank ")));
    }

    @Test
    void anyOtherMethodIsRejectedWithTheRightField() {
        ValidationException fee = assertThrows(ValidationException.class,
                () -> validator.validate(new ReturnTicketRequest("Momo", null)));
        assertEquals("payment_method", fee.getField());
        assertEquals("INVALID_PAYMENT_METHOD", fee.getCode());
        ValidationException refund = assertThrows(ValidationException.class,
                () -> validator.validate(new ReturnTicketRequest(null, "")));
        assertEquals("refund_method", refund.getField());
        assertEquals("INVALID_PAYMENT_METHOD", refund.getCode());
    }
}
