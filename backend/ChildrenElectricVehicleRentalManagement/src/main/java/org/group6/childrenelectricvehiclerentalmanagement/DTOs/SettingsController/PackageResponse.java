package org.group6.childrenelectricvehiclerentalmanagement.DTOs.SettingsController;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.group6.childrenelectricvehiclerentalmanagement.Models.Package;

public record PackageResponse(
        Long id,
        int amount,
        int minutes,
        @JsonProperty("is_default") boolean defaultPackage,
        boolean active) {

    public static PackageResponse from(Package pricePackage) {
        return new PackageResponse(
                pricePackage.getId(),
                pricePackage.getAmount(),
                pricePackage.getMinutes(),
                Boolean.TRUE.equals(pricePackage.getIsDefault()),
                Boolean.TRUE.equals(pricePackage.getActive()));
    }
}
