package org.group6.childrenelectricvehiclerentalmanagement.Utility.Validators;

import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Auth.LoginRequest;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.ValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LoginRequestValidatorTest {

    private final LoginRequestValidator validator = new LoginRequestValidator();

    private LoginRequest withPassword(String password) {
        return new LoginRequest("banhang", password);
    }

    @Test
    void asciiPasswordOfSeventyTwoCharactersIsAccepted() {
        assertDoesNotThrow(() -> validator.validate(withPassword("a".repeat(72))));
    }

    @Test
    void asciiPasswordOfSeventyThreeCharactersIsRejected() {
        assertThrows(ValidationException.class, () -> validator.validate(withPassword("a".repeat(73))));
    }

    @Test
    void twoByteCharactersAreCountedByBytes() {
        assertDoesNotThrow(() -> validator.validate(withPassword("\u00e1".repeat(36))));
        assertThrows(ValidationException.class, () -> validator.validate(withPassword("\u00e1".repeat(37))));
    }

    @Test
    void threeByteCharactersAreCountedByBytes() {
        assertDoesNotThrow(() -> validator.validate(withPassword("\u1ebf".repeat(24))));
        assertThrows(ValidationException.class, () -> validator.validate(withPassword("\u1ebf".repeat(25))));
    }

    @Test
    void emptyAndMissingPasswordsAreRejected() {
        assertThrows(ValidationException.class, () -> validator.validate(withPassword("")));
        assertThrows(ValidationException.class, () -> validator.validate(withPassword(null)));
    }
}
