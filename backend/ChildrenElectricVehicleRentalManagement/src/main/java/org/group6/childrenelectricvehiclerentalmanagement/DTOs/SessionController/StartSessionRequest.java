package org.group6.childrenelectricvehiclerentalmanagement.DTOs.SessionController;

import java.time.LocalDateTime;
import java.util.List;

/**
 * POST /sessions/start
 * - Có session_id: chuyển phiên Waiting sang InProgress.
 * - Không có session_id: tạo phiên instant InProgress ngay (planned_end_at mặc định 00:00 hôm sau).
 * vehicle_slugs: các xe được đưa vào phiên và chuyển sang Online (có thể rỗng).
 */
public record StartSessionRequest(
        Long session_id,
        List<String> vehicle_slugs,
        LocalDateTime planned_end_at
) {}
