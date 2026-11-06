package org.group6.childrenelectricvehiclerentalmanagement.Repositories;

import org.group6.childrenelectricvehiclerentalmanagement.Models.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @Query("select p.type, p.method, sum(p.amount) from Payment p where p.sessionId = :sessionId group by p.type, p.method")
    List<Object[]> sumGroupedByTypeAndMethod(@Param("sessionId") Long sessionId);
}
