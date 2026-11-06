package org.group6.childrenelectricvehiclerentalmanagement.Repositories;

import jakarta.persistence.LockModeType;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.VehicleStatus;
import org.group6.childrenelectricvehiclerentalmanagement.Models.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    boolean existsBySlugAndDeletedAtIsNull(String slug);

    List<Vehicle> findByStatusOrderByIdAsc(VehicleStatus status);

    List<Vehicle> findByStatusNotOrderByIdAsc(VehicleStatus status);

    @Query(value = "select v.* from vehicles v where v.status <> 'Deleted' and exists "
            + "(select 1 from session_vehicles sv where sv.vehicle_id = v.id and sv.session_id = :sessionId and sv.removed_at is null) "
            + "order by v.id", nativeQuery = true)
    List<Vehicle> findActiveBySessionId(@Param("sessionId") long sessionId);

    @Query(value = "select v.* from vehicles v where v.status = :status and exists "
            + "(select 1 from session_vehicles sv where sv.vehicle_id = v.id and sv.session_id = :sessionId and sv.removed_at is null) "
            + "order by v.id", nativeQuery = true)
    List<Vehicle> findByStatusAndSessionId(@Param("status") String status, @Param("sessionId") long sessionId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select v from Vehicle v where v.id = :id")
    Optional<Vehicle> findByIdForUpdate(@Param("id") long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select v from Vehicle v where v.slug in :slugs and v.deletedAt is null order by v.id")
    List<Vehicle> findActiveBySlugsForUpdate(@Param("slugs") Collection<String> slugs);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select v from Vehicle v where v.status <> :excluded and v.id in "
            + "(select sv.vehicleId from SessionVehicle sv where sv.sessionId = :sessionId and sv.removedAt is null) "
            + "order by v.id")
    List<Vehicle> findActiveBySessionIdForUpdate(@Param("sessionId") long sessionId, @Param("excluded") VehicleStatus excluded);
}
