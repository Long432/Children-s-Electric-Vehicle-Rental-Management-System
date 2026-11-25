package org.group6.childrenelectricvehiclerentalmanagement.DTOs;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Body của set_overdue_limit và set_minimum_time: { "minutes": 5 } */
@Getter
@Setter
@NoArgsConstructor
public class MinutesRequest {
    private Integer minutes;
}
