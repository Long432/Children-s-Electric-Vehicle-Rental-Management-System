package org.group6.childrenelectricvehiclerentalmanagement.Models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "settings")
public class Settings extends Timestamped {
    @Id
    @Column(name = "id", nullable = false)
    private Short id;

    @Column(name = "refund_enabled", nullable = false)
    private Boolean refundEnabled;

    @Column(name = "extend_enabled", nullable = false)
    private Boolean extendEnabled;

    @Column(name = "overdue_fee_per_minute", nullable = false)
    private Integer overdueFeePerMinute;

    @Column(name = "overdue_grace_minutes", nullable = false)
    private Integer overdueGraceMinutes;

    @Column(name = "minimum_minutes", nullable = false)
    private Integer minimumMinutes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;
}
