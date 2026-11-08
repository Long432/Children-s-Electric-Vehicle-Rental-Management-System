package org.group6.childrenelectricvehiclerentalmanagement.integration;

import org.group6.childrenelectricvehiclerentalmanagement.DTOs.SettingsController.AmountPerMinuteRequest;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.SettingsController.MinutesRequest;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.SettingsController.PackageItemRequest;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.SettingsController.SettingsResponse;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.SettingsController.ToggleRequest;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.SettingsController.UpdatePackagesRequest;
import org.group6.childrenelectricvehiclerentalmanagement.Services.SettingsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test cho SettingsService với H2 in-memory database.
 * Luồng kiểm tra: xem cài đặt → toggle_refund → toggle_extend → set_overdue_fee/limit/min → updatePackages.
 */
class SettingsIntegrationTest extends AbstractIntegrationTest {

    @Autowired private SettingsService settingsService;

    @Test
    void getSettings_shouldReturnDefaults() throws Exception {
        SettingsResponse res = settingsService.getSettings();

        assertNotNull(res);
        assertFalse(res.isRefundEnabled());
        assertTrue(res.isExtendEnabled());
        assertEquals(2000, res.getOverdueFeePerMinute());
        assertEquals(5, res.getOverdueGraceMinutes());
        assertEquals(10, res.getMinimumMinutes());
        assertFalse(res.getPackages().isEmpty());
    }

    @Test
    void toggleRefund_shouldFlip() throws Exception {
        ToggleRequest req = new ToggleRequest();
        req.setEnabled(true);

        SettingsResponse res = settingsService.toggleRefund(seller, req);

        assertNotNull(res);
        assertTrue(res.isRefundEnabled());

        // Tắt lại
        req.setEnabled(false);
        res = settingsService.toggleRefund(seller, req);
        assertFalse(res.isRefundEnabled());
    }

    @Test
    void toggleExtend_shouldFlip() throws Exception {
        ToggleRequest req = new ToggleRequest();
        req.setEnabled(false);

        SettingsResponse res = settingsService.toggleExtend(seller, req);

        assertNotNull(res);
        assertFalse(res.isExtendEnabled());
    }

    @Test
    void setOverdueFee_shouldUpdate() throws Exception {
        AmountPerMinuteRequest req = new AmountPerMinuteRequest();
        req.setAmountPerMinute(3000);

        SettingsResponse res = settingsService.setOverdueFee(seller, req);

        assertNotNull(res);
        assertEquals(3000, res.getOverdueFeePerMinute());
    }

    @Test
    void setOverdueLimit_shouldUpdate() throws Exception {
        MinutesRequest req = new MinutesRequest();
        req.setMinutes(10);

        SettingsResponse res = settingsService.setOverdueLimit(seller, req);

        assertNotNull(res);
        assertEquals(10, res.getOverdueGraceMinutes());
    }

    @Test
    void setMinimumTime_shouldUpdate() throws Exception {
        MinutesRequest req = new MinutesRequest();
        req.setMinutes(15);

        SettingsResponse res = settingsService.setMinimumTime(seller, req);

        assertNotNull(res);
        assertEquals(15, res.getMinimumMinutes());
    }
}
