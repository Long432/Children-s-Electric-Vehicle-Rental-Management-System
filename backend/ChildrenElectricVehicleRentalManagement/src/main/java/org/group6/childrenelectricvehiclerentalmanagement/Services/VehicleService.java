package org.group6.childrenelectricvehiclerentalmanagement.Services;

import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Vehicle.CreateVehicleRequest;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Vehicle.UpdateVehicleSlugRequest;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Vehicle.UpdateVehicleStatusRequest;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Vehicle.VehicleResponse;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.SessionStatus;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.VehicleStatus;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.AbstractException;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.ConflictException;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.ValidationException;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.VehicleNotExits;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.VehicleSlugAlreadyExits;
import org.group6.childrenelectricvehiclerentalmanagement.Models.Vehicle;
import org.group6.childrenelectricvehiclerentalmanagement.Repositories.SessionVehicleRepository;
import org.group6.childrenelectricvehiclerentalmanagement.Repositories.VehicleRepository;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.Validators.CreateVehicleRequestValidator;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.Validators.UpdateVehicleSlugRequestValidator;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.Validators.UpdateVehicleStatusRequestValidator;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

@Service
public class VehicleService {

    private static final String SLUG_INDEX = "uq_vehicles_slug_active";
    private static final String UNIQUE_VIOLATION = "23505";
    private static final int MAX_CAUSE_DEPTH = 16;

    private static final CreateVehicleRequestValidator CREATE_VALIDATOR = new CreateVehicleRequestValidator();
    private static final UpdateVehicleSlugRequestValidator SLUG_VALIDATOR = new UpdateVehicleSlugRequestValidator();
    private static final UpdateVehicleStatusRequestValidator STATUS_VALIDATOR = new UpdateVehicleStatusRequestValidator();

    private final VehicleRepository vehicleRepository;
    private final SessionVehicleRepository sessionVehicleRepository;

    public VehicleService(VehicleRepository vehicleRepository, SessionVehicleRepository sessionVehicleRepository) {
        this.vehicleRepository = vehicleRepository;
        this.sessionVehicleRepository = sessionVehicleRepository;
    }

    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public List<VehicleResponse> list(String status, Long sessionId) throws ValidationException {
        if (sessionId != null && sessionId < 1) {
            throw new ValidationException("session_id", "Mã phiên không hợp lệ");
        }
        boolean hasStatus = status != null && !status.isBlank();
        List<Vehicle> vehicles;
        if (sessionId == null) {
            vehicles = hasStatus
                    ? vehicleRepository.findByStatusOrderByIdAsc(parseStatus(status))
                    : vehicleRepository.findByStatusNotOrderByIdAsc(VehicleStatus.Deleted);
        } else {
            vehicles = hasStatus
                    ? vehicleRepository.findByStatusAndSessionId(parseStatus(status).name(), sessionId)
                    : vehicleRepository.findActiveBySessionId(sessionId);
        }
        return vehicles.stream().map(VehicleResponse::from).toList();
    }

    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public VehicleResponse get(long id) throws AbstractException {
        return VehicleResponse.from(findActive(id));
    }

    @Transactional(rollbackFor = Exception.class)
    public VehicleResponse create(CreateVehicleRequest request) throws AbstractException {
        CREATE_VALIDATOR.validate(request);
        String slug = request.slug().trim();
        if (vehicleRepository.existsBySlugAndDeletedAtIsNull(slug)) {
            throw new VehicleSlugAlreadyExits();
        }
        return VehicleResponse.from(saveChecked(new Vehicle(slug)));
    }

    @Transactional(rollbackFor = Exception.class)
    public VehicleResponse rename(long id, UpdateVehicleSlugRequest request) throws AbstractException {
        SLUG_VALIDATOR.validate(request);
        Vehicle vehicle = findActiveForUpdate(id);
        if (vehicle.getStatus() == VehicleStatus.Inused) {
            throw new ConflictException("VEHICLE_IN_USE", "vehicle in use", "Không thể đổi tên khi xe đang được sử dụng");
        }
        String slug = request.slug().trim();
        if (!slug.equals(vehicle.getSlug())) {
            if (vehicleRepository.existsBySlugAndDeletedAtIsNull(slug)) {
                throw new VehicleSlugAlreadyExits();
            }
            vehicle.setSlug(slug);
            saveChecked(vehicle);
        }
        return VehicleResponse.from(vehicle);
    }

