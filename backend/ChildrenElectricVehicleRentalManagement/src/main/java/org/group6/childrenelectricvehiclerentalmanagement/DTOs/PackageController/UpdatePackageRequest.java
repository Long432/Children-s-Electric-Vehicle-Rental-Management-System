package org.group6.childrenelectricvehiclerentalmanagement.DTOs.PackageController;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpdatePackageRequest {
    private Integer amount;
    private Integer minutes;

    @JsonProperty("is_default")
    private Boolean isDefault;
}
