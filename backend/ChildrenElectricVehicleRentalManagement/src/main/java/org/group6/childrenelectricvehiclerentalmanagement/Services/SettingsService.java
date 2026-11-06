package org.group6.childrenelectricvehiclerentalmanagement.Services;

import org.group6.childrenelectricvehiclerentalmanagement.DTOs.AmountPerMinuteRequest;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.MinutesRequest;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.PackageItemRequest;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.PackageResponse;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.SettingsResponse;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.ToggleRequest;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.UpdatePackagesRequest;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.ValidationException;
import org.group6.childrenelectricvehiclerentalmanagement.Models.Package;
import org.group6.childrenelectricvehiclerentalmanagement.Models.Settings;
import org.group6.childrenelectricvehiclerentalmanagement.Repositories.PackageRepository;
import org.group6.childrenelectricvehiclerentalmanagement.Repositories.SettingsRepository;
import org.group6.childrenelectricvehiclerentalmanagement.Repositories.UserRepository;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.AmountPerMinuteRequestValidator;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.MinutesRequestValidator;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.Security.AuthenticatedUser;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.TimeUtil;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.ToggleRequestValidator;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.UpdatePackagesRequestValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class SettingsService {

    private static final Short SETTINGS_ID = 1;
    private static final String ENTITY_TYPE = "settings";

    private static final ToggleRequestValidator TOGGLE_VALIDATOR = new ToggleRequestValidator();
    private static final UpdatePackagesRequestValidator PACKAGES_VALIDATOR = new UpdatePackagesRequestValidator();
    private static final AmountPerMinuteRequestValidator OVERDUE_FEE_VALIDATOR = new AmountPerMinuteRequestValidator();
    private static final MinutesRequestValidator OVERDUE_LIMIT_VALIDATOR =
            new MinutesRequestValidator("Thời gian ân hạn", 0, MinutesRequestValidator.MAX_MINUTES);
    private static final MinutesRequestValidator MINIMUM_TIME_VALIDATOR =
            new MinutesRequestValidator("Thời gian thuê tối thiểu", 0, MinutesRequestValidator.MAX_MINUTES);

    private final SettingsRepository settingsRepository;
    private final PackageRepository packageRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    public SettingsService(SettingsRepository settingsRepository,
                           PackageRepository packageRepository,
                           UserRepository userRepository,
                           AuditLogService auditLogService) {
        this.settingsRepository = settingsRepository;
        this.packageRepository = packageRepository;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional(rollbackFor = Exception.class)
    public SettingsResponse getSettings() {
        return toResponse(loadOrCreate());
    }

    @Transactional(rollbackFor = Exception.class)
    public SettingsResponse toggleRefund(AuthenticatedUser actor, ToggleRequest request) throws ValidationException {
        TOGGLE_VALIDATOR.validate(request);
        Settings settings = loadOrCreate();
        boolean before = settings.getRefundEnabled();
        settings.setRefundEnabled(request.getEnabled());
        saveSettings(settings, actor);
        audit(actor, "settings.toggle_refund", "refund_enabled", before, settings.getRefundEnabled());
        return toResponse(settings);
    }

    @Transactional(rollbackFor = Exception.class)
    public SettingsResponse toggleExtend(AuthenticatedUser actor, ToggleRequest request) throws ValidationException {
        TOGGLE_VALIDATOR.validate(request);
        Settings settings = loadOrCreate();
        boolean before = settings.getExtendEnabled();
        settings.setExtendEnabled(request.getEnabled());
        saveSettings(settings, actor);
        audit(actor, "settings.toggle_extend", "extend_enabled", before, settings.getExtendEnabled());
        return toResponse(settings);
    }

    @Transactional(rollbackFor = Exception.class)
    public SettingsResponse setOverdueFee(AuthenticatedUser actor, AmountPerMinuteRequest request) throws ValidationException {
        OVERDUE_FEE_VALIDATOR.validate(request);
        Settings settings = loadOrCreate();
        int before = settings.getOverdueFeePerMinute();
        settings.setOverdueFeePerMinute(request.getAmountPerMinute());
        saveSettings(settings, actor);
        audit(actor, "settings.set_overdue_fee", "overdue_fee_per_minute", before, settings.getOverdueFeePerMinute());
        return toResponse(settings);
    }

    @Transactional(rollbackFor = Exception.class)
    public SettingsResponse setOverdueLimit(AuthenticatedUser actor, MinutesRequest request) throws ValidationException {
        OVERDUE_LIMIT_VALIDATOR.validate(request);
        Settings settings = loadOrCreate();
        int before = settings.getOverdueGraceMinutes();
        settings.setOverdueGraceMinutes(request.getMinutes());
        saveSettings(settings, actor);
        audit(actor, "settings.set_overdue_limit", "overdue_grace_minutes", before, settings.getOverdueGraceMinutes());
        return toResponse(settings);
    }

    @Transactional(rollbackFor = Exception.class)
    public SettingsResponse setMinimumTime(AuthenticatedUser actor, MinutesRequest request) throws ValidationException {
        MINIMUM_TIME_VALIDATOR.validate(request);
        Settings settings = loadOrCreate();
        int before = settings.getMinimumMinutes();
        settings.setMinimumMinutes(request.getMinutes());
        saveSettings(settings, actor);
        audit(actor, "settings.set_minimum_time", "minimum_minutes", before, settings.getMinimumMinutes());
        return toResponse(settings);
    }

    @Transactional(rollbackFor = Exception.class)
    public SettingsResponse updatePackages(AuthenticatedUser actor, UpdatePackagesRequest request) throws ValidationException {
        PACKAGES_VALIDATOR.validate(request);
        LocalDateTime now = TimeUtil.nowUtc();
        List<Package> current = packageRepository.findByActiveTrueOrderByMinutesAsc();
        List<Map<String, Object>> before = describePackages(current);
        for (Package old : current) {
            old.setActive(false);
            old.setUpdatedAt(now);
        }
        packageRepository.saveAllAndFlush(current);
        List<Package> created = new ArrayList<>();
        for (PackageItemRequest item : request.getPackages()) {
            Package pkg = new Package();
            pkg.setAmount(item.getAmount());
            pkg.setMinutes(item.getMinutes());
            pkg.setIsDefault(item.getIsDefault());
            pkg.setActive(true);
            pkg.setCreatedAt(now);
            pkg.setUpdatedAt(now);
            created.add(pkg);
        }
        List<Package> saved = packageRepository.saveAllAndFlush(created);
        saveSettings(loadOrCreate(), actor);
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("before", before);
        payload.put("after", describePackages(saved));
        auditLogService.log(actor, "settings.update_packages", ENTITY_TYPE, SETTINGS_ID.longValue(), payload);
        return toResponse(loadOrCreate());
    }

    private Settings loadOrCreate() {
        return settingsRepository.findById(SETTINGS_ID).orElseGet(this::createDefaultSettings);
    }

    private Settings createDefaultSettings() {
        LocalDateTime now = TimeUtil.nowUtc();
        Settings settings = new Settings();
        settings.setId(SETTINGS_ID);
        settings.setRefundEnabled(false);
        settings.setExtendEnabled(true);
        settings.setOverdueFeePerMinute(0);
        settings.setOverdueGraceMinutes(0);
        settings.setMinimumMinutes(0);
        settings.setCreatedAt(now);
        settings.setUpdatedAt(now);
        return settingsRepository.save(settings);
    }

    private void saveSettings(Settings settings, AuthenticatedUser actor) {
        settings.setUpdatedBy(userRepository.getReferenceById(actor.id()));
        settings.setUpdatedAt(TimeUtil.nowUtc());
        settingsRepository.save(settings);
    }

    private void audit(AuthenticatedUser actor, String action, String field, Object before, Object after) {
        Map<String, Object> beforeMap = new LinkedHashMap<>();
        beforeMap.put(field, before);
        Map<String, Object> afterMap = new LinkedHashMap<>();
        afterMap.put(field, after);
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("before", beforeMap);
        payload.put("after", afterMap);
        auditLogService.log(actor, action, ENTITY_TYPE, SETTINGS_ID.longValue(), payload);
    }

    private List<Map<String, Object>> describePackages(List<Package> packages) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Package pkg : packages) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("amount", pkg.getAmount());
            row.put("minutes", pkg.getMinutes());
            row.put("is_default", pkg.getIsDefault());
            result.add(row);
        }
        return result;
    }

    private SettingsResponse toResponse(Settings settings) {
        List<PackageResponse> packages = new ArrayList<>();
        for (Package pkg : packageRepository.findByActiveTrueOrderByMinutesAsc()) {
            packages.add(new PackageResponse(pkg.getId(), pkg.getAmount(), pkg.getMinutes(), pkg.getIsDefault()));
        }
        return new SettingsResponse(
                settings.getRefundEnabled(),
                settings.getExtendEnabled(),
                settings.getOverdueFeePerMinute(),
                settings.getOverdueGraceMinutes(),
                settings.getMinimumMinutes(),
                packages,
                TimeUtil.toIsoUtc(settings.getUpdatedAt()));
    }
}
