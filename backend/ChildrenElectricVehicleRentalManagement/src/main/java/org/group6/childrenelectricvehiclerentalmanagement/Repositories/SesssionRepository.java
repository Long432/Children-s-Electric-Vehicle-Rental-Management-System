package org.group6.childrenelectricvehiclerentalmanagement.Repositories;

import org.group6.childrenelectricvehiclerentalmanagement.Enum.SessionStatus;
import org.group6.childrenelectricvehiclerentalmanagement.Models.Session;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SesssionRepository extends JpaRepository<Session, Long> {
    Session findTopByStatus(SessionStatus status);

    Optional<Session> findByStatus(SessionStatus status);

    List<Session> findTopByStatusOrderByCreatedAtDesc(SessionStatus status);

    List<Session> findAllByScheduledDateIs(LocalDate scheduledDate);

    List<Session> findAllByPlannedEndAt_Date(LocalDate plannedEndAtDate);

    Session findTopByScheduledDate(LocalDate scheduledDate, Sort sort);
}
