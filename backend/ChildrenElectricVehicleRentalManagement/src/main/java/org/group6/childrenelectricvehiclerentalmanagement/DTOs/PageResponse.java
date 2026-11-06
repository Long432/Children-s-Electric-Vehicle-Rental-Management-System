package org.group6.childrenelectricvehiclerentalmanagement.DTOs;

import java.util.List;

public record PageResponse<T>(List<T> items, int page, int limit, long total) {

    public static <T> PageResponse<T> empty(int page, int limit) {
        return new PageResponse<>(List.of(), page, limit, 0L);
    }
}
