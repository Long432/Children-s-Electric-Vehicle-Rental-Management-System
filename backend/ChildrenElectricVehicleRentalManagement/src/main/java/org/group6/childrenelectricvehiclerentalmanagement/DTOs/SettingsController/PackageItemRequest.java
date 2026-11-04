package org.group6.childrenelectricvehiclerentalmanagement.DTOs.SettingsController;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Một gói giá: { "amount": 15000, "minutes": 30, "is_default": true } */
@Getter
@Setter
@NoArgsConstructor
public class PackageItemRequest {
    private Integer amount;
    private Integer minutes;

    @JsonProperty("is_default")
    private Boolean isDefault;
}