    @Transactional(rollbackFor = Exception.class)
    public VehicleResponse changeStatus(long id, UpdateVehicleStatusRequest request) throws AbstractException {
        STATUS_VALIDATOR.validate(request);
        VehicleStatus target = VehicleStatus.valueOf(request.status().trim());
        Vehicle vehicle = findActiveForUpdate(id);
        if (vehicle.getStatus() == VehicleStatus.Inused) {
            throw new ConflictException("VEHICLE_IN_USE", "vehicle in use", "Xe đang được sử dụng, không thể đổi trạng thái");
        }
        if (vehicle.getStatus() == target) {
            throw new ConflictException("VEHICLE_STATUS_UNCHANGED", "same status", "Xe đang ở trạng thái này");
        }
        vehicle.setStatus(target);
        return VehicleResponse.from(saveChecked(vehicle));
    }

    @Transactional(rollbackFor = Exception.class)
    public VehicleResponse delete(long id) throws AbstractException {
        Vehicle vehicle = findActiveForUpdate(id);
        if (vehicle.getStatus() == VehicleStatus.Inused) {
            throw new ConflictException("VEHICLE_IN_USE", "vehicle in use", "Xe đang được sử dụng, không thể xóa");
        }
        if (sessionVehicleRepository.countActiveForVehicleInStatus(vehicle.getId(), SessionStatus.InProgress) > 0) {
            throw new ConflictException("VEHICLE_IN_SESSION", "vehicle in progress session", "Xe đang thuộc phiên đang làm, không thể xóa");
        }
        if (vehicle.getStatus() == VehicleStatus.Online) {
            throw new ConflictException("VEHICLE_NOT_DELETABLE", "vehicle online", "Hãy chuyển xe sang Nghỉ hoặc Sửa chữa trước khi xóa");
        }
        vehicle.setStatus(VehicleStatus.Deleted);
        return VehicleResponse.from(saveChecked(vehicle));
    }

    private Vehicle findActive(long id) throws VehicleNotExits {
        return vehicleRepository.findById(id)
                .filter(vehicle -> vehicle.getStatus() != VehicleStatus.Deleted)
                .orElseThrow(VehicleNotExits::new);
    }

    private Vehicle findActiveForUpdate(long id) throws VehicleNotExits {
        return vehicleRepository.findByIdForUpdate(id)
                .filter(vehicle -> vehicle.getStatus() != VehicleStatus.Deleted)
                .orElseThrow(VehicleNotExits::new);
    }

    private Vehicle saveChecked(Vehicle vehicle) throws VehicleSlugAlreadyExits {
        try {
            return vehicleRepository.saveAndFlush(vehicle);
        } catch (DataIntegrityViolationException e) {
            if (violatesSlugIndex(e)) {
                throw new VehicleSlugAlreadyExits();
            }
            throw e;
        }
    }

    private boolean violatesSlugIndex(Throwable error) {
        Throwable current = error;
        for (int depth = 0; current != null && depth < MAX_CAUSE_DEPTH; depth++) {
            if (current instanceof ConstraintViolationException violation
                    && SLUG_INDEX.equalsIgnoreCase(violation.getConstraintName())) {
                return true;
            }
            if (current instanceof SQLException sql
                    && UNIQUE_VIOLATION.equals(sql.getSQLState())
                    && sql.getMessage() != null
                    && sql.getMessage().toLowerCase(Locale.ROOT).contains(SLUG_INDEX)) {
                return true;
            }
            current = current.getCause() == current ? null : current.getCause();
        }
        return false;
    }

    private VehicleStatus parseStatus(String status) throws ValidationException {
        try {
            return VehicleStatus.valueOf(status.trim());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("status", "Trạng thái xe không hợp lệ");
        }
    }
}
