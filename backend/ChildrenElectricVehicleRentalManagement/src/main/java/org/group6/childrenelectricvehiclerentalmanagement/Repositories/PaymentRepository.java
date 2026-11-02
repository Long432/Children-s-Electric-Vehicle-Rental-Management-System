package org.group6.childrenelectricvehiclerentalmanagement.Repositories;

import org.group6.childrenelectricvehiclerentalmanagement.Models.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Payment[] findBySession_Id(Long sessionId);

    Collection<Payment> findByIdOrderByIdAsc(Long id);
}
