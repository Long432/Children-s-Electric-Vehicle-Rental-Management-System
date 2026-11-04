package org.group6.childrenelectricvehiclerentalmanagement.Repositories;

import org.group6.childrenelectricvehiclerentalmanagement.Models.LinkingObject.SessionVehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SessionVehicleRepository extends JpaRepository<SessionVehicle, Long> {
    boolean existsBySessionIdAndVehicleIdAndRemovedAtIsNull(Long sessionId, Long vehicleId);

    List<SessionVehicle> findBySessionIdAndRemovedAtIsNull(Long sessionId);
}
