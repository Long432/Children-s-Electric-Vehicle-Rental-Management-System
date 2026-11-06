package org.group6.childrenelectricvehiclerentalmanagement.Models;

import jakarta.persistence.*;
import lombok.Getter;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.VehicleStatus;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.TimeUtil;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "vehicles")
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "slug", nullable = false, length = Integer.MAX_VALUE)
    private String slug;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private VehicleStatus status;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @ColumnDefault("now()")
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ColumnDefault("now()")
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Vehicle() {
    }

    public Vehicle(String slug) {
        LocalDateTime now = TimeUtil.nowUtc();
        this.slug = slug;
        this.status = VehicleStatus.Offline;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public void setSlug(String slug) {
        this.slug = slug;
        this.updatedAt = TimeUtil.nowUtc();
    }

    public void setStatus(VehicleStatus status) {
        LocalDateTime now = TimeUtil.nowUtc();
        this.status = status;
        if (status == VehicleStatus.Deleted) this.deletedAt = now;
        this.updatedAt = now;
    }
}
