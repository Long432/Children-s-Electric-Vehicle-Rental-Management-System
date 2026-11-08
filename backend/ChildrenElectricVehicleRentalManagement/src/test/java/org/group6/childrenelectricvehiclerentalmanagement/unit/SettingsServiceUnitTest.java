package org.group6.childrenelectricvehiclerentalmanagement.unit;

import org.group6.childrenelectricvehiclerentalmanagement.DTOs.SettingsController.AmountPerMinuteRequest;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.SettingsController.MinutesRequest;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.SettingsController.PackageItemRequest;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.SettingsController.SettingsResponse;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.SettingsController.ToggleRequest;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.SettingsController.UpdatePackagesRequest;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.ValidationException;
import org.group6.childrenelectricvehiclerentalmanagement.Models.Package;
import org.group6.childrenelectricvehiclerentalmanagement.Models.Settings;
import org.group6.childrenelectricvehiclerentalmanagement.Models.User;
import org.group6.childrenelectricvehiclerentalmanagement.Repositories.PackageRepository;
import org.group6.childrenelectricvehiclerentalmanagement.Repositories.SettingsRepository;
import org.group6.childrenelectricvehiclerentalmanagement.Repositories.UserRepository;
import org.group6.childrenelectricvehiclerentalmanagement.Repositories.AuditLogRepository;
import org.group6.childrenelectricvehiclerentalmanagement.Services.SettingsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit test cho SettingsService. Dependencies được mock.
 * Dựa trên nghiệp vụ cài đặt (features.md section 2.4, phân tích nghiệp vụ section 2.4, 6.1).
 */
@ExtendWith(MockitoExtension.class)
class SettingsServiceUnitTest {

    @Mock private SettingsRepository settingsRepository;
    @Mock private PackageRepository packageRepository;
    @Mock private UserRepository userRepository;
    @Mock private org.group6.childrenelectricvehiclerentalmanagement.Services.AuditLogService auditLogService;
    @InjectMocks private SettingsService settingsService;

    private User actor;
    private Settings settings;

    @BeforeEach
    void setUp() {
        actor = new User();
        actor.setId(1L);
        actor.setUsername("seller");

        settings = new Settings();
        settings.setId((short) 1);
        settings.setRefundEnabled(false);
        settings.setExtendEnabled(true);
        settings.setOverdueFeePerMinute(0);
        settings.setOverdueGraceMinutes(0);
        settings.setMinimumMinutes(0);
        settings.setCreatedAt(LocalDateTime.now());
        settings.setUpdatedAt(LocalDateTime.now());
    }

    // ==================== getSettings ====================

    @Test
    void getSettings_existing_shouldReturnSettings() throws Exception {
        when(settingsRepository.findById((short) 1)).thenReturn(Optional.of(settings));
        when(packageRepository.findByActiveTrueOrderByMinutesAsc()).thenReturn(List.of());

        SettingsResponse res = settingsService.getSettings();

        assertNotNull(res);
        assertFalse(res.isRefundEnabled());
        assertTrue(res.isExtendEnabled());
    }

    @Test
    void getSettings_notExisting_shouldCreateDefault() throws Exception {
        when(settingsRepository.findById((short) 1)).thenReturn(Optional.empty());
        when(settingsRepository.save(any(Settings.class))).thenAnswer(inv -> {
            Settings s = inv.getArgument(0);
            s.setId((short) 1);
            return s;
        });
        when(packageRepository.findByActiveTrueOrderByMinutesAsc()).thenReturn(List.of());

        SettingsResponse res = settingsService.getSettings();

        assertNotNull(res);
        assertFalse(res.isRefundEnabled());
        assertTrue(res.isExtendEnabled());
        verify(settingsRepository).save(any(Settings.class));
    }

    // ==================== toggleRefund ====================

    @Test
    void toggleRefund_enable_shouldFlip() throws Exception {
        when(settingsRepository.findById((short) 1)).thenReturn(Optional.of(settings));
        when(settingsRepository.save(any(Settings.class))).thenReturn(settings);
        when(userRepository.getReferenceById(1L)).thenReturn(actor);

        ToggleRequest req = new ToggleRequest();
        req.setEnabled(true);

        SettingsResponse res = settingsService.toggleRefund(actor, req);

        assertNotNull(res);
        assertTrue(res.isRefundEnabled());
    }

    @Test
    void toggleRefund_nullEnabled_shouldThrowValidationException() throws Exception {
        when(settingsRepository.findById((short) 1)).thenReturn(Optional.of(settings));

        ToggleRequest req = new ToggleRequest();
        req.setEnabled(null);

        assertThrows(ValidationException.class, () -> settingsService.toggleRefund(actor, req));
    }

    // ==================== toggleExtend ====================

