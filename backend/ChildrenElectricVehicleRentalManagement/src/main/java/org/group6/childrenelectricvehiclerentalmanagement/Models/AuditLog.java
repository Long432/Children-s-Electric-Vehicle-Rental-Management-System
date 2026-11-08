package org.group6.childrenelectricvehiclerentalmanagement.Models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "audit_logs")
public class AuditLog extends Timestamped {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "action", nullable = false, length = Integer.MAX_VALUE)
    private String action;

    @Column(name = "entity_type", nullable = false, length = Integer.MAX_VALUE)
    private String entityType;

    @Column(name = "entity_id")
    private Long entityId;

    @Column(name = "payload",length = 600)
    private String payload;
}
