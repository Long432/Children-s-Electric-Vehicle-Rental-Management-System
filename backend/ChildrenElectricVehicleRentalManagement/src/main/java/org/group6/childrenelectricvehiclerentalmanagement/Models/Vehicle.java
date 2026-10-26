package org.group6.childrenelectricvehiclerentalmanagement.Models;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.Getter;
import lombok.Setter;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.VehicleStatus;
import org.hibernate.annotations.Audited;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Audited.Table(name = "vehicles")
public class Vehicle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "slug", nullable = false, length = Integer.MAX_VALUE)
    private String slug;

    @ColumnDefault("'Offline'")
    @Column(name = "status", columnDefinition = "vehicle_status not null")
    private VehicleStatus status;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @ColumnDefault("now()")
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ColumnDefault("now()")
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;


}