    @Test
    void toggleExtend_disable_shouldFlip() throws Exception {
        when(settingsRepository.findById((short) 1)).thenReturn(Optional.of(settings));
        when(settingsRepository.save(any(Settings.class))).thenReturn(settings);
        when(userRepository.getReferenceById(1L)).thenReturn(actor);

        ToggleRequest req = new ToggleRequest();
        req.setEnabled(false);

        SettingsResponse res = settingsService.toggleExtend(actor, req);

        assertNotNull(res);
        assertFalse(res.isExtendEnabled());
    }

    // ==================== setOverdueFee ====================

    @Test
    void setOverdueFee_shouldUpdate() throws Exception {
        when(settingsRepository.findById((short) 1)).thenReturn(Optional.of(settings));
        when(settingsRepository.save(any(Settings.class))).thenReturn(settings);
        when(userRepository.getReferenceById(1L)).thenReturn(actor);

        AmountPerMinuteRequest req = new AmountPerMinuteRequest();
        req.setAmountPerMinute(2000);

        SettingsResponse res = settingsService.setOverdueFee(actor, req);

        assertNotNull(res);
        assertEquals(2000, res.getOverdueFeePerMinute());
    }

    @Test
    void setOverdueFee_negative_shouldThrowValidationException() throws Exception {
        when(settingsRepository.findById((short) 1)).thenReturn(Optional.of(settings));

        AmountPerMinuteRequest req = new AmountPerMinuteRequest();
        req.setAmountPerMinute(-1000);

        assertThrows(ValidationException.class, () -> settingsService.setOverdueFee(actor, req));
    }

    // ==================== setOverdueLimit ====================

    @Test
    void setOverdueLimit_shouldUpdate() throws Exception {
        when(settingsRepository.findById((short) 1)).thenReturn(Optional.of(settings));
        when(settingsRepository.save(any(Settings.class))).thenReturn(settings);
        when(userRepository.getReferenceById(1L)).thenReturn(actor);

        MinutesRequest req = new MinutesRequest();
        req.setMinutes(10);

        SettingsResponse res = settingsService.setOverdueLimit(actor, req);

        assertNotNull(res);
        assertEquals(10, res.getOverdueGraceMinutes());
    }

    // ==================== setMinimumTime ====================

    @Test
    void setMinimumTime_shouldUpdate() throws Exception {
        when(settingsRepository.findById((short) 1)).thenReturn(Optional.of(settings));
        when(settingsRepository.save(any(Settings.class))).thenReturn(settings);
        when(userRepository.getReferenceById(1L)).thenReturn(actor);

        MinutesRequest req = new MinutesRequest();
        req.setMinutes(15);

        SettingsResponse res = settingsService.setMinimumTime(actor, req);

        assertNotNull(res);
        assertEquals(15, res.getMinimumMinutes());
    }

    // ==================== updatePackages ====================

    @Test
    void updatePackages_shouldReplaceAll() throws Exception {
        Package oldPkg = new Package();
        oldPkg.setId(1L);
        oldPkg.setAmount(15000);
        oldPkg.setMinutes(30);
        oldPkg.setIsDefault(true);
        oldPkg.setActive(true);

        when(packageRepository.findByActiveTrueOrderByMinutesAsc()).thenReturn(List.of(oldPkg));
        when(packageRepository.saveAllAndFlush(anyList())).thenAnswer(inv -> inv.getArgument(0));
        when(settingsRepository.findById((short) 1)).thenReturn(Optional.of(settings));
        when(settingsRepository.save(any(Settings.class))).thenReturn(settings);
        when(userRepository.getReferenceById(1L)).thenReturn(actor);

        PackageItemRequest item = new PackageItemRequest();
        item.setAmount(30000);
        item.setMinutes(60);
        item.setIsDefault(true);

        UpdatePackagesRequest req = new UpdatePackagesRequest();
        req.setPackages(List.of(item));

        SettingsResponse res = settingsService.updatePackages(actor, req);

        assertNotNull(res);
        verify(packageRepository).saveAllAndFlush(anyList());
    }

    @Test
    void updatePackages_empty_shouldThrowValidationException() throws Exception {
        when(packageRepository.findByActiveTrueOrderByMinutesAsc()).thenReturn(List.of());

        UpdatePackagesRequest req = new UpdatePackagesRequest();
        req.setPackages(List.of());

        assertThrows(ValidationException.class, () -> settingsService.updatePackages(actor, req));
    }

    @Test
    void updatePackages_noDefault_shouldThrowValidationException() throws Exception {
        PackageItemRequest item = new PackageItemRequest();
        item.setAmount(15000);
        item.setMinutes(30);
        item.setIsDefault(false);

        UpdatePackagesRequest req = new UpdatePackagesRequest();
        req.setPackages(List.of(item));

        assertThrows(ValidationException.class, () -> settingsService.updatePackages(actor, req));
    }
}
