package org.group6.childrenelectricvehiclerentalmanagement.Controllers;

import org.group6.childrenelectricvehiclerentalmanagement.DTOs.ApiResponse;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Vehicle.CreateVehicleRequest;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Vehicle.UpdateVehicleSlugRequest;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Vehicle.UpdateVehicleStatusRequest;
import org.group6.childrenelectricvehiclerentalmanagement.Services.VehicleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController extends BaseController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @GetMapping({"", "/"})
    public ResponseEntity<ApiResponse> list(@RequestParam(required = false) String status,
                                            @RequestParam(name = "session_id", required = false) Long sessionId) throws Exception {
        return ResponseEntity.ok(ApiResponse.success(vehicleService.list(status, sessionId)));
    }

    @PostMapping({"", "/"})
    public ResponseEntity<ApiResponse> create(@RequestBody(required = false) CreateVehicleRequest request) throws Exception {
        return ResponseEntity.ok(ApiResponse.success(vehicleService.create(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> get(@PathVariable long id) throws Exception {
        return ResponseEntity.ok(ApiResponse.success(vehicleService.get(id)));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse> rename(@PathVariable long id, @RequestBody(required = false) UpdateVehicleSlugRequest request) throws Exception {
        return ResponseEntity.ok(ApiResponse.success(vehicleService.rename(id, request)));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse> changeStatus(@PathVariable long id, @RequestBody(required = false) UpdateVehicleStatusRequest request) throws Exception {
        return ResponseEntity.ok(ApiResponse.success(vehicleService.changeStatus(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> delete(@PathVariable long id) throws Exception {
        return ResponseEntity.ok(ApiResponse.success(vehicleService.delete(id)));
    }
}
