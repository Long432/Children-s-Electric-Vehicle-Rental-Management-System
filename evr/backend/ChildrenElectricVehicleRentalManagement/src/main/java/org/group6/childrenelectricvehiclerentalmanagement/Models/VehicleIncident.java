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
import org.group6.childrenelectricvehiclerentalmanagement.Enum.IncidentSource;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "vehicle_incidents")
public class VehicleIncident {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "vehicle_id", nullable = false)
    private Long vehicleId;

    @Column(name = "session_id")
    private Long sessionId;

    @Column(name = "ticket_id")
    private Long ticketId;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false)
    private IncidentSource source;

    @Column(name = "reported_by", nullable = false)
    private Long reportedBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public VehicleIncident() {
    }

    public VehicleIncident(Long vehicleId, Long sessionId, Long ticketId, IncidentSource source, Long reportedBy, LocalDateTime createdAt) {
        this.vehicleId = vehicleId;
        this.sessionId = sessionId;
        this.ticketId = ticketId;
        this.source = source;
        this.reportedBy = reportedBy;
        this.createdAt = createdAt;
    }
}
