package org.group6.childrenelectricvehiclerentalmanagement.Models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.VehicleStatus;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "vehicles")
public class Vehicle extends Timestamped {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "slug", nullable = false, length = Integer.MAX_VALUE)
    private String slug;

    @Column(name = "status", nullable = false)
    private VehicleStatus status;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public Vehicle(String slug) {
        this.slug = slug;
        this.status = VehicleStatus.Offline;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public void setStatus(VehicleStatus status) {
        this.status = status;
        if (status == VehicleStatus.Deleted) this.deletedAt = java.time.LocalDateTime.now();
    }

    public Vehicle() {}
}
