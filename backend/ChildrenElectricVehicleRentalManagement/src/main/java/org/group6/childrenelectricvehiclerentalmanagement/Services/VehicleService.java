package org.group6.childrenelectricvehiclerentalmanagement.Services;

import lombok.RequiredArgsConstructor;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.VehicleController.UpdateVehicleSlugRequest;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.VehicleController.UpdateVehicleStatusRequest;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.VehicleController.createVehicleRequest;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.SessionStatus;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.VehicleStatus;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.AbstractException;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.UnauthorizedException;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.UserException;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.ValidationException;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.VehicleInUse;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.VehicleNotExits;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.VehicleSlugAlreadyExits;
import org.group6.childrenelectricvehiclerentalmanagement.Models.LinkingObject.SessionVehicle;
import org.group6.childrenelectricvehiclerentalmanagement.Models.User;
import org.group6.childrenelectricvehiclerentalmanagement.Models.Vehicle;
import org.group6.childrenelectricvehiclerentalmanagement.Repositories.SessionVehicleRepository;
import org.group6.childrenelectricvehiclerentalmanagement.Repositories.VehicleRepository;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.Validators.IdValidator;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.Validators.InputValidator;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class VehicleService {
    private static final int MAX_SLUG_LENGTH = 50;
    /** API đổi trạng thái chỉ nhận các giá trị này. Inused do luồng vé đặt, Deleted do API xóa đặt. */
    private static final Set<VehicleStatus> SETTABLE =
            EnumSet.of(VehicleStatus.Online, VehicleStatus.Repairing, VehicleStatus.Offline);

    private final VehicleRepository vehicleRepository;
    private final SessionVehicleRepository sessionVehicleRepository;
    private final AuditLogService auditLogService;

    // ======================================================================
    // Tạo
    // ======================================================================

    /** POST /vehicle: xe mới mặc định Offline. */
    @Transactional(rollbackFor = Exception.class)
    public Vehicle createNewVehicle(createVehicleRequest request, User actor) throws Exception {
        requireActor(actor);
        String slug = normalizeSlug(request == null ? null : request.vehicle_slug());

        if (vehicleRepository.findBySlugAndDeletedAtIsNull(slug).isPresent())
            throw new VehicleSlugAlreadyExits();

        Vehicle vehicle = new Vehicle(slug);
        vehicle.setStatus(VehicleStatus.Offline);
        try {
            vehicle = vehicleRepository.saveAndFlush(vehicle);
        } catch (DataIntegrityViolationException e) { // partial unique index: slug chưa xóa phải duy nhất
            throw new VehicleSlugAlreadyExits();
        }

        audit(actor, "vehicle.create", vehicle, null, snapshot(vehicle));
        return vehicle;
    }

    // ======================================================================
    // Đọc
    // ======================================================================

    /**
     * GET /vehicle?status=&session_id=
     * Mặc định không gồm xe Deleted (trừ khi lọc status=Deleted).
     * session_id: chỉ lấy xe đang thuộc phiên đó.
     */
    @Transactional(readOnly = true)
    public List<Vehicle> getAllVehicle(List<String> statusFilter, String rawSessionId) throws Exception {
        Set<VehicleStatus> statuses = parseStatusFilter(statusFilter);

        Set<Long> idsInSession = null;
        if (rawSessionId != null && !rawSessionId.isBlank()) {
            Long sessionId = IdValidator.validate("session_id", rawSessionId);
            idsInSession = new HashSet<>();
            for (SessionVehicle sv : sessionVehicleRepository.findBySessionIdAndRemovedAtIsNull(sessionId)) {
                idsInSession.add(sv.getVehicle().getId());
            }
        }

        boolean wantDeleted = statuses.contains(VehicleStatus.Deleted);
        List<Vehicle> result = new ArrayList<>();
        for (Vehicle v : vehicleRepository.findAll()) {
            boolean deleted = v.getDeletedAt() != null || v.getStatus() == VehicleStatus.Deleted;
            if (deleted && !wantDeleted) continue;
            if (!statuses.isEmpty() && !statuses.contains(v.getStatus())) continue;
            if (idsInSession != null && !idsInSession.contains(v.getId())) continue;
            result.add(v);
        }
        result.sort(Comparator.comparing(Vehicle::getId));
        return result;
    }

    @Transactional(readOnly = true)
    public Vehicle getVehicle(long id) throws Exception {
        return vehicleRepository.findById(id).orElseThrow(VehicleNotExits::new);
    }

    // ======================================================================
    // Đổi tên nhận diện (chỉ khi xe Offline)
    // ======================================================================

    @Transactional(rollbackFor = Exception.class)
    public Vehicle updateVehicleSlug(long id, UpdateVehicleSlugRequest request, User actor) throws Exception {
        requireActor(actor);
        String slug = normalizeSlug(request == null ? null : request.vehicle_slug());

        Vehicle vehicle = lockVehicle(id);
        if (vehicle.getStatus() == VehicleStatus.Inused) throw new VehicleInUse(vehicle.getSlug());
        if (vehicle.getStatus() != VehicleStatus.Offline)
            throw new AbstractException("vehicle must be offline to be renamed", "VEHICLE_NOT_OFFLINE",
                    "Chỉ được đổi tên khi xe đang ở trạng thái Offline (hiện tại: " + vehicle.getStatus() + ")", 409);

        if (slug.equals(vehicle.getSlug())) return vehicle; // không đổi gì

        if (vehicleRepository.findBySlugAndDeletedAtIsNull(slug).isPresent())
            throw new VehicleSlugAlreadyExits();

        Map<String, Object> before = snapshot(vehicle);
        vehicle.setSlug(slug);
        try {
            vehicle = vehicleRepository.saveAndFlush(vehicle);
        } catch (DataIntegrityViolationException e) {
            throw new VehicleSlugAlreadyExits();
        }

        audit(actor, "vehicle.update_slug", vehicle, before, snapshot(vehicle));
        return vehicle;
    }

    // ======================================================================
    // Đổi trạng thái (Online, Repairing, Offline)
    // ======================================================================

    /**
     * Xe Inused không được đổi. Xe đang gắn vé Overdue cũng ở trạng thái Inused (chưa trả xe)
     * nên được chặn bởi cùng điều kiện này.
     */
    @Transactional(rollbackFor = Exception.class)
    public Vehicle updateVehicleStatus(long id, UpdateVehicleStatusRequest request, User actor) throws Exception {
        requireActor(actor);
        VehicleStatus target = parseSettableStatus(request == null ? null : request.vehicle_status());

        Vehicle vehicle = lockVehicle(id);
        if (vehicle.getStatus() == VehicleStatus.Inused) throw new VehicleInUse(vehicle.getSlug());
        if (vehicle.getStatus() == target)
            throw new AbstractException("vehicle is in the same status", "SAME_STATUS",
                    "Xe đang ở trạng thái này", 409);

        Map<String, Object> before = snapshot(vehicle);
        vehicle.setStatus(target);
        vehicle = vehicleRepository.save(vehicle);

        audit(actor, "vehicle.status_change", vehicle, before, snapshot(vehicle));
        return vehicle;
    }

    // ======================================================================
    // Xóa mềm
    // ======================================================================

    @Transactional(rollbackFor = Exception.class)
    public void deleteVehicle(long id, User actor) throws Exception {
        requireActor(actor);
        Vehicle vehicle = lockVehicle(id);

        switch (vehicle.getStatus()) {
            case Inused -> throw new VehicleInUse(vehicle.getSlug());
            case Online -> throw new AbstractException("vehicle is online", "VEHICLE_ONLINE",
                    "Hãy chuyển xe sang Offline trước khi xóa", 409);
            default -> { }
        }


        if (sessionVehicleRepository.existsByVehicle_IdAndRemovedAtIsNullAndSession_Status(vehicle.getId(), SessionStatus.InProgress))
            throw new AbstractException("vehicle belongs to the in-progress session", "VEHICLE_IN_ACTIVE_SESSION",
                    "Xe đang nằm trong phiên đang làm, hãy bớt xe khỏi phiên trước", 409);

        Map<String, Object> before = snapshot(vehicle);
        vehicle.setStatus(VehicleStatus.Deleted); // Vehicle.setStatus tự ghi deleted_at
        vehicle = vehicleRepository.save(vehicle);

        audit(actor, "vehicle.delete", vehicle, before, snapshot(vehicle));
    }

    // ======================================================================
    // Trợ giúp
    // ======================================================================

    private static void requireActor(User actor) throws UnauthorizedException {
        if (actor == null) throw new UnauthorizedException();
    }

    /** Lấy xe và khóa ghi (cùng cách với luồng gán vé) để tránh đổi trạng thái chen ngang. */
    private Vehicle lockVehicle(long id) throws Exception {
        Vehicle found = vehicleRepository.findById(id).orElseThrow(VehicleNotExits::new);
        if (found.getDeletedAt() != null || found.getStatus() == VehicleStatus.Deleted)
            throw new UserException("vehicle already deleted", "Xe đã bị xóa");
        return vehicleRepository.lockBySlug(found.getSlug()).orElseThrow(VehicleNotExits::new);
    }

    private static String normalizeSlug(String raw) throws Exception {
        if (raw == null || raw.isBlank()) throw new ValidationException("vehicle_slug", "Tên xe không được để trống");
        String slug = raw.trim();
        if (slug.length() > MAX_SLUG_LENGTH)
            throw new ValidationException("vehicle_slug", "Tên xe tối đa " + MAX_SLUG_LENGTH + " ký tự");
        InputValidator.isVehicleNameValid(slug);
        return slug; // luôn lưu bản đã trim
    }

    private static VehicleStatus parseSettableStatus(String raw) throws ValidationException {
        if (raw == null || raw.isBlank())
            throw new ValidationException("vehicle_status", "Trạng thái xe không được để trống");
        VehicleStatus status;
        try {
            status = VehicleStatus.valueOf(raw.trim());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("vehicle_status", "Trạng thái xe không hợp lệ");
        }
        if (!SETTABLE.contains(status))
            throw new ValidationException("vehicle_status", "Chỉ được đặt trạng thái Online, Repairing hoặc Offline");
        return status;
    }

    private static Set<VehicleStatus> parseStatusFilter(List<String> raw) throws ValidationException {
        Set<VehicleStatus> result = EnumSet.noneOf(VehicleStatus.class);
        if (raw == null) return result;
        for (String s : raw) {
            if (s == null || s.isBlank()) continue;
            try {
                result.add(VehicleStatus.valueOf(s.trim()));
            } catch (IllegalArgumentException e) {
                throw new ValidationException("status", "status chứa giá trị không hợp lệ: " + s);
            }
        }
        return result;
    }

    private Map<String, Object> snapshot(Vehicle v) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("slug", v.getSlug());
        m.put("status", v.getStatus());
        m.put("deleted_at", v.getDeletedAt());
        return m;
    }

    private void audit(User actor, String action, Vehicle vehicle, Map<String, Object> before, Map<String, Object> after) {
        auditLogService.log(actor, action, "vehicle", vehicle.getId(), before, after);
    }
}
