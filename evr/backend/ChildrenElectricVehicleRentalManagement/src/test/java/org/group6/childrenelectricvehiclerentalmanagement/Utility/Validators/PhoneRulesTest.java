package org.group6.childrenelectricvehiclerentalmanagement.Utility.Validators;

import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PhoneRulesTest {

    @ParameterizedTest
    @ValueSource(strings = {"0312345678", "0512345678", "0712345678", "0812345678", "0912345678", "0901234567"})
    void validNumbersPass(String phone) throws ValidationException {
        assertEquals(phone, PhoneRules.check(phone));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "0212345678", "0412345678", "0612345678", "1912345678", "912345678",
            "09123456789", "091234567", "09123abc78", "0912 345 67x", "+8409123456789", "0912345678a"})
    void invalidNumbersAreRejected(String phone) {
        ValidationException error = assertThrows(ValidationException.class, () -> PhoneRules.check(phone));
        assertEquals("phone", error.getField());
    }

    @Test
    void separatorsAreRemoved() {
        assertEquals("0912345678", PhoneRules.normalize(" 0912 345 678 "));
        assertEquals("0912345678", PhoneRules.normalize("0912.345.678"));
        assertEquals("0912345678", PhoneRules.normalize("0912-345-678"));
    }

    @Test
    void countryCodeBecomesLeadingZero() throws ValidationException {
        assertEquals("0912345678", PhoneRules.normalize("+84912345678"));
        assertEquals("0912345678", PhoneRules.normalize("+84 912 345 678"));
        assertEquals("0912345678", PhoneRules.normalize("84912345678"));
        assertEquals("0912345678", PhoneRules.check("+84-912-345-678"));
    }

    @Test
    void shortSearchFragmentsAreLeftAlone() {
        assertEquals("8412", PhoneRules.normalize("8412"));
        assertEquals("84567", PhoneRules.normalize("84567"));
    }

    @Test
    void elevenDigitNumberIsRejected() {
        assertThrows(ValidationException.class, () -> PhoneRules.check("09123456789"));
    }

    @Test
    void nullNormalizesToEmpty() {
        assertEquals("", PhoneRules.normalize(null));
    }
}
