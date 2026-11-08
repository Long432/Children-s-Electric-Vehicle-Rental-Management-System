package org.group6.childrenelectricvehiclerentalmanagement.DTOs.SessionController;

import java.time.LocalDate;
import java.time.LocalTime;

public record CreateSessionRequest(
        LocalDate start_date,
        LocalTime start_time,
        LocalDate end_date,
        LocalTime end_time,
        Boolean is_instant
) {

}
