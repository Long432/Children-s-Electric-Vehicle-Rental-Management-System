package org.group6.childrenelectricvehiclerentalmanagement.DTOs.Session;

import java.util.List;

public record StartSessionRequest(List<String> vehicle_slugs, Long session_id, String end_time) {
}
