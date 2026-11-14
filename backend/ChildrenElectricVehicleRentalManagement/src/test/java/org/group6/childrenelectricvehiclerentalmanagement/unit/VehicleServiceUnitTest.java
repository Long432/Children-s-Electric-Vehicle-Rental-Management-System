package org.group6.childrenelectricvehiclerentalmanagement.unit;

import org.group6.childrenelectricvehiclerentalmanagement.DTOs.VehicleController.UpdateVehicleSlugRequest;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.VehicleController.UpdateVehicleStatusRequest;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.VehicleController.createVehicleRequest;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.SessionStatus;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.VehicleStatus;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.*;
import org.group6.childrenelectricvehiclerentalmanagement.Models.User;
import org.group6.childrenelectricvehiclerentalmanagement.Models.Vehicle;
import org.group6.childrenelectricvehiclerentalmanagement.Models.LinkingObject.SessionVehicle;
import org.group6.childrenelectricvehiclerentalmanagement.Repositories.SessionVehicleRepository;
import org.group6.childrenelectricvehiclerentalmanagement.Repositories.VehicleRepository;
import org.group6.childrenelectricvehiclerentalmanagement.Services.VehicleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit test cho VehicleService. Dependencies được mock.
 * Dựa trên nghiệp vụ quản lý xe điện (features.md section 2.2, phân tích nghiệp vụ section 5).
 */
@ExtendWith(MockitoExtension.class)
class VehicleServiceUnitTest {

    @Mock private VehicleRepository vehicleRepository;
    @Mock private SessionVehicleRepository sessionVehicleRepository;
    @Mock private org.group6.childrenelectricvehiclerentalmanagement.Services.AuditLogService auditLogService;
    @InjectMocks private VehicleService vehicleService;

    private User actor;

    @BeforeEach
    void setUp() {
        actor = new User();
        actor.setId(1L);
        actor.setUsername("seller");
    }

    // ==================== createNewVehicle ====================

    @Test
    void createNewVehicle_success() throws Exception {
        createVehicleRequest req = new createVehicleRequest("xe-01");
        when(vehicleRepository.findBySlugAndDeletedAtIsNull("xe-01")).thenReturn(Optional.empty());
        when(vehicleRepository.saveAndFlush(any(Vehicle.class))).thenAnswer(inv -> {
            Vehicle v = inv.getArgument(0);
            v.setId(1L);
            return v;
        });

        Vehicle res = vehicleService.createNewVehicle(req, actor);

        assertNotNull(res);
        assertEquals("xe-01", res.getSlug());
        assertEquals(VehicleStatus.Offline, res.getStatus());
    }

    @Test
    void createNewVehicle_slugTaken_shouldThrowVehicleSlugAlreadyExits() throws Exception {
        createVehicleRequest req = new createVehicleRequest("xe-01");
        when(vehicleRepository.findBySlugAndDeletedAtIsNull("xe-01")).thenReturn(Optional.of(new Vehicle("xe-01")));

        assertThrows(VehicleSlugAlreadyExits.class, () -> vehicleService.createNewVehicle(req, actor));
    }

    @Test
    void createNewVehicle_blankSlug_shouldThrowValidationException() throws Exception {
        createVehicleRequest req = new createVehicleRequest("   ");

        assertThrows(ValidationException.class, () -> vehicleService.createNewVehicle(req, actor));
    }

    // ==================== getVehicle ====================

    @Test
    void getVehicle_success() throws Exception {
        Vehicle vehicle = new Vehicle("xe-01");
        vehicle.setId(1L);
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

        Vehicle res = vehicleService.getVehicle(1L);

        assertNotNull(res);
        assertEquals("xe-01", res.getSlug());
    }

