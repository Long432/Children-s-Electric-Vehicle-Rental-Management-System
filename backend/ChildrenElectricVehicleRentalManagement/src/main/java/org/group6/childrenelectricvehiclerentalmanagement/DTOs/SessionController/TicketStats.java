package org.group6.childrenelectricvehiclerentalmanagement.DTOs.SessionController;

import java.util.Map;

public record TicketStats(int total, Map<String, Integer> byStatus, long totalMinutesSold) {
}
