package org.group6.childrenelectricvehiclerentalmanagement.DTOs.Common;

import java.util.List;

public record PageResponse<T>(List<T> items, int page, int limit, long total, int totalPages,
                              Long sessionId) {
}
