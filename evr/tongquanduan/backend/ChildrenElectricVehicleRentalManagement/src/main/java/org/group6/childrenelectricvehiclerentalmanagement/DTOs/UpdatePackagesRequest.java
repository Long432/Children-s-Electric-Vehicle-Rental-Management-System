package org.group6.childrenelectricvehiclerentalmanagement.DTOs;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/** Body của PUT /api/settings: { "packages": [ { "amount": 15000, "minutes": 30, "is_default": true } ] } */
@Getter
@Setter
@NoArgsConstructor
public class UpdatePackagesRequest {
    private List<PackageItemRequest> packages;
}
