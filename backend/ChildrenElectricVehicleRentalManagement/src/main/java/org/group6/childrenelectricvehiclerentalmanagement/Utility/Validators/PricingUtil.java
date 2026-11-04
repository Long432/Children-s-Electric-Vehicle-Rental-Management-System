package org.group6.childrenelectricvehiclerentalmanagement.Utility.Validators;

import org.group6.childrenelectricvehiclerentalmanagement.Utility.ValidPackage;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public final class PricingUtil {
    private static final long MAX_MONEY = 2_000_000_000L;

    private PricingUtil() {}

    public static int priceByRate(int minutes, int rateAmount, int rateMinutes) {
        long raw = (long) minutes * rateAmount;
        long price = (raw + rateMinutes - 1) / rateMinutes;
        return (int) Math.min(price, MAX_MONEY);
    }

    public static int priceForNewTicket(int minutes, List<ValidPackage> packages, ValidPackage defaultPackage) {
        for (ValidPackage p : packages) {
            if (p.minutes() == minutes) {
                return p.amount();
            }
        }
        return priceByRate(minutes, defaultPackage.amount(), defaultPackage.minutes());
    }

    public static int remainingMinutes(LocalDateTime dueAt, LocalDateTime now) {
        if (dueAt == null || !dueAt.isAfter(now)) {
            return 0;
        }
        return (int) Math.min(Duration.between(now, dueAt).toMinutes(), Integer.MAX_VALUE);
    }

    public static int overdueMinutes(LocalDateTime dueAt, LocalDateTime now) {
        if (dueAt == null || !now.isAfter(dueAt)) {
            return 0;
        }
        long seconds = Duration.between(dueAt, now).getSeconds();
        long minutes = (seconds + 59) / 60;
        return (int) Math.min(minutes, Integer.MAX_VALUE);
    }

    public static int chargeableMinutes(int overdueMinutes, int graceMinutes) {
        return Math.max(0, overdueMinutes - graceMinutes);
    }

    public static int overdueFee(int chargeableMinutes, int feePerMinute) {
        long fee = (long) chargeableMinutes * feePerMinute;
        return (int) Math.min(fee, MAX_MONEY);
    }

    public static int refundForEarlyReturn(int remainingMinutes, int rateAmount, int rateMinutes, int amountPaid) {
        long refund = ((long) remainingMinutes * rateAmount) / rateMinutes;
        return (int) Math.min(refund, amountPaid);
    }
}
