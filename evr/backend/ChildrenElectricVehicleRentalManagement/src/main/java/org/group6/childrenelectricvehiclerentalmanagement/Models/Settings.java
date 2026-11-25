package org.group6.childrenelectricvehiclerentalmanagement.Models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "settings")
public class Settings {
    @Id
    @Column(name = "id", nullable = false)
    private Short id;

    @ColumnDefault("false")
    @Column(name = "refund_enabled", nullable = false)
    private Boolean refundEnabled;

    @ColumnDefault("true")
    @Column(name = "extend_enabled", nullable = false)
    private Boolean extendEnabled;

    @ColumnDefault("0")
    @Column(name = "overdue_fee_per_minute", nullable = false)
    private Integer overdueFeePerMinute;

    @ColumnDefault("0")
    @Column(name = "overdue_grace_minutes", nullable = false)
    private Integer overdueGraceMinutes;

    @ColumnDefault("0")
    @Column(name = "minimum_minutes", nullable = false)
    private Integer minimumMinutes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;

    @ColumnDefault("now()")
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ColumnDefault("now()")
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;


}