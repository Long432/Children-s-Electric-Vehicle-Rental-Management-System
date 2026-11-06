package org.group6.childrenelectricvehiclerentalmanagement.Repositories;

import org.group6.childrenelectricvehiclerentalmanagement.Enum.SessionStatus;
import org.group6.childrenelectricvehiclerentalmanagement.Models.SessionVehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SessionVehicleRepository extends JpaRepository<SessionVehicle, Long> {

    boolean existsBySessionIdAndVehicleIdAndRemovedAtIsNull(Long sessionId, Long vehicleId);

    @Query("select count(sv) from SessionVehicle sv, Session s "
            + "where s.id = sv.sessionId and sv.vehicleId = :vehicleId and sv.removedAt is null and s.status = :status")
    long countActiveForVehicleInStatus(@Param("vehicleId") long vehicleId, @Param("status") SessionStatus status);
}
