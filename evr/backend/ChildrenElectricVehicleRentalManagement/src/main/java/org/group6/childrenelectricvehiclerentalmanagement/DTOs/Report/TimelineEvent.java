package org.group6.childrenelectricvehiclerentalmanagement.DTOs.Report;

public record TimelineEvent(
        String at,
        UserRef user,
        String action,
        String entity_type,
        Long entity_id,
        String payload) {
}
