package org.group6.childrenelectricvehiclerentalmanagement.unit;

import org.group6.childrenelectricvehiclerentalmanagement.Utility.Validators.PricingUtil;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.ValidPackage;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test cho PricingUtil (pure static methods, không cần mock dependency).
 * Dựa trên logic nghiệp vụ trong phân tích nghiệp vụ section 6.1:
 * - Giá mỗi phút tính theo tỷ lệ: giá gói / số phút gói, làm tròn lên.
 * - Phụ phí quá giờ = chargeable_minutes * fee_per_minute.
 * - Hoàn tiền trả sớm = remaining_minutes * rate_amount / rate_minutes, không vượt quá amount_paid.
 */
class PricingUtilTest {

    @Test
    void priceForNewTicket_whenMinutesMatchPackage_shouldReturnPackageAmount() {
        List<ValidPackage> packages = List.of(
                new ValidPackage(15000, 30, false),
                new ValidPackage(30000, 60, true)
        );
        ValidPackage defaultPackage = new ValidPackage(30000, 60, true);

        int price = PricingUtil.priceForNewTicket(30, packages, defaultPackage);
        assertEquals(15000, price);

        price = PricingUtil.priceForNewTicket(60, packages, defaultPackage);
        assertEquals(30000, price);
    }

    @Test
    void priceForNewTicket_whenMinutesNotMatchPackage_shouldUseDefaultPackageRateRoundedUp() {
        // default: 30000/60p => 500 đ/phút
        List<ValidPackage> packages = List.of(
                new ValidPackage(15000, 30, false),
                new ValidPackage(30000, 60, true)
        );
        ValidPackage defaultPackage = new ValidPackage(30000, 60, true);

        // 45 phút => ceil(45 * 30000 / 60) = ceil(22500) = 22500
        int price = PricingUtil.priceForNewTicket(45, packages, defaultPackage);
        assertEquals(22500, price);

        // 1 phút => ceil(1 * 30000 / 60) = ceil(500) = 500
        price = PricingUtil.priceForNewTicket(1, packages, defaultPackage);
        assertEquals(500, price);

        // 61 phút => ceil(61 * 30000 / 60) = ceil(30500) = 30500
        price = PricingUtil.priceForNewTicket(61, packages, defaultPackage);
        assertEquals(30500, price);
    }

    @Test
    void priceByRate_shouldRoundUp() {
        // 30 phút, 15000/30p => 15000
        assertEquals(15000, PricingUtil.priceByRate(30, 15000, 30));

        // 31 phút, 15000/30p => ceil(31 * 15000 / 30) = ceil(15500) = 15500
        assertEquals(15500, PricingUtil.priceByRate(31, 15000, 30));

        // 1 phút, 30000/60p => ceil(500) = 500
        assertEquals(500, PricingUtil.priceByRate(1, 30000, 60));
    }

    @Test
    void remainingMinutes_shouldReturnCorrectRemaining() {
        LocalDateTime now = LocalDateTime.of(2026, 11, 8, 10, 0);

        // dueAt sau now 15 phút
        assertEquals(15, PricingUtil.remainingMinutes(now.plusMinutes(15), now));

        // dueAt trước now => 0
        assertEquals(0, PricingUtil.remainingMinutes(now.minusMinutes(15), now));

        // dueAt null => 0
        assertEquals(0, PricingUtil.remainingMinutes(null, now));

        // dueAt bằng now => 0
        assertEquals(0, PricingUtil.remainingMinutes(now, now));
    }

    @Test
    void overdueMinutes_shouldReturnCorrectOverdue() {
        LocalDateTime now = LocalDateTime.of(2026, 11, 8, 10, 0);

        // now sau dueAt 5 phút 30 giây => ceil(330s/60) = 6 phút
        assertEquals(6, PricingUtil.overdueMinutes(now.minusMinutes(5).minusSeconds(30), now));

        // now trước dueAt => 0
        assertEquals(0, PricingUtil.overdueMinutes(now.plusMinutes(15), now));

        // dueAt null => 0
        assertEquals(0, PricingUtil.overdueMinutes(null, now));

        // now bằng dueAt => 0
        assertEquals(0, PricingUtil.overdueMinutes(now, now));
    }

    @Test
    void chargeableMinutes_shouldSubtractGraceMinutes() {
        assertEquals(7, PricingUtil.chargeableMinutes(12, 5)); // ân hạn 5p, trễ 12p => tính 7p
        assertEquals(0, PricingUtil.chargeableMinutes(3, 5)); // trễ 3p < ân hạn 5p => 0
        assertEquals(0, PricingUtil.chargeableMinutes(0, 5));
        assertEquals(10, PricingUtil.chargeableMinutes(10, 0)); // không có ân hạn
    }

    @Test
    void overdueFee_shouldMultiplyChargeableByRate() {
        assertEquals(14000, PricingUtil.overdueFee(7, 2000)); // 7 * 2000 = 14000
        assertEquals(0, PricingUtil.overdueFee(0, 2000));
        assertEquals(10000, PricingUtil.overdueFee(5, 2000));
    }

    @Test
    void refundForEarlyReturn_shouldNotExceedAmountPaid() {
        // remaining 30 phút, rate 30000/60p, amountPaid 30000 => 30 * 30000 / 60 = 15000
        assertEquals(15000, PricingUtil.refundForEarlyReturn(30, 30000, 60, 30000));

        // remaining 60 phút, rate 30000/60p, amountPaid 30000 => 60 * 30000 / 60 = 30000 => không vượt quá amountPaid
        assertEquals(30000, PricingUtil.refundForEarlyReturn(60, 30000, 60, 30000));

        // remaining 120 phút, rate 30000/60p, amountPaid 30000 => 60000 > amountPaid => giới hạn 30000
        assertEquals(30000, PricingUtil.refundForEarlyReturn(120, 30000, 60, 30000));

        // remaining 0 => 0
        assertEquals(0, PricingUtil.refundForEarlyReturn(0, 30000, 60, 30000));
    }
}
