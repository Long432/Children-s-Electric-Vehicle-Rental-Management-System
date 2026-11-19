package org.group6.childrenelectricvehiclerentalmanagement.DTOs;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Body của POST /api/settings/set_overdue_fee: { "amount_per_minute": 2000 } */
@Getter
@Setter
@NoArgsConstructor
public class AmountPerMinuteRequest {
    @JsonProperty("amount_per_minute")
    private Integer amountPerMinute;
}
