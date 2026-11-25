package org.group6.childrenelectricvehiclerentalmanagement.Utility;

import org.group6.childrenelectricvehiclerentalmanagement.DTOs.PackageResponse;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.ConflictException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TicketPricingTest {

    private final List<PackageResponse> packages = List.of(
            new PackageResponse(1L, 30000, 15, false),
            new PackageResponse(2L, 50000, 30, true),
            new PackageResponse(3L, 90000, 60, false));

    @Test
    void exactPackageUsesItsOwnPrice() throws Exception {
        TicketPricing.Quote quote = TicketPricing.quote(60, packages);
        assertEquals(90000, quote.amount());
        assertEquals(50000, quote.priceAmount());
        assertEquals(30, quote.priceMinutes());
    }

    @Test
    void otherMinutesUseTheDefaultPackageRate() throws Exception {
        assertEquals(75000, TicketPricing.quote(45, packages).amount());
    }

    @Test
    void fractionsAreRoundedUpToWholeDong() throws Exception {
        assertEquals(11667, TicketPricing.quote(7, packages).amount());
    }

    @Test
    void missingDefaultPackageIsRejected() {
        List<PackageResponse> noDefault = List.of(new PackageResponse(1L, 30000, 15, false));
        ConflictException error = assertThrows(ConflictException.class, () -> TicketPricing.quote(15, noDefault));
        assertEquals("NO_PRICE_PACKAGE", error.getCode());
    }

    @Test
    void chargeUsesTheTicketSnapshotRateAndRoundsUp() throws Exception {
        assertEquals(25000, TicketPricing.charge(15, 50000, 30));
        assertEquals(11667, TicketPricing.charge(7, 50000, 30));
    }

    @Test
    void chargeRejectsABrokenSnapshot() {
        ConflictException error = assertThrows(ConflictException.class, () -> TicketPricing.charge(15, 50000, 0));
        assertEquals("NO_PRICE_PACKAGE", error.getCode());
    }

    @Test
    void chargeRejectsAnAmountThatOverflows() {
        ConflictException error = assertThrows(ConflictException.class, () -> TicketPricing.charge(1440, Integer.MAX_VALUE, 1));
        assertEquals("PRICE_TOO_LARGE", error.getCode());
    }

    @Test
    void proportionalRefundRoundsDownAndNeverExceedsWhatWasPaid() {
        assertEquals(16666, TicketPricing.proportionalRefund(50000, 30, 10));
        assertEquals(50000, TicketPricing.proportionalRefund(50000, 30, 99));
        assertEquals(0, TicketPricing.proportionalRefund(50000, 30, 0));
        assertEquals(0, TicketPricing.proportionalRefund(0, 30, 10));
        assertEquals(0, TicketPricing.proportionalRefund(50000, 0, 10));
    }

    @Test
    void overdueMinutesRoundUpAndIgnoreNonPositiveLateness() {
        assertEquals(0, TicketPricing.overdueMinutes(-30));
        assertEquals(0, TicketPricing.overdueMinutes(0));
        assertEquals(1, TicketPricing.overdueMinutes(1));
        assertEquals(1, TicketPricing.overdueMinutes(60));
        assertEquals(2, TicketPricing.overdueMinutes(61));
    }

    @Test
    void chargeableOverdueMinutesSubtractGraceAndNeverGoNegative() {
        assertEquals(0, TicketPricing.chargeableOverdueMinutes(3, 5));
        assertEquals(0, TicketPricing.chargeableOverdueMinutes(5, 5));
        assertEquals(7, TicketPricing.chargeableOverdueMinutes(12, 5));
    }

    @Test
    void estimatedOverdueFeeMultipliesAndCapsAtIntegerMax() {
        assertEquals(14000, TicketPricing.estimatedOverdueFee(7, 2000));
        assertEquals(0, TicketPricing.estimatedOverdueFee(0, 2000));
        assertEquals(Integer.MAX_VALUE, TicketPricing.estimatedOverdueFee(Integer.MAX_VALUE, 2000));
    }
}
