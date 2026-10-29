package org.group6.childrenelectricvehiclerentalmanagement.Controllers;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.VehicleController.UpdateVehicleSlugRequest;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.VehicleController.UpdateVehicleStatusRequest;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.VehicleController.createVehicleRequest;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.VehicleController.vehicleInfoRespond;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.VehicleStatus;
import org.group6.childrenelectricvehiclerentalmanagement.Models.Vehicle;
import org.group6.childrenelectricvehiclerentalmanagement.Services.VehicleService;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.Validators.InputValidator;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping(path="/vehicle")
@AllArgsConstructor
@Slf4j
public class VehicleController extends BaseController {
    private VehicleService vehicleService;

    @PostMapping()
    public ResponseEntity<?> createNewVehicle(@RequestBody createVehicleRequest request) throws Exception {
        log.info("Nhận được");
        InputValidator.isVehicleNameValid(request.vehicle_slug());
        return ResponseEntity.ok(new vehicleInfoRespond(vehicleService.createNewVehicle(request)));
    }

    @GetMapping()
    public ResponseEntity<?> getAllVehicles() throws Exception {
        return ResponseEntity.ok(vehicleService.getAllVehicle().stream().map(vehicleInfoRespond::new));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOneVehicleDetail(@PathVariable long id) throws Exception {
        return ResponseEntity.ok(new vehicleInfoRespond(vehicleService.getVehicle(id)));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> updateVehicleSlug(@PathVariable long id, @RequestBody UpdateVehicleSlugRequest request) throws Exception {
        InputValidator.isVehicleNameValid(request.vehicle_slug());
        Vehicle updatedVehicle = vehicleService.updateVehicleSlug(id,request);
        return ResponseEntity.ok(new vehicleInfoRespond(updatedVehicle));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateVehicleStatus(@PathVariable long id, @RequestBody UpdateVehicleStatusRequest request) throws Exception {
        log.info(request.vehicle_status());
        InputValidator.isVehicleStatusValid(request.vehicle_status());
        Vehicle updatedVehicle = vehicleService.updateVehicleStatus(id,VehicleStatus.valueOf(request.vehicle_status()));
        return ResponseEntity.ok(new vehicleInfoRespond(updatedVehicle));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteVehicle(@PathVariable long id) throws Exception {
        vehicleService.deleteVehicle(id);
        return ResponseEntity.ok("Xóa xe thành công");
    }
}
