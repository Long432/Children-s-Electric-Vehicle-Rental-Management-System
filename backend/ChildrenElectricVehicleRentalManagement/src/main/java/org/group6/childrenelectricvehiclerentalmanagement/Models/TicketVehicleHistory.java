package org.group6.childrenelectricvehiclerentalmanagement.Models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.ReleasedVehicleStatus;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "ticket_vehicle_history")
public class TicketVehicleHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "ticket_id", nullable = false)
    private Long ticketId;

    @Column(name = "vehicle_id", nullable = false)
    private Long vehicleId;

    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt;

    @Column(name = "released_at")
    private LocalDateTime releasedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "released_status")
    private ReleasedVehicleStatus releasedStatus;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public TicketVehicleHistory() {
    }

    public TicketVehicleHistory(Long ticketId, Long vehicleId, LocalDateTime now) {
        this.ticketId = ticketId;
        this.vehicleId = vehicleId;
        this.assignedAt = now;
        this.createdAt = now;
        this.updatedAt = now;
    }
}
