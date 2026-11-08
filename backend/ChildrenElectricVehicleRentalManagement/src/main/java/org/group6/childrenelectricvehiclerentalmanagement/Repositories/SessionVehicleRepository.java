package org.group6.childrenelectricvehiclerentalmanagement.Repositories;

import org.group6.childrenelectricvehiclerentalmanagement.Enum.SessionStatus;
import org.group6.childrenelectricvehiclerentalmanagement.Models.LinkingObject.SessionVehicle;
import org.group6.childrenelectricvehiclerentalmanagement.Models.Vehicle;
import org.springframework.cglib.core.CollectionUtils;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SessionVehicleRepository extends JpaRepository<SessionVehicle, Long> {
    boolean existsBySessionIdAndVehicleIdAndRemovedAtIsNull(Long sessionId, Long vehicleId);

    List<SessionVehicle> findBySessionIdAndRemovedAtIsNull(Long sessionId);

    List<SessionVehicle> findAllBySession_Id(Long sessionId);

    List<SessionVehicle> findAllByRemovedAtIsNullAndSession_Id(Long sessionId);

    boolean existsByVehicle_IdAndRemovedAtIsNullAndSession_Status(Long vehicleId, SessionStatus sessionStatus);

    Optional<SessionVehicle> findBySessionIdAndVehicleId(Long sessionId, Long vehicleId);
}
