package org.group6.childrenelectricvehiclerentalmanagement.DTOs.SessionController;

import java.util.List;

public record SessionPageResponse(
        List<SessionBasicResponse> items,
        int page,
        int limit,
        long total,
        int totalPages
) {}
