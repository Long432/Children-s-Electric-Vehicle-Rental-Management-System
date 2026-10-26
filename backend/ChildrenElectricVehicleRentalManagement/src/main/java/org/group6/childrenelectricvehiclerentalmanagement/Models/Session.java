package org.group6.childrenelectricvehiclerentalmanagement.Models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.SessionStatus;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@Entity
@Table(name = "sessions")
public class Session {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ColumnDefault("'Waiting'")
    @Column(name = "status", columnDefinition = "session_status not null")
    private SessionStatus status;

    @Column(name = "scheduled_date", nullable = false)
    private LocalDate scheduledDate;

    @Column(name = "scheduled_time", nullable = false)
    private LocalTime scheduledTime;

    @Column(name = "planned_end_at")
    private LocalDateTime plannedEndAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @ColumnDefault("false")
    @Column(name = "is_instant", nullable = false)
    private Boolean isInstant;

    @ColumnDefault("1")
    @Column(name = "next_ticket_no", nullable = false)
    private Integer nextTicketNo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @ColumnDefault("now()")
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ColumnDefault("now()")
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;


}