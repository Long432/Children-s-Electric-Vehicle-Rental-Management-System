package org.group6.childrenelectricvehiclerentalmanagement.Models.LinkingObject;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.group6.childrenelectricvehiclerentalmanagement.Models.Session;
import org.group6.childrenelectricvehiclerentalmanagement.Models.Timestamped;
import org.group6.childrenelectricvehiclerentalmanagement.Models.Vehicle;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "session_vehicles")
public class SessionVehicle extends Timestamped {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @Column(name = "added_at", nullable = false)
    private LocalDateTime addedAt;

    @Column(name = "removed_at")
    private LocalDateTime removedAt;
}
