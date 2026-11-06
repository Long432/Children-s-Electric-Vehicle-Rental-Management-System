package org.group6.childrenelectricvehiclerentalmanagement.DTOs;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

/** Toàn bộ cài đặt hiện tại, dùng cho cả GET và kết quả của mọi API cập nhật cài đặt. */
@Getter
public class SettingsResponse {
    @JsonProperty("refund_enabled")
    private final boolean refundEnabled;

    @JsonProperty("extend_enabled")
    private final boolean extendEnabled;

    @JsonProperty("overdue_fee_per_minute")
    private final int overdueFeePerMinute;

    @JsonProperty("overdue_grace_minutes")
    private final int overdueGraceMinutes;

    @JsonProperty("minimum_minutes")
    private final int minimumMinutes;

    private final List<PackageResponse> packages;

    @JsonProperty("updated_at")
    private final String updatedAt;

    public SettingsResponse(boolean refundEnabled, boolean extendEnabled, int overdueFeePerMinute,
                            int overdueGraceMinutes, int minimumMinutes,
                            List<PackageResponse> packages, String updatedAt) {
        this.refundEnabled = refundEnabled;
        this.extendEnabled = extendEnabled;
        this.overdueFeePerMinute = overdueFeePerMinute;
        this.overdueGraceMinutes = overdueGraceMinutes;
        this.minimumMinutes = minimumMinutes;
        this.packages = packages;
        this.updatedAt = updatedAt;
    }
}
