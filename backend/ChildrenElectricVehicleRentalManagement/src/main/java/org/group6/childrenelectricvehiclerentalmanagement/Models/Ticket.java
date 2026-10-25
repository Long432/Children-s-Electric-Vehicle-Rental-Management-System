package org.group6.childrenelectricvehiclerentalmanagement.Models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.TicketStatus;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.PaymentMethod;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "tickets")
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "ticket_no", nullable = false)
    private Integer ticketNo;

    @Column(name = "phone", nullable = false, length = Integer.MAX_VALUE)
    private String phone;

    @ColumnDefault("'Waiting'")
    @Column(name = "status", columnDefinition = "ticket_status not null")
    private TicketStatus status;

    @Column(name = "payment_method", columnDefinition = "payment_method not null")
    private PaymentMethod paymentMethod;

    @Column(name = "total_minutes", nullable = false)
    private Integer totalMinutes;

    @Column(name = "amount_paid", nullable = false)
    private Integer amountPaid;

    @Column(name = "price_amount", nullable = false)
    private Integer priceAmount;

    @Column(name = "price_minutes", nullable = false)
    private Integer priceMinutes;

    @Column(name = "overdue_fee_per_minute", nullable = false)
    private Integer overdueFeePerMinute;

    @Column(name = "overdue_grace_minutes", nullable = false)
    private Integer overdueGraceMinutes;

    @Column(name = "refund_enabled", nullable = false)
    private Boolean refundEnabled;

    @Column(name = "queued_at", nullable = false)
    private LocalDateTime queuedAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "due_at")
    private LocalDateTime dueAt;

    @Column(name = "returned_at")
    private LocalDateTime returnedAt;

    @ColumnDefault("0")
    @Column(name = "overdue_minutes", nullable = false)
    private Integer overdueMinutes;

    @ColumnDefault("0")
    @Column(name = "overdue_fee", nullable = false)
    private Integer overdueFee;

    @ColumnDefault("0")
    @Column(name = "refund_amount", nullable = false)
    private Integer refundAmount;

    @Column(name = "canceled_at")
    private LocalDateTime canceledAt;

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