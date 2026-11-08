package org.group6.childrenelectricvehiclerentalmanagement.Services;

import org.group6.childrenelectricvehiclerentalmanagement.DTOs.SettingsController.AmountPerMinuteRequest;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.SettingsController.MinutesRequest;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.SettingsController.PackageItemRequest;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.SettingsController.PackageResponse;
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
import org.group6.childrenelectricvehiclerentalmanagement.Utility.Long.AmountPerMinuteRequestValidator;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.Long.MinutesRequestValidator;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.Long.ToggleRequestValidator;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.Long.UpdatePackagesRequestValidator;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.TimeUtil;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.Validators.CancelWaitPolicy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class SettingsService {
    private static final Short SETTINGS_ID = 1;

    private static final ToggleRequestValidator TOGGLE_VALIDATOR = new ToggleRequestValidator();
    private static final UpdatePackagesRequestValidator PACKAGES_VALIDATOR = new UpdatePackagesRequestValidator();
    private static final AmountPerMinuteRequestValidator OVERDUE_FEE_VALIDATOR = new AmountPerMinuteRequestValidator();
    private static final MinutesRequestValidator OVERDUE_LIMIT_VALIDATOR = new MinutesRequestValidator("Thời gian ân hạn", 0, MinutesRequestValidator.MAX_MINUTES);
    private static final MinutesRequestValidator MINIMUM_TIME_VALIDATOR = new MinutesRequestValidator("Thời gian thuê tối thiểu", 0, MinutesRequestValidator.MAX_MINUTES);
    private static final MinutesRequestValidator CANCEL_WAIT_VALIDATOR = new MinutesRequestValidator("Thời gian chờ trước khi hủy vé", CancelWaitPolicy.MIN_CANCEL_WAIT_MINUTES, MinutesRequestValidator.MAX_MINUTES);

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
    public Settings loadSettings() {
        return loadOrCreate();
    }

    @Transactional(rollbackFor = Exception.class)
    public SettingsResponse getSettings() {
        return toResponse(loadOrCreate());
    }

    @Transactional(rollbackFor = Exception.class)
    public SettingsResponse toggleRefund(User actor, ToggleRequest request) throws ValidationException {
        TOGGLE_VALIDATOR.validate(request);
        Settings settings = loadOrCreate();
        boolean before = settings.getRefundEnabled();
        settings.setRefundEnabled(request.getEnabled());
        saveSettings(settings, actor);
        audit(actor, "settings.toggle_refund", "refund_enabled", before, settings.getRefundEnabled());
        return toResponse(settings);
    }

    @Transactional(rollbackFor = Exception.class)
    public SettingsResponse toggleExtend(User actor, ToggleRequest request) throws ValidationException {
        TOGGLE_VALIDATOR.validate(request);
        Settings settings = loadOrCreate();
        boolean before = settings.getExtendEnabled();
        settings.setExtendEnabled(request.getEnabled());
        saveSettings(settings, actor);
        audit(actor, "settings.toggle_extend", "extend_enabled", before, settings.getExtendEnabled());
        return toResponse(settings);
    }

    @Transactional(rollbackFor = Exception.class)
    public SettingsResponse setOverdueFee(User actor, AmountPerMinuteRequest request) throws ValidationException {
        OVERDUE_FEE_VALIDATOR.validate(request);
        Settings settings = loadOrCreate();
        int before = settings.getOverdueFeePerMinute();
        settings.setOverdueFeePerMinute(request.getAmountPerMinute());
        saveSettings(settings, actor);
        audit(actor, "settings.set_overdue_fee", "overdue_fee_per_minute", before, settings.getOverdueFeePerMinute());
        return toResponse(settings);
    }

    @Transactional(rollbackFor = Exception.class)
    public SettingsResponse setOverdueLimit(User actor, MinutesRequest request) throws ValidationException {
        OVERDUE_LIMIT_VALIDATOR.validate(request);
        Settings settings = loadOrCreate();
        int before = settings.getOverdueGraceMinutes();
        settings.setOverdueGraceMinutes(request.getMinutes());
        saveSettings(settings, actor);
        audit(actor, "settings.set_overdue_limit", "overdue_grace_minutes", before, settings.getOverdueGraceMinutes());
        return toResponse(settings);
    }

    @Transactional(rollbackFor = Exception.class)
    public SettingsResponse setMinimumTime(User actor, MinutesRequest request) throws ValidationException {
        MINIMUM_TIME_VALIDATOR.validate(request);
        Settings settings = loadOrCreate();
        int before = settings.getMinimumMinutes();
        settings.setMinimumMinutes(request.getMinutes());
        saveSettings(settings, actor);
        audit(actor, "settings.set_minimum_time", "minimum_minutes", before, settings.getMinimumMinutes());
        return toResponse(settings);
    }

    @Transactional(rollbackFor = Exception.class)
    public SettingsResponse setCancelWait(User actor, MinutesRequest request) throws ValidationException {
        CANCEL_WAIT_VALIDATOR.validate(request);
        Optional<Package> lowest = packageRepository.findFirstByActiveTrueOrderByMinutesAsc();
        if (lowest.isPresent() && request.getMinutes() >= lowest.get().getMinutes()) {
            throw new ValidationException("minutes",
                    "Thời gian chờ trước khi hủy vé phải nhỏ hơn gói thấp nhất hiện tại (" + lowest.get().getMinutes() + " phút)");
        }
        Settings settings = loadOrCreate();
        int before = settings.getCancelWaitMinutes();
        settings.setCancelWaitMinutes(request.getMinutes());
        saveSettings(settings, actor);
        audit(actor, "settings.set_cancel_wait", "cancel_wait_minutes", before, settings.getCancelWaitMinutes());
        return toResponse(settings);
    }

    @Transactional(rollbackFor = Exception.class)
    public SettingsResponse updatePackages(User actor, UpdatePackagesRequest request) throws ValidationException {
        PACKAGES_VALIDATOR.validate(request);
        Settings settings = loadOrCreate();
        int cancelWait = settings.getCancelWaitMinutes();

        List<PackageItemRequest> items = request.getPackages();
        Set<Integer> requestedMinutes = new HashSet<>();
        for (int i = 0; i < items.size(); i++) {
            int minutes = items.get(i).getMinutes();
            if (minutes <= cancelWait) {
                throw new ValidationException("packages[" + i + "].minutes",
                        "Số phút của gói thứ " + (i + 1) + " phải lớn hơn thời gian chờ hủy vé (" + cancelWait + " phút)");
            }
            requestedMinutes.add(minutes);
        }

        List<Package> existing = packageRepository.findAllByOrderByMinutesAsc();
        List<Map<String, Object>> before = describePackages(existing);

        Map<Integer, Package> kept = new HashMap<>();
        List<Package> removed = new ArrayList<>();
        for (Package current : existing) {
            if (requestedMinutes.contains(current.getMinutes()) && !kept.containsKey(current.getMinutes())) {
                kept.put(current.getMinutes(), current);
            } else {
                removed.add(current);
            }
        }
        packageRepository.deleteAll(removed);
        packageRepository.flush();

        List<Package> result = new ArrayList<>();
        for (PackageItemRequest item : items) {
            Package pricePackage = kept.getOrDefault(item.getMinutes(), new Package());
            pricePackage.setAmount(item.getAmount());
            pricePackage.setMinutes(item.getMinutes());
            pricePackage.setIsDefault(item.getIsDefault());
            pricePackage.setActive(true);
            result.add(pricePackage);
        }
        List<Package> saved = packageRepository.saveAllAndFlush(result);

        saveSettings(settings, actor);
        audit(actor, "settings.update_packages", "packages", before, describePackages(saved));
        return toResponse(settings);
    }

    private Settings loadOrCreate() {
        return settingsRepository.findById(SETTINGS_ID).orElseGet(this::createDefaultSettings);
    }

    private Settings createDefaultSettings() {
        LocalDateTime now = TimeUtil.now();
        Settings settings = new Settings();
        settings.setId(SETTINGS_ID);
        settings.setRefundEnabled(false);
        settings.setExtendEnabled(true);
        settings.setOverdueFeePerMinute(0);
        settings.setOverdueGraceMinutes(0);
        settings.setMinimumMinutes(0);
        settings.setCancelWaitMinutes(CancelWaitPolicy.MIN_CANCEL_WAIT_MINUTES);
        settings.setCreatedAt(now);
        settings.setUpdatedAt(now);
        return settingsRepository.save(settings);
    }

    private void saveSettings(Settings settings, User actor) {
        settings.setUpdatedBy(userRepository.getReferenceById(actor.getId()));
        settings.setUpdatedAt(TimeUtil.now());
        settingsRepository.save(settings);
    }

    private void audit(User actor, String action, String field, Object before, Object after) {
        Map<String, Object> beforeMap = new LinkedHashMap<>();
        beforeMap.put(field, before);
        Map<String, Object> afterMap = new LinkedHashMap<>();
        afterMap.put(field, after);
        auditLogService.log(actor, action, "settings", SETTINGS_ID.longValue(), beforeMap, afterMap);
    }

    private List<Map<String, Object>> describePackages(List<Package> packages) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Package pricePackage : packages) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("amount", pricePackage.getAmount());
            row.put("minutes", pricePackage.getMinutes());
            row.put("is_default", pricePackage.getIsDefault());
            row.put("active", pricePackage.getActive());
            result.add(row);
        }
        return result;
    }

    private SettingsResponse toResponse(Settings settings) {
        List<PackageResponse> packages = new ArrayList<>();
        for (Package pricePackage : packageRepository.findByActiveTrueOrderByMinutesAsc()) {
            packages.add(PackageResponse.from(pricePackage));
        }
        return new SettingsResponse(
                settings.getRefundEnabled(),
                settings.getExtendEnabled(),
                settings.getOverdueFeePerMinute(),
                settings.getOverdueGraceMinutes(),
                settings.getMinimumMinutes(),
                settings.getCancelWaitMinutes(),
                packages,
                TimeUtil.iso(settings.getUpdatedAt()));
    }
}
