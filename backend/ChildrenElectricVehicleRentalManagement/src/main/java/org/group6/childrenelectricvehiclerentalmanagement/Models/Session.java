package org.group6.childrenelectricvehiclerentalmanagement.Models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.SessionStatus;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@Entity
@Table(name = "sessions")
public class Session extends Timestamped {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "status", nullable = false)
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

    public Session(LocalDateTime plannedStartAT, LocalDateTime plannedEndAt, User createdBy) {
        this.status=SessionStatus.Waiting;
        this.scheduledDate = plannedStartAT.toLocalDate();
        this.scheduledTime = plannedStartAT.toLocalTime();
        this.plannedEndAt = plannedEndAt;
        this.isInstant = false;
        this.nextTicketNo=1;
        this.createdBy = createdBy;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Session(LocalDateTime plannedEndAt, User createdBy) {
        this.status=SessionStatus.InProgress;
        this.scheduledDate = LocalDate.now();
        this.scheduledTime = LocalTime.now();
        this.plannedEndAt = plannedEndAt;
        this.isInstant = true;
        this.nextTicketNo=1;
        this.createdBy = createdBy;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Session() {

    }

    public void setStatus(SessionStatus status) {
        this.status = status;
        if (status == SessionStatus.Ended) this.endedAt=LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
        this.updatedAt = LocalDateTime.now();
    }

    public void setEndedAt(LocalDateTime endedAt) {
        this.endedAt = endedAt;
        this.updatedAt = LocalDateTime.now();
    }

    public void setNextTicketNo(Integer nextTicketNo) {
        this.nextTicketNo = nextTicketNo;
        this.updatedAt = LocalDateTime.now();
    }
}