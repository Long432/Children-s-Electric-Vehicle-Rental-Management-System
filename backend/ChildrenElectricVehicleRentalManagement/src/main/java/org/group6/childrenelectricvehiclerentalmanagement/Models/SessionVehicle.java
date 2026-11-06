package org.group6.childrenelectricvehiclerentalmanagement.Models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "session_vehicles")
public class SessionVehicle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "session_id", nullable = false)
    private Long sessionId;

    @Column(name = "vehicle_id", nullable = false)
    private Long vehicleId;

    @Column(name = "added_at", nullable = false)
    private LocalDateTime addedAt;

    @Column(name = "removed_at")
    private LocalDateTime removedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public SessionVehicle() {
    }

    public SessionVehicle(Long sessionId, Long vehicleId, LocalDateTime now) {
        this.sessionId = sessionId;
        this.vehicleId = vehicleId;
        this.addedAt = now;
        this.createdAt = now;
        this.updatedAt = now;
    }
}
