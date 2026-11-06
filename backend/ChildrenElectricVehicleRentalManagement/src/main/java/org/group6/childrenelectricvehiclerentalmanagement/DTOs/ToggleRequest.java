package org.group6.childrenelectricvehiclerentalmanagement.DTOs;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Body của POST /api/settings/toggle_refund và /toggle_extend: { "enabled": true } */
@Getter
@Setter
@NoArgsConstructor
public class ToggleRequest {
    private Boolean enabled;
}
