package org.group6.childrenelectricvehiclerentalmanagement.DTOs.SettingsController;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

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

    @JsonProperty("cancel_wait_minutes")
    private final int cancelWaitMinutes;

    private final List<PackageResponse> packages;

    @JsonProperty("updated_at")
    private final String updatedAt;

    public SettingsResponse(boolean refundEnabled, boolean extendEnabled, int overdueFeePerMinute,
                            int overdueGraceMinutes, int minimumMinutes, int cancelWaitMinutes,
                            List<PackageResponse> packages, String updatedAt) {
        this.refundEnabled = refundEnabled;
        this.extendEnabled = extendEnabled;
        this.overdueFeePerMinute = overdueFeePerMinute;
        this.overdueGraceMinutes = overdueGraceMinutes;
        this.minimumMinutes = minimumMinutes;
        this.cancelWaitMinutes = cancelWaitMinutes;
        this.packages = packages;
        this.updatedAt = updatedAt;
    }
}
