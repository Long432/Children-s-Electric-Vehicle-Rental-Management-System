package org.group6.childrenelectricvehiclerentalmanagement.Services;

import lombok.RequiredArgsConstructor;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.PackageController.CreatePackageRequest;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.PackageController.DeletePackageResponse;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.PackageController.PackageSale;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.PackageController.PackageStatsItem;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.PackageController.PackageStatsResponse;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.PackageController.UpdatePackageRequest;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.SettingsController.PackageResponse;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.SettingsController.ToggleRequest;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.PaymentType;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.StatsGroup;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.TicketStatus;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.PackageNotFound;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.PackageRuleViolation;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.ValidationException;
import org.group6.childrenelectricvehiclerentalmanagement.Models.Package;
import org.group6.childrenelectricvehiclerentalmanagement.Models.User;
import org.group6.childrenelectricvehiclerentalmanagement.Repositories.PackageRepository;
import org.group6.childrenelectricvehiclerentalmanagement.Repositories.PaymentRepository;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.Long.CreatePackageRequestValidator;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.Long.UpdatePackageRequestValidator;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.Long.UpdatePackagesRequestValidator;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.TimeUtil;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.Validators.IdValidator;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.Validators.PackageStatsValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PackageService {
    private static final CreatePackageRequestValidator CREATE_VALIDATOR = new CreatePackageRequestValidator();
    private static final UpdatePackageRequestValidator UPDATE_VALIDATOR = new UpdatePackageRequestValidator();

    private final PackageRepository packageRepository;
    private final PaymentRepository paymentRepository;
    private final SettingsService settingsService;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public List<PackageResponse> listPackages() {
        return packageRepository.findAllByOrderByMinutesAsc().stream().map(PackageResponse::from).toList();
    }

    @Transactional(rollbackFor = Exception.class)
    public PackageResponse createPackage(User actor, CreatePackageRequest request) throws ValidationException {
        CREATE_VALIDATOR.validate(request);
        int minutes = request.getMinutes();
        int cancelWait = settingsService.loadSettings().getCancelWaitMinutes();
        requireAboveCancelWait("minutes", minutes, cancelWait);
        if (packageRepository.count() >= UpdatePackagesRequestValidator.MAX_PACKAGES) {
            throw new ValidationException("packages", "Tối đa " + UpdatePackagesRequestValidator.MAX_PACKAGES + " gói giá");
        }
        if (packageRepository.existsByMinutes(minutes)) {
            throw new ValidationException("minutes", "Đã có gói " + minutes + " phút");
        }

        List<Package> defaults = packageRepository.findByIsDefaultTrueAndActiveTrue();
        if (request.getIsDefault()) {
            demote(defaults);
        } else if (defaults.isEmpty()) {
            throw new ValidationException("is_default", "Chưa có gói mặc định, gói này phải là gói mặc định");
        }

        Package pricePackage = new Package();
        pricePackage.setAmount(request.getAmount());
        pricePackage.setMinutes(minutes);
        pricePackage.setIsDefault(request.getIsDefault());
        pricePackage.setActive(true);
        packageRepository.saveAndFlush(pricePackage);

        auditLogService.log(actor, "package.create", "package", pricePackage.getId(), null, describe(pricePackage));
        return PackageResponse.from(pricePackage);
    }

    @Transactional(rollbackFor = Exception.class)
    public PackageResponse updatePackage(User actor, String rawId, UpdatePackageRequest request) throws Exception {
        Long id = IdValidator.validate("id", rawId);
        UPDATE_VALIDATOR.validate(request);
        Package pricePackage = packageRepository.lockById(id).orElseThrow(PackageNotFound::new);
        Map<String, Object> before = describe(pricePackage);
        boolean active = Boolean.TRUE.equals(pricePackage.getActive());

        Integer minutes = request.getMinutes();
        if (minutes != null && !minutes.equals(pricePackage.getMinutes())) {
            if (packageRepository.existsByMinutesAndIdNot(minutes, id)) {
                throw new ValidationException("minutes", "Đã có gói " + minutes + " phút");
            }
            if (active) {
                requireAboveCancelWait("minutes", minutes, settingsService.loadSettings().getCancelWaitMinutes());
            }
            pricePackage.setMinutes(minutes);
        }
        if (request.getAmount() != null) {
            pricePackage.setAmount(request.getAmount());
        }

        Boolean isDefault = request.getIsDefault();
        boolean currentlyDefault = Boolean.TRUE.equals(pricePackage.getIsDefault());
        if (Boolean.TRUE.equals(isDefault) && !currentlyDefault) {
            if (!active) {
                throw new PackageRuleViolation("Gói đang đóng không thể làm gói mặc định");
            }
            demote(packageRepository.findByIsDefaultTrueAndActiveTrue());
            pricePackage.setIsDefault(true);
        } else if (Boolean.FALSE.equals(isDefault) && currentlyDefault) {
            throw new PackageRuleViolation("Không thể bỏ gói mặc định, hãy đặt gói khác làm mặc định");
        }

        packageRepository.saveAndFlush(pricePackage);
        auditLogService.log(actor, "package.update", "package", id, before, describe(pricePackage));
        return PackageResponse.from(pricePackage);
    }

    @Transactional(rollbackFor = Exception.class)
    public PackageResponse togglePackage(User actor, String rawId, ToggleRequest request) throws Exception {
        Long id = IdValidator.validate("id", rawId);
        Package pricePackage = packageRepository.lockById(id).orElseThrow(PackageNotFound::new);
        boolean current = Boolean.TRUE.equals(pricePackage.getActive());
        boolean target = request != null && request.getEnabled() != null ? request.getEnabled() : !current;
        if (target == current) {
            return PackageResponse.from(pricePackage);
        }

        if (!target && Boolean.TRUE.equals(pricePackage.getIsDefault())) {
            throw new PackageRuleViolation("Không thể đóng gói mặc định, hãy đặt gói khác làm mặc định trước");
        }
        if (target) {
            int cancelWait = settingsService.loadSettings().getCancelWaitMinutes();
            if (pricePackage.getMinutes() <= cancelWait) {
                throw new PackageRuleViolation("Gói " + pricePackage.getMinutes()
                        + " phút không lớn hơn thời gian chờ hủy vé (" + cancelWait + " phút), không thể mở");
            }
        }

        Map<String, Object> before = describe(pricePackage);
        pricePackage.setActive(target);
        packageRepository.saveAndFlush(pricePackage);
        auditLogService.log(actor, target ? "package.open" : "package.close", "package", id, before, describe(pricePackage));
        return PackageResponse.from(pricePackage);
    }

    @Transactional(rollbackFor = Exception.class)
    public DeletePackageResponse deletePackage(User actor, String rawId) throws Exception {
        Long id = IdValidator.validate("id", rawId);
        Package pricePackage = packageRepository.lockById(id).orElseThrow(PackageNotFound::new);
        if (Boolean.TRUE.equals(pricePackage.getIsDefault())) {
            throw new PackageRuleViolation("Không thể xóa gói mặc định, hãy đặt gói khác làm mặc định trước");
        }
        Map<String, Object> before = describe(pricePackage);
        packageRepository.delete(pricePackage);
        packageRepository.flush();
        auditLogService.log(actor, "package.delete", "package", id, before, null);
        return new DeletePackageResponse(id, true);
    }

    @Transactional(readOnly = true)
    public PackageStatsResponse getStats(String rawId, String rawGroupBy, String rawFrom, String rawTo) throws Exception {
        Long id = IdValidator.validate("id", rawId);
        Package pricePackage = packageRepository.findById(id).orElseThrow(PackageNotFound::new);
        PackageStatsValidator.Query query = PackageStatsValidator.validate(rawGroupBy, rawFrom, rawTo, TimeUtil.now().toLocalDate());

        LocalDateTime rangeStart = query.from().atStartOfDay();
        LocalDateTime rangeEnd = query.to().plusDays(1).atStartOfDay();
        List<PackageSale> sales = paymentRepository.findPackageSales(
                id, PaymentType.Ticket, TicketStatus.Canceled, rangeStart, rangeEnd);

        boolean byDay = query.group() == StatsGroup.Day;
        Map<String, long[]> buckets = new LinkedHashMap<>();
        if (byDay) {
            for (LocalDate day = query.from(); !day.isAfter(query.to()); day = day.plusDays(1)) {
                buckets.put(day.toString(), new long[2]);
            }
        } else {
            YearMonth last = YearMonth.from(query.to());
            for (YearMonth month = YearMonth.from(query.from()); !month.isAfter(last); month = month.plusMonths(1)) {
                buckets.put(month.toString(), new long[2]);
            }
        }
        for (PackageSale sale : sales) {
            String key = byDay ? sale.soldAt().toLocalDate().toString() : YearMonth.from(sale.soldAt()).toString();
            long[] bucket = buckets.computeIfAbsent(key, k -> new long[2]);
            bucket[0]++;
            bucket[1] += sale.amount();
        }

        List<PackageStatsItem> items = new ArrayList<>();
        long totalTickets = 0;
        long totalRevenue = 0;
        for (Map.Entry<String, long[]> entry : buckets.entrySet()) {
            items.add(new PackageStatsItem(entry.getKey(), entry.getValue()[0], entry.getValue()[1]));
            totalTickets += entry.getValue()[0];
            totalRevenue += entry.getValue()[1];
        }
        return new PackageStatsResponse(
                PackageResponse.from(pricePackage),
                query.group().name().toLowerCase(Locale.ROOT),
                query.from().toString(),
                query.to().toString(),
                totalTickets,
                totalRevenue,
                items);
    }

    private void demote(List<Package> packages) {
        for (Package item : packages) {
            item.setIsDefault(false);
        }
        packageRepository.saveAllAndFlush(packages);
    }

    private void requireAboveCancelWait(String field, int minutes, int cancelWait) throws ValidationException {
        if (minutes <= cancelWait) {
            throw new ValidationException(field,
                    "Số phút của gói phải lớn hơn thời gian chờ hủy vé (" + cancelWait + " phút)");
        }
    }

    private Map<String, Object> describe(Package pricePackage) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("amount", pricePackage.getAmount());
        row.put("minutes", pricePackage.getMinutes());
        row.put("is_default", pricePackage.getIsDefault());
        row.put("active", pricePackage.getActive());
        return row;
    }
}