    @Test
    void getVehicle_notFound_shouldThrowVehicleNotExits() throws Exception {
        when(vehicleRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(VehicleNotExits.class, () -> vehicleService.getVehicle(1L));
    }

    // ==================== updateVehicleSlug ====================

    @Test
    void updateVehicleSlug_offline_shouldRename() throws Exception {
        Vehicle vehicle = new Vehicle("xe-01");
        vehicle.setId(1L);
        vehicle.setStatus(VehicleStatus.Offline);

        UpdateVehicleSlugRequest req = new UpdateVehicleSlugRequest("xe-02");

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(vehicleRepository.lockBySlug("xe-01")).thenReturn(Optional.of(vehicle));
        when(vehicleRepository.findBySlugAndDeletedAtIsNull("xe-02")).thenReturn(Optional.empty());
        when(vehicleRepository.saveAndFlush(any(Vehicle.class))).thenReturn(vehicle);

        Vehicle res = vehicleService.updateVehicleSlug(1L, req, actor);

        assertNotNull(res);
        assertEquals("xe-02", res.getSlug());
    }

    @Test
    void updateVehicleSlug_inused_shouldThrowVehicleInUse() throws Exception {
        Vehicle vehicle = new Vehicle("xe-01");
        vehicle.setId(1L);
        vehicle.setStatus(VehicleStatus.Inused);

        UpdateVehicleSlugRequest req = new UpdateVehicleSlugRequest("xe-02");

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(vehicleRepository.lockBySlug("xe-01")).thenReturn(Optional.of(vehicle));

        assertThrows(VehicleInUse.class, () -> vehicleService.updateVehicleSlug(1L, req, actor));
    }

    @Test
    void updateVehicleSlug_notOffline_shouldThrow() throws Exception {
        Vehicle vehicle = new Vehicle("xe-01");
        vehicle.setId(1L);
        vehicle.setStatus(VehicleStatus.Online);

        UpdateVehicleSlugRequest req = new UpdateVehicleSlugRequest("xe-02");

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(vehicleRepository.lockBySlug("xe-01")).thenReturn(Optional.of(vehicle));

        assertThrows(AbstractException.class, () -> vehicleService.updateVehicleSlug(1L, req, actor));
    }

    @Test
    void updateVehicleSlug_slugTaken_shouldThrowVehicleSlugAlreadyExits() throws Exception {
        Vehicle vehicle = new Vehicle("xe-01");
        vehicle.setId(1L);
        vehicle.setStatus(VehicleStatus.Offline);

        UpdateVehicleSlugRequest req = new UpdateVehicleSlugRequest("xe-02");

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(vehicleRepository.lockBySlug("xe-01")).thenReturn(Optional.of(vehicle));
        when(vehicleRepository.findBySlugAndDeletedAtIsNull("xe-02")).thenReturn(Optional.of(new Vehicle("xe-02")));

        assertThrows(VehicleSlugAlreadyExits.class, () -> vehicleService.updateVehicleSlug(1L, req, actor));
    }

    @Test
    void updateVehicleSlug_sameSlug_shouldReturnUnchanged() throws Exception {
        Vehicle vehicle = new Vehicle("xe-01");
        vehicle.setId(1L);
        vehicle.setStatus(VehicleStatus.Offline);

        UpdateVehicleSlugRequest req = new UpdateVehicleSlugRequest("xe-01");

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(vehicleRepository.lockBySlug("xe-01")).thenReturn(Optional.of(vehicle));

        Vehicle res = vehicleService.updateVehicleSlug(1L, req, actor);

        assertNotNull(res);
        assertEquals("xe-01", res.getSlug());
        verify(vehicleRepository, never()).saveAndFlush(any());
    }

    // ==================== updateVehicleStatus ====================

    @Test
    void updateVehicleStatus_onlineToRepairing_shouldChange() throws Exception {
        Vehicle vehicle = new Vehicle("xe-01");
        vehicle.setId(1L);
        vehicle.setStatus(VehicleStatus.Online);

        UpdateVehicleStatusRequest req = new UpdateVehicleStatusRequest("Repairing");

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(vehicleRepository.lockBySlug("xe-01")).thenReturn(Optional.of(vehicle));
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(vehicle);

        Vehicle res = vehicleService.updateVehicleStatus(1L, req, actor);

        assertNotNull(res);
        assertEquals(VehicleStatus.Repairing, res.getStatus());
    }

    @Test
    void updateVehicleStatus_inused_shouldThrowVehicleInUse() throws Exception {
        Vehicle vehicle = new Vehicle("xe-01");
        vehicle.setId(1L);
        vehicle.setStatus(VehicleStatus.Inused);

        UpdateVehicleStatusRequest req = new UpdateVehicleStatusRequest("Offline");

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(vehicleRepository.lockBySlug("xe-01")).thenReturn(Optional.of(vehicle));

        assertThrows(VehicleInUse.class, () -> vehicleService.updateVehicleStatus(1L, req, actor));
    }

    @Test
    void updateVehicleStatus_sameStatus_shouldThrow() throws Exception {
        Vehicle vehicle = new Vehicle("xe-01");
        vehicle.setId(1L);
        vehicle.setStatus(VehicleStatus.Online);

        UpdateVehicleStatusRequest req = new UpdateVehicleStatusRequest("Online");

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(vehicleRepository.lockBySlug("xe-01")).thenReturn(Optional.of(vehicle));

        assertThrows(AbstractException.class, () -> vehicleService.updateVehicleStatus(1L, req, actor));
    }

    @Test
    void updateVehicleStatus_invalidStatus_shouldThrowValidationException() throws Exception {
        Vehicle vehicle = new Vehicle("xe-01");
        vehicle.setId(1L);
        vehicle.setStatus(VehicleStatus.Online);

        UpdateVehicleStatusRequest req = new UpdateVehicleStatusRequest("Inused"); // không được set qua API

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

        assertThrows(ValidationException.class, () -> vehicleService.updateVehicleStatus(1L, req, actor));
    }

    // ==================== deleteVehicle ====================

    @Test
    void deleteVehicle_offline_notInActiveSession_shouldSoftDelete() throws Exception {
        Vehicle vehicle = new Vehicle("xe-01");
        vehicle.setId(1L);
        vehicle.setStatus(VehicleStatus.Offline);

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(vehicleRepository.lockBySlug("xe-01")).thenReturn(Optional.of(vehicle));
        when(sessionVehicleRepository.existsByVehicle_IdAndRemovedAtIsNullAndSession_Status(eq(1L), eq(SessionStatus.InProgress))).thenReturn(false);
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(vehicle);

        vehicleService.deleteVehicle(1L, actor);

        assertEquals(VehicleStatus.Deleted, vehicle.getStatus());
        assertNotNull(vehicle.getDeletedAt());
    }

    @Test
    void deleteVehicle_inused_shouldThrowVehicleInUse() throws Exception {
        Vehicle vehicle = new Vehicle("xe-01");
        vehicle.setId(1L);
        vehicle.setStatus(VehicleStatus.Inused);

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(vehicleRepository.lockBySlug("xe-01")).thenReturn(Optional.of(vehicle));

        assertThrows(VehicleInUse.class, () -> vehicleService.deleteVehicle(1L, actor));
    }

    @Test
    void deleteVehicle_online_shouldThrow() throws Exception {
        Vehicle vehicle = new Vehicle("xe-01");
        vehicle.setId(1L);
        vehicle.setStatus(VehicleStatus.Online);

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(vehicleRepository.lockBySlug("xe-01")).thenReturn(Optional.of(vehicle));

        assertThrows(AbstractException.class, () -> vehicleService.deleteVehicle(1L, actor));
    }

    @Test
    void deleteVehicle_inActiveSession_shouldThrow() throws Exception {
        Vehicle vehicle = new Vehicle("xe-01");
        vehicle.setId(1L);
        vehicle.setStatus(VehicleStatus.Offline);

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(vehicleRepository.lockBySlug("xe-01")).thenReturn(Optional.of(vehicle));
        when(sessionVehicleRepository.existsByVehicle_IdAndRemovedAtIsNullAndSession_Status(eq(1L), eq(SessionStatus.InProgress))).thenReturn(true);

        assertThrows(AbstractException.class, () -> vehicleService.deleteVehicle(1L, actor));
    }

    @Test
    void deleteVehicle_alreadyDeleted_shouldThrowUserException() throws Exception {
        Vehicle vehicle = new Vehicle("xe-01");
        vehicle.setId(1L);
        vehicle.setStatus(VehicleStatus.Deleted);
        vehicle.setDeletedAt(java.time.LocalDateTime.now());

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

        assertThrows(UserException.class, () -> vehicleService.deleteVehicle(1L, actor));
    }
}
