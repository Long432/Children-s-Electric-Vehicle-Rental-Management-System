package org.group6.childrenelectricvehiclerentalmanagement.Repositories;

import org.group6.childrenelectricvehiclerentalmanagement.Models.Ticket;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Ticket t where t.id = :id")
    Optional<Ticket> lockById(@Param("id") Long id);

    Page<Ticket> findBySessionId(Long sessionId, Pageable pageable);

    Page<Ticket> findBySessionIdAndStatus(Long sessionId, TicketStatus status, Pageable pageable);

    List<Ticket> findBySessionId(Long sessionId);

    List<Ticket> findBySessionIdAndPhoneContainingOrderByIdDesc(Long sessionId, String phone);

    List<Ticket> findBySessionIdAndTicketNo(Long sessionId, Integer ticketNo);

    List<Ticket> findBySessionIdAndStatusOrderByQueuedAtAscIdAsc(Long sessionId, TicketStatus status);

    List<Ticket> findBySessionIdAndStatusOrderByDueAtAsc(Long sessionId, TicketStatus status);

    List<Ticket> findBySessionIdAndStatusAndDueAtBefore(Long sessionId, TicketStatus status, LocalDateTime time);

    Optional<Ticket> findFirstBySessionIdAndStatusOrderByQueuedAtAscIdAsc(Long sessionId, TicketStatus status);

    List<Ticket> findBySession_IdAndStatus(Long sessionId, TicketStatus status);

    List<Ticket> findBySession_IdAndStatusIn(Long sessionId, Collection<TicketStatus> statuses);

    boolean existsBySession_IdAndStatusIn(Long sessionId, Collection<TicketStatus> statuses);
}
