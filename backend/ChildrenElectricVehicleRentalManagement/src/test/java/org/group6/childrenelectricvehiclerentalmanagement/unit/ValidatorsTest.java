package org.group6.childrenelectricvehiclerentalmanagement.unit;

import org.group6.childrenelectricvehiclerentalmanagement.Enum.PaymentMethod;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.ValidationException;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.Validators.IdValidator;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.Validators.MinutesValidator;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.Validators.PaymentMethodValidator;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.Validators.PhoneValidator;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.Validators.VehicleSlugValidator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test cho các Validator (pure static methods, không cần mock dependency).
 * Quy tắc slug dựa trên phân tích nghiệp vụ section 4.2:
 * ^(?=.{3,50}$)[a-z][a-z0-9]*(?:-[a-z0-9]+)*$
 */
class ValidatorsTest {

    // ==================== PhoneValidator ====================

    @Test
    void phoneValidator_validNumbers_shouldPass() throws Exception {
        assertEquals("0901234567", PhoneValidator.validate("0901234567"));
        assertEquals("+84901234567", PhoneValidator.validate("+84901234567"));
        assertEquals("09012345678", PhoneValidator.validate(" 09012345678 "));
    }

    @Test
    void phoneValidator_invalidNumbers_shouldThrow() {
        assertThrows(ValidationException.class, () -> PhoneValidator.validate(null));
        assertThrows(ValidationException.class, () -> PhoneValidator.validate(""));
        assertThrows(ValidationException.class, () -> PhoneValidator.validate("   "));
        assertThrows(ValidationException.class, () -> PhoneValidator.validate("090123456")); // ít hơn 9 số sau 0
        assertThrows(ValidationException.class, () -> PhoneValidator.validate("abc1234567"));
        assertThrows(ValidationException.class, () -> PhoneValidator.validate("090-1234-567"));
    }

    // ==================== MinutesValidator ====================

    @Test
    void minutesValidator_positive_valid_shouldPass() throws Exception {
        assertEquals(30, MinutesValidator.validatePositive("minutes", "30"));
        assertEquals(1, MinutesValidator.validatePositive("minutes", "1"));
        assertEquals(1440, MinutesValidator.validatePositive("minutes", "1440"));
    }

    @Test
    void minutesValidator_positive_invalid_shouldThrow() {
        assertThrows(ValidationException.class, () -> MinutesValidator.validatePositive("minutes", null));
        assertThrows(ValidationException.class, () -> MinutesValidator.validatePositive("minutes", ""));
        assertThrows(ValidationException.class, () -> MinutesValidator.validatePositive("minutes", "0"));
        assertThrows(ValidationException.class, () -> MinutesValidator.validatePositive("minutes", "-5"));
        assertThrows(ValidationException.class, () -> MinutesValidator.validatePositive("minutes", "1441"));
        assertThrows(ValidationException.class, () -> MinutesValidator.validatePositive("minutes", "abc"));
    }

    // ==================== PaymentMethodValidator ====================

    @Test
    void paymentMethodValidator_valid_shouldPass() throws Exception {
        assertEquals(PaymentMethod.Cash, PaymentMethodValidator.validate("Cash"));
        assertEquals(PaymentMethod.Bank, PaymentMethodValidator.validate("Bank"));
        assertEquals(PaymentMethod.Cash, PaymentMethodValidator.validate("cash"));
        assertEquals(PaymentMethod.Bank, PaymentMethodValidator.validate(" BANK "));
    }

    @Test
    void paymentMethodValidator_invalid_shouldThrow() {
        assertThrows(ValidationException.class, () -> PaymentMethodValidator.validate(null));
        assertThrows(ValidationException.class, () -> PaymentMethodValidator.validate(""));
        assertThrows(ValidationException.class, () -> PaymentMethodValidator.validate("CreditCard"));
        assertThrows(ValidationException.class, () -> PaymentMethodValidator.validate("MoMo"));
    }

    @Test
    void paymentMethodValidator_optional_shouldReturnNullForEmpty() throws Exception {
        assertNull(PaymentMethodValidator.validateOptional(null));
        assertNull(PaymentMethodValidator.validateOptional(""));
        assertEquals(PaymentMethod.Cash, PaymentMethodValidator.validateOptional("Cash"));
    }

    // ==================== VehicleSlugValidator ====================

    @Test
    void vehicleSlugValidator_validSlugs_shouldPass() throws Exception {
        assertEquals("xe-gau-03", VehicleSlugValidator.validate("xe-gau-03"));
        assertEquals("a", VehicleSlugValidator.validate("a"));
        assertEquals("xe1", VehicleSlugValidator.validate("xe1"));
        assertEquals("xe-1", VehicleSlugValidator.validate("xe-1"));
        assertEquals("abc123-def456", VehicleSlugValidator.validate("abc123-def456"));
    }

    @Test
    void vehicleSlugValidator_invalidSlugs_shouldThrow() {
        assertThrows(ValidationException.class, () -> VehicleSlugValidator.validate(null));
        assertThrows(ValidationException.class, () -> VehicleSlugValidator.validate(""));
        assertThrows(ValidationException.class, () -> VehicleSlugValidator.validate("Xe-Gau")); // chữ hoa
        assertThrows(ValidationException.class, () -> VehicleSlugValidator.validate("xe gau")); // khoảng trắng
        assertThrows(ValidationException.class, () -> VehicleSlugValidator.validate("xe_gau")); // gạch dưới
        assertThrows(ValidationException.class, () -> VehicleSlugValidator.validate("-xe")); // bắt đầu bằng -
        assertThrows(ValidationException.class, () -> VehicleSlugValidator.validate("1xe")); // bắt đầu bằng số
        assertThrows(ValidationException.class, () -> VehicleSlugValidator.validate("xe--gau")); // hai gạch ngang liên tiếp
        assertThrows(ValidationException.class, () -> VehicleSlugValidator.validate("xe-")); // kết thúc bằng -
    }

    // ==================== IdValidator ====================

    @Test
    void idValidator_validIds_shouldPass() throws Exception {
        assertEquals(1L, IdValidator.validate("id", "1"));
        assertEquals(100L, IdValidator.validate("id", "100"));
        assertEquals(1L, IdValidator.validate("id", " 1 "));
    }

    @Test
    void idValidator_invalidIds_shouldThrow() {
        assertThrows(ValidationException.class, () -> IdValidator.validate("id", null));
        assertThrows(ValidationException.class, () -> IdValidator.validate("id", ""));
        assertThrows(ValidationException.class, () -> IdValidator.validate("id", "0"));
        assertThrows(ValidationException.class, () -> IdValidator.validate("id", "-1"));
        assertThrows(ValidationException.class, () -> IdValidator.validate("id", "abc"));
    }

    @Test
    void idValidator_optional_shouldReturnNullForEmpty() throws Exception {
        assertNull(IdValidator.validateOptional("session_id", null));
        assertNull(IdValidator.validateOptional("session_id", ""));
        assertEquals(5L, IdValidator.validateOptional("session_id", "5"));
    }
}
