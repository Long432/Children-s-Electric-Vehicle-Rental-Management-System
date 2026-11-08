package org.group6.childrenelectricvehiclerentalmanagement.DTOs.SessionController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * PATCH /sessions/{id}: gửi nguyên request.
 * Trường nào bắt buộc mà thiếu sẽ bị từ chối (422).
 * is_instant luôn bắt buộc; phiên đã lên lịch (Waiting) không thể chuyển sang instant nên chỉ nhận false.
 */
public record UpdateSessionRequest(
        LocalDateTime planned_end_at,
        LocalDate scheduled_date,
        LocalTime scheduled_time,
        Boolean is_instant
) {}
