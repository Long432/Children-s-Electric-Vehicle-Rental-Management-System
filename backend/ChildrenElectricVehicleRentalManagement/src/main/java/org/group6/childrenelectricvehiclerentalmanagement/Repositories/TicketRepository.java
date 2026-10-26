package org.group6.childrenelectricvehiclerentalmanagement.Repositories;

import org.group6.childrenelectricvehiclerentalmanagement.Models.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

interface TicketRepository extends JpaRepository<Ticket, Long> {
}
