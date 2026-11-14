package org.group6.childrenelectricvehiclerentalmanagement.Repositories;

import org.group6.childrenelectricvehiclerentalmanagement.Enum.SessionStatus;
import org.group6.childrenelectricvehiclerentalmanagement.Models.Session;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface SessionRepository extends JpaRepository<Session, Long> {
    Optional<Session> findFirstByStatusOrderByIdDesc(SessionStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Session s where s.status = :status order by s.id desc")
    List<Session> lockByStatus(@Param("status") SessionStatus status);

    boolean existsByStatus(SessionStatus status);

    Page<Session> findByStatus(SessionStatus status, Pageable pageable);

    List<Session> findAllByStatusAndPlannedEndAt(SessionStatus status, LocalDateTime plannedEndAt);

    List<Session> findAllByStatusAndPlannedEndAtAfter(SessionStatus status, LocalDateTime plannedEndAtAfter);

    List<Session> findAllByStatusInAndPlannedEndAtAfter(Collection<SessionStatus> statuses, LocalDateTime plannedEndAtAfter);

    Optional<Session> findByScheduledDateIsAndScheduledTimeIsAfter(LocalDate scheduledDate, LocalTime scheduledTimeAfter);
}
