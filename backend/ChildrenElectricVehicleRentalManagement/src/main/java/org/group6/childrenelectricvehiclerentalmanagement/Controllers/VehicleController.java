package org.group6.childrenelectricvehiclerentalmanagement.Controllers;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.VehicleController.UpdateVehicleSlugRequest;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.VehicleController.UpdateVehicleStatusRequest;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.VehicleController.createVehicleRequest;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.VehicleController.vehicleInfoRespond;
import org.group6.childrenelectricvehiclerentalmanagement.Models.User;
import org.group6.childrenelectricvehiclerentalmanagement.Services.VehicleService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/vehicles")
@AllArgsConstructor
@Slf4j
public class VehicleController extends BaseController {
    private VehicleService vehicleService;

    @PostMapping
    public ResponseEntity<?> createNewVehicle(@RequestBody createVehicleRequest request,
                                              @AuthenticationPrincipal User actor) throws Exception {
        return ResponseEntity.ok(new vehicleInfoRespond(vehicleService.createNewVehicle(request, actor)));
    }

    /** GET /api/vehicle?status=Online&status=Offline&session_id=3 (mặc định không gồm xe đã xóa). */
    @GetMapping
    public ResponseEntity<?> getAllVehicles(@RequestParam(name = "status", required = false) List<String> status,
                                            @RequestParam(name = "session_id", required = false) String sessionId) throws Exception {
        return ResponseEntity.ok(vehicleService.getAllVehicle(status, sessionId).stream()
                .map(vehicleInfoRespond::new)
                .toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOneVehicleDetail(@PathVariable long id) throws Exception {
        return ResponseEntity.ok(new vehicleInfoRespond(vehicleService.getVehicle(id)));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> updateVehicleSlug(@PathVariable long id,
                                               @RequestBody UpdateVehicleSlugRequest request,
                                               @AuthenticationPrincipal User actor) throws Exception {
        return ResponseEntity.ok(new vehicleInfoRespond(vehicleService.updateVehicleSlug(id, request, actor)));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateVehicleStatus(@PathVariable long id,
                                                 @RequestBody UpdateVehicleStatusRequest request,
                                                 @AuthenticationPrincipal User actor) throws Exception {
        return ResponseEntity.ok(new vehicleInfoRespond(vehicleService.updateVehicleStatus(id, request, actor)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteVehicle(@PathVariable long id,
                                           @AuthenticationPrincipal User actor) throws Exception {
        vehicleService.deleteVehicle(id, actor);
        return ResponseEntity.ok("Xóa xe thành công");
    }
}
