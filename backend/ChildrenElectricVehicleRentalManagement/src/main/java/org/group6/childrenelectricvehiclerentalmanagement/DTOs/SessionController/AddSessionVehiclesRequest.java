package org.group6.childrenelectricvehiclerentalmanagement.DTOs.SessionController;

import java.util.List;

/** POST /sessions/current/vehicles */
public record AddSessionVehiclesRequest(List<String> vehicle_slugs) {}
