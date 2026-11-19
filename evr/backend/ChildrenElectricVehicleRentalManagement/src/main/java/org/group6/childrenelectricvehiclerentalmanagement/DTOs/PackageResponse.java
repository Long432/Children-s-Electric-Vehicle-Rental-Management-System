package org.group6.childrenelectricvehiclerentalmanagement.DTOs;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class PackageResponse {
    private final Long id;
    private final int amount;
    private final int minutes;

    // Tên trường Java khác "isDefault" để getter do Lombok sinh (isDefaultPackage) khớp tên trường,
    // nếu không Jackson sẽ xuất thêm một khóa "default" thừa bên cạnh "is_default".
    @JsonProperty("is_default")
    private final boolean defaultPackage;

    public PackageResponse(Long id, int amount, int minutes, boolean defaultPackage) {
        this.id = id;
        this.amount = amount;
        this.minutes = minutes;
        this.defaultPackage = defaultPackage;
    }
}
