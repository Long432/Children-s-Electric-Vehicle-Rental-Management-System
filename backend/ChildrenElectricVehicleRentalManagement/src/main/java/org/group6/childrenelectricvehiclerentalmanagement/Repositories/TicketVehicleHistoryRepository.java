package org.group6.childrenelectricvehiclerentalmanagement.Repositories;

import org.group6.childrenelectricvehiclerentalmanagement.Models.TicketVehicleHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TicketVehicleHistoryRepository extends JpaRepository<TicketVehicleHistory, Long> {

    Optional<TicketVehicleHistory> findFirstByTicketIdAndVehicleIdAndReleasedAtIsNullOrderByIdDesc(Long ticketId, Long vehicleId);
}
