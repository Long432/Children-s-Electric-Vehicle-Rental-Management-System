package org.group6.childrenelectricvehiclerentalmanagement.DTOs.SessionController;

public record SessionBrief(Long id, String status, String scheduledDate, String scheduledTime,
                           String plannedEndAt, String startedAt, String endedAt, Boolean isInstant) {
}
