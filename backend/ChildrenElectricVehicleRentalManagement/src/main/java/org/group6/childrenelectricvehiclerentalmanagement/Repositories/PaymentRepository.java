package org.group6.childrenelectricvehiclerentalmanagement.Repositories;

import org.group6.childrenelectricvehiclerentalmanagement.DTOs.PackageController.PackageSale;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.PaymentType;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.TicketStatus;
import org.group6.childrenelectricvehiclerentalmanagement.Models.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Payment[] findBySession_Id(Long sessionId);

    List<Payment> findByTicketIdOrderByIdAsc(Long ticketId);

    @Query("select new org.group6.childrenelectricvehiclerentalmanagement.DTOs.PackageController.PackageSale(t.queuedAt, p.amount) "
            + "from Payment p join p.ticket t "
            + "where p.type = :type and t.pricePackage.id = :packageId and t.status <> :excluded "
            + "and t.queuedAt >= :rangeStart and t.queuedAt < :rangeEnd "
            + "order by t.queuedAt")
    List<PackageSale> findPackageSales(@Param("packageId") Long packageId,
                                       @Param("type") PaymentType type,
                                       @Param("excluded") TicketStatus excluded,
                                       @Param("rangeStart") LocalDateTime rangeStart,
                                       @Param("rangeEnd") LocalDateTime rangeEnd);
}
