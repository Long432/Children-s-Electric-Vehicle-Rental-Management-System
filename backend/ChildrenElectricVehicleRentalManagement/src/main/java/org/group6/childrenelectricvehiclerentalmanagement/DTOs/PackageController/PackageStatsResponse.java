package org.group6.childrenelectricvehiclerentalmanagement.DTOs.PackageController;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.SettingsController.PackageResponse;

import java.util.List;

public record PackageStatsResponse(
        @JsonProperty("package") PackageResponse pricePackage,
        @JsonProperty("group_by") String groupBy,
        String from,
        String to,
        @JsonProperty("total_tickets_sold") long totalTicketsSold,
        @JsonProperty("total_revenue") long totalRevenue,
        List<PackageStatsItem> items) {}
