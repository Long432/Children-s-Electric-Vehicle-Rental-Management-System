package org.group6.childrenelectricvehiclerentalmanagement.DTOs.PackageController;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PackageStatsItem(
        String period,
        @JsonProperty("tickets_sold") long ticketsSold,
        long revenue) {}
