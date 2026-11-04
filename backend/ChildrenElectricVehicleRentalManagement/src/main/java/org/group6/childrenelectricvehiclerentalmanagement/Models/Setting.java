package org.group6.childrenelectricvehiclerentalmanagement.Models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "settings")
public class Setting {
    @Id
    @Column(name = "id", nullable = false)
    private Short id;

    @NotNull
    @ColumnDefault("false")
    @Column(name = "refund_enabled", nullable = false)
    private Boolean refundEnabled;

    @NotNull
    @ColumnDefault("true")
    @Column(name = "extend_enabled", nullable = false)
    private Boolean extendEnabled;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "overdue_fee_per_minute", nullable = false)
    private Integer overdueFeePerMinute;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "overdue_grace_minutes", nullable = false)
    private Integer overdueGraceMinutes;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "minimum_minutes", nullable = false)
    private Integer minimumMinutes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;

    @NotNull
    @ColumnDefault("now()")
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @NotNull
    @ColumnDefault("now()")
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;


}