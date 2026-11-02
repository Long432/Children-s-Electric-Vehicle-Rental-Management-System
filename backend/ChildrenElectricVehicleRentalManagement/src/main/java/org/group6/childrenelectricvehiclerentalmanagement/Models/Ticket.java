package org.group6.childrenelectricvehiclerentalmanagement.Models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.TicketStatus;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.PaymentMethod;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "tickets")
public class Ticket extends Timestamped {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

    @Column(name = "ticket_no", nullable = false)
    private Integer ticketNo;

    @Column(name = "phone", nullable = false, length = Integer.MAX_VALUE)
    private String phone;

    @Column(name = "status", nullable = false)
    private TicketStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    @Column(name = "payment_method", nullable = false)
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

    @Column(name = "overdue_minutes", nullable = false)
    private Integer overdueMinutes = 0;

    @Column(name = "overdue_fee", nullable = false)
    private Integer overdueFee = 0;

    @Column(name = "refund_amount", nullable = false)
    private Integer refundAmount = 0;

    @Column(name = "canceled_at")
    private LocalDateTime canceledAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;
}
