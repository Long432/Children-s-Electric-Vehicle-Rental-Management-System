package org.group6.childrenelectricvehiclerentalmanagement.Repositories;

import jakarta.persistence.LockModeType;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.SessionStatus;
import org.group6.childrenelectricvehiclerentalmanagement.Models.Session;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SesssionRepository extends JpaRepository<Session, Long> {

    boolean existsByStatus(SessionStatus status);

    Optional<Session> findFirstByStatus(SessionStatus status);

    Page<Session> findByStatus(SessionStatus status, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Session s where s.id = :id")
    Optional<Session> findByIdForUpdate(@Param("id") long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Session s where s.status = :status")
    Optional<Session> findByStatusForUpdate(@Param("status") SessionStatus status);
}
