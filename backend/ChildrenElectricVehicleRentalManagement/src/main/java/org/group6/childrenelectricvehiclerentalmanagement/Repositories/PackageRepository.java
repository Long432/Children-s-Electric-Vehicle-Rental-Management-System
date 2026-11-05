package org.group6.childrenelectricvehiclerentalmanagement.Repositories;

import jakarta.persistence.LockModeType;
import org.group6.childrenelectricvehiclerentalmanagement.Models.Package;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PackageRepository extends JpaRepository<Package, Long> {
    List<Package> findByActiveTrueOrderByMinutesAsc();

    List<Package> findAllByOrderByMinutesAsc();

    Optional<Package> findFirstByActiveTrueOrderByMinutesAsc();

    List<Package> findByIsDefaultTrueAndActiveTrue();

    boolean existsByMinutes(Integer minutes);

    boolean existsByMinutesAndIdNot(Integer minutes, Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Package p where p.id = :id")
    Optional<Package> lockById(@Param("id") Long id);
}
