package org.group6.childrenelectricvehiclerentalmanagement.DTOs.SessionController;

import java.util.Map;

public record VehicleStats(int total, Map<String, Integer> byStatus) {
}
