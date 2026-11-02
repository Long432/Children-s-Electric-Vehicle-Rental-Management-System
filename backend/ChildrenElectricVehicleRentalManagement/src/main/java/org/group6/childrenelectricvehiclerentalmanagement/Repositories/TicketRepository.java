package org.group6.childrenelectricvehiclerentalmanagement.Repositories;

import org.group6.childrenelectricvehiclerentalmanagement.Models.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    @Query(value = """
    SELECT * FROM tickets
    WHERE session_id = :sessionId
    AND status = 'Waiting'
    ORDER BY queued_at ASC
    """,nativeQuery = true)
    List<Ticket> getInUsedTicketsWSessionId(Long sessionId);
}
