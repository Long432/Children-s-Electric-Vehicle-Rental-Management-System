package org.group6.childrenelectricvehiclerentalmanagement.Repositories;

import org.group6.childrenelectricvehiclerentalmanagement.Enum.TicketStatus;
import org.group6.childrenelectricvehiclerentalmanagement.Models.Ticket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Ticket t where t.id = :id")
    Optional<Ticket> findByIdForUpdate(@Param("id") long id);

    @EntityGraph(attributePaths = "vehicle")
    @Query("select t from Ticket t where t.id = :id")
    Optional<Ticket> findDetailById(@Param("id") long id);

    @EntityGraph(attributePaths = "vehicle")
    Page<Ticket> findBySessionIdOrderByQueuedAtDescIdDesc(Long sessionId, Pageable pageable);

    @EntityGraph(attributePaths = "vehicle")
    Page<Ticket> findBySessionIdAndStatusOrderByQueuedAtDescIdDesc(Long sessionId, TicketStatus status, Pageable pageable);

    List<Ticket> findBySessionIdAndStatusOrderByQueuedAtAscIdAsc(Long sessionId, TicketStatus status);

    @EntityGraph(attributePaths = "vehicle")
    @Query("select t from Ticket t where t.sessionId = :sessionId and (t.status = :overdue or (t.status = :inused and t.dueAt < :now)) order by t.dueAt asc, t.id asc")
    List<Ticket> findOverdueTickets(@Param("sessionId") Long sessionId,
                                    @Param("overdue") TicketStatus overdue,
                                    @Param("inused") TicketStatus inused,
                                    @Param("now") LocalDateTime now);

    Optional<Ticket> findFirstBySessionIdAndStatusOrderByQueuedAtAscIdAsc(Long sessionId, TicketStatus status);

    @EntityGraph(attributePaths = "vehicle")
    List<Ticket> findBySessionIdAndTicketNo(Long sessionId, Integer ticketNo);

    @EntityGraph(attributePaths = "vehicle")
    @Query("select t from Ticket t where t.sessionId = :sessionId and t.phone like concat('%', :phone, '%') order by t.ticketNo desc")
    List<Ticket> searchByPhone(@Param("sessionId") Long sessionId, @Param("phone") String phone, Pageable pageable);

    long countBySessionIdAndStatusIn(Long sessionId, Collection<TicketStatus> statuses);

    @Query("select t.status, count(t) from Ticket t where t.sessionId = :sessionId group by t.status")
    List<Object[]> countGroupedByStatus(@Param("sessionId") Long sessionId);
}
