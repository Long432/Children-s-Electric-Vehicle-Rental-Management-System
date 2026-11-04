package org.group6.childrenelectricvehiclerentalmanagement.Models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.PaymentType;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.PaymentMethod;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "payments")
public class Payment extends Timestamped {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @Column(name = "type", nullable = false)
    private PaymentType type;

    @Column(name = "method", nullable = false)
    private PaymentMethod method;

    @Column(name = "amount", nullable = false)
    private Integer amount;

    @Column(name = "minutes")
    private Integer minutes;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    public Payment(Session session, Ticket ticket, User createdBy) {
        this.session = session;
        this.ticket = ticket;
        this.type = PaymentType.Ticket;
        this.method = ticket.getPaymentMethod();
        this.createdBy = createdBy;
    }
    public Payment(Session session, Ticket ticket, PaymentMethod method, User createdBy) {
        this.session = session;
        this.ticket = ticket;
        this.type = PaymentType.Ticket;
        this.method = method;
        this.createdBy = createdBy;
    }

    public Payment() {

    }
}
