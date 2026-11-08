package org.group6.childrenelectricvehiclerentalmanagement.Controllers;

import org.group6.childrenelectricvehiclerentalmanagement.DTOs.PackageController.CreatePackageRequest;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.PackageController.UpdatePackageRequest;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.SettingsController.ToggleRequest;
import org.group6.childrenelectricvehiclerentalmanagement.Models.User;
import org.group6.childrenelectricvehiclerentalmanagement.Services.PackageService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/package")
public class PackageController extends BaseController {
    private final PackageService packageService;

    public PackageController(PackageService packageService) {
        this.packageService = packageService;
    }

    @GetMapping({"", "/"})
    public ResponseEntity<?> listPackages() {
        return ResponseEntity.ok(packageService.listPackages());
    }

    @PostMapping({"", "/"})
    public ResponseEntity<?> createPackage(
            @AuthenticationPrincipal User actor,
            @RequestBody(required = false) CreatePackageRequest request) throws Exception {
        return ResponseEntity.ok(packageService.createPackage(actor, request));
    }

    @PostMapping("/{id}/update")
    public ResponseEntity<?> updatePackage(
            @AuthenticationPrincipal User actor,
            @PathVariable("id") String id,
            @RequestBody(required = false) UpdatePackageRequest request) throws Exception {
        return ResponseEntity.ok(packageService.updatePackage(actor, id, request));
    }

    @PostMapping("/{id}/toggle")
    public ResponseEntity<?> togglePackage(
            @AuthenticationPrincipal User actor,
            @PathVariable("id") String id,
            @RequestBody(required = false) ToggleRequest request) throws Exception {
        return ResponseEntity.ok(packageService.togglePackage(actor, id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePackage(
            @AuthenticationPrincipal User actor,
            @PathVariable("id") String id) throws Exception {
        return ResponseEntity.ok(packageService.deletePackage(actor, id));
    }

    @GetMapping("/{id}/stats")
    public ResponseEntity<?> getStats(
            @PathVariable("id") String id,
            @RequestParam(name = "group_by", required = false) String groupBy,
            @RequestParam(name = "from", required = false) String from,
            @RequestParam(name = "to", required = false) String to) throws Exception {
        return ResponseEntity.ok(packageService.getStats(id, groupBy, from, to));
    }
}
