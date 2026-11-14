package org.group6.childrenelectricvehiclerentalmanagement.Models;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

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

    @Column(name = "cancel_wait_minutes", nullable = false)
    private Integer cancelWaitMinutes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;
}
