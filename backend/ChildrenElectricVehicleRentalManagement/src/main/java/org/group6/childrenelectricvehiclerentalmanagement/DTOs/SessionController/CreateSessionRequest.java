package org.group6.childrenelectricvehiclerentalmanagement.DTOs.SessionController;

import lombok.NonNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record CreateSessionRequest(
        @NonNull
        LocalDate start_date,
        @NonNull
        LocalTime start_time,
        LocalDate end_date,
        LocalTime end_time
        ) {

}
