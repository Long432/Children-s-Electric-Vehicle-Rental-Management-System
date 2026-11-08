package org.group6.childrenelectricvehiclerentalmanagement.Repositories;

import org.group6.childrenelectricvehiclerentalmanagement.Enum.SessionStatus;
import org.group6.childrenelectricvehiclerentalmanagement.Models.Session;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface SesssionRepository extends JpaRepository<Session, Long> {

    Optional<Session> findByStatus(SessionStatus status);

    List<Session> findAllByScheduledDateIs(LocalDate scheduledDate);


    Optional<Session> findFirstByStatusOrderByIdDesc(SessionStatus status);


    Collection<? extends Session> findAllByPlannedEndAtBetween(LocalDateTime plannedEndAtAfter, LocalDateTime plannedEndAtBefore);

    List<Session> findAllByStatusInAndPlannedEndAtAfter(Collection<SessionStatus> statuses, LocalDateTime time);

    boolean existsByStatus(SessionStatus status);

    Session findByStatusAndScheduledDateAfterAndPlannedEndAtBefore(SessionStatus status, LocalDate scheduledDateAfter, LocalDateTime plannedEndAtBefore);

    Optional<Session> findByStatusAndPlannedEndAtAfter(SessionStatus status, LocalDateTime plannedEndAtAfter);

    Session findFirstByStatusAndPlannedEndAtAfter(SessionStatus status, LocalDateTime plannedEndAtAfter);
}
