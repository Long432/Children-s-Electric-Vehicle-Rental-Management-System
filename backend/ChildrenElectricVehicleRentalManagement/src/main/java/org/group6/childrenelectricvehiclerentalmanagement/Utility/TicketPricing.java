package org.group6.childrenelectricvehiclerentalmanagement.Utility;

import org.group6.childrenelectricvehiclerentalmanagement.DTOs.PackageResponse;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.ConflictException;

import java.util.List;

public final class TicketPricing {

    public record Quote(int amount, int priceAmount, int priceMinutes) {
    }

    private TicketPricing() {
    }

    public static Quote quote(int minutes, List<PackageResponse> packages) throws ConflictException {
        PackageResponse base = packages.stream()
                .filter(PackageResponse::isDefaultPackage)
                .findFirst()
                .orElseThrow(() -> new ConflictException("NO_PRICE_PACKAGE", "no default package", "Chưa cấu hình gói giá mặc định"));
        if (base.getMinutes() <= 0) {
            throw new ConflictException("NO_PRICE_PACKAGE", "default package has no minutes", "Gói giá mặc định không hợp lệ");
        }
        for (PackageResponse item : packages) {
            if (item.getMinutes() == minutes) {
                return new Quote(item.getAmount(), base.getAmount(), base.getMinutes());
            }
        }
        long numerator = (long) minutes * base.getAmount();
        long amount = (numerator + base.getMinutes() - 1) / base.getMinutes();
        if (amount > Integer.MAX_VALUE) {
            throw new ConflictException("PRICE_TOO_LARGE", "amount overflow", "Số tiền vượt giới hạn cho phép");
        }
        return new Quote((int) amount, base.getAmount(), base.getMinutes());
    }

    public static int charge(int minutes, int priceAmount, int priceMinutes) throws ConflictException {
        if (priceMinutes <= 0) {
            throw new ConflictException("NO_PRICE_PACKAGE", "ticket price snapshot has no minutes", "Vé không có giá gói hợp lệ");
        }
        long amount = ((long) minutes * priceAmount + priceMinutes - 1) / priceMinutes;
        if (amount > Integer.MAX_VALUE) {
            throw new ConflictException("PRICE_TOO_LARGE", "amount overflow", "Số tiền vượt giới hạn cho phép");
        }
        return (int) amount;
    }

    public static int overdueMinutes(long lateSeconds) {
        if (lateSeconds <= 0) {
            return 0;
        }
        return (int) Math.min((lateSeconds + 59) / 60, Integer.MAX_VALUE);
    }

    public static int chargeableOverdueMinutes(int overdueMinutes, int graceMinutes) {
        return Math.max(0, overdueMinutes - graceMinutes);
    }

    public static int estimatedOverdueFee(int chargeableMinutes, int feePerMinute) {
        long fee = (long) chargeableMinutes * feePerMinute;
        return (int) Math.min(fee, Integer.MAX_VALUE);
    }

    public static int proportionalRefund(int amountPaid, int totalMinutes, int remainingMinutes) {
        if (totalMinutes <= 0 || remainingMinutes <= 0 || amountPaid <= 0) {
            return 0;
        }
        int remaining = Math.min(remainingMinutes, totalMinutes);
        return (int) ((long) amountPaid * remaining / totalMinutes);
    }
}
