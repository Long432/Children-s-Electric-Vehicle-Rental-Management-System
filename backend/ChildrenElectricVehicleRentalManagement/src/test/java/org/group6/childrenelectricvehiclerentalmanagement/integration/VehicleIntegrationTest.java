package org.group6.childrenelectricvehiclerentalmanagement.integration;

import org.group6.childrenelectricvehiclerentalmanagement.DTOs.VehicleController.UpdateVehicleSlugRequest;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.VehicleController.UpdateVehicleStatusRequest;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.VehicleController.createVehicleRequest;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.VehicleStatus;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.VehicleSlugAlreadyExits;
import org.group6.childrenelectricvehiclerentalmanagement.Models.User;
import org.group6.childrenelectricvehiclerentalmanagement.Models.Vehicle;
import org.group6.childrenelectricvehiclerentalmanagement.Services.VehicleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test cho VehicleService với H2 in-memory database.
 * Luồng kiểm tra: thêm xe → sửa slug → đổi trạng thái → xóa mềm.
 */
class VehicleIntegrationTest extends AbstractIntegrationTest {

    @Autowired private VehicleService vehicleService;

    @Test
    void createNewVehicle_success() throws Exception {
        createVehicleRequest req = new createVehicleRequest("xe-02");

        Vehicle res = vehicleService.createNewVehicle(req, seller);

        assertNotNull(res);
        assertEquals("xe-02", res.getSlug());
        assertEquals(VehicleStatus.Offline, res.getStatus());
    }

    @Test
    void createNewVehicle_duplicateSlug_shouldThrow() throws Exception {
        createVehicleRequest req = new createVehicleRequest("xe-01"); // đã có trong setUp

        assertThrows(VehicleSlugAlreadyExits.class, () -> vehicleService.createNewVehicle(req, seller));
    }

    @Test
    void updateVehicleSlug_offline_shouldRename() throws Exception {
        Vehicle offline = new Vehicle("xe-offline");
        offline.setStatus(VehicleStatus.Offline);
        vehicleRepository.save(offline);

        UpdateVehicleSlugRequest req = new UpdateVehicleSlugRequest("xe-offline-new");

        Vehicle res = vehicleService.updateVehicleSlug(offline.getId(), req, seller);

        assertNotNull(res);
        assertEquals("xe-offline-new", res.getSlug());
    }

    @Test
    void updateVehicleStatus_shouldChange() throws Exception {
        UpdateVehicleStatusRequest req = new UpdateVehicleStatusRequest("Repairing");

        Vehicle res = vehicleService.updateVehicleStatus(vehicle.getId(), req, seller);

        assertNotNull(res);
        assertEquals(VehicleStatus.Repairing, res.getStatus());
    }

    @Test
    void deleteVehicle_offline_shouldSoftDelete() throws Exception {
        Vehicle offline = new Vehicle("xe-to-delete");
        offline.setStatus(VehicleStatus.Offline);
        vehicleRepository.save(offline);

        vehicleService.deleteVehicle(offline.getId(), seller);

        assertEquals(VehicleStatus.Deleted, offline.getStatus());
        assertNotNull(offline.getDeletedAt());
    }
}
