package org.group6.childrenelectricvehiclerentalmanagement.Controllers;

import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Long.Requests;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Long.Responses;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.*;
import org.group6.childrenelectricvehiclerentalmanagement.Models.User;
import org.group6.childrenelectricvehiclerentalmanagement.Services.TicketService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tickets")
public class TicketController extends BaseController {
    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping
    public ResponseEntity<Object> createTicket(
            @AuthenticationPrincipal User actor,
            @RequestBody(required = false) Requests.CreateTicketRequest body) throws Exception {
            Requests.CreateTicketRequest request = body != null ? body : new Requests.CreateTicketRequest();
            request.setActorId(actor.getId());
            return ResponseEntity.ok(ticketService.createTicket(request));
    }

    @GetMapping
    public ResponseEntity<Object> listTickets(
            @AuthenticationPrincipal User actor,
            @RequestParam(name = "session_id", required = false) String sessionId,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "page", required = false) String page,
            @RequestParam(name = "limit", required = false) String limit) throws Exception {

            Requests.ListTicketsRequest request = new Requests.ListTicketsRequest();
            request.setActorId(actor.getId());
            request.setSessionId(sessionId);
            request.setStatus(status);
            request.setPage(page);
            request.setLimit(limit);
            return ResponseEntity.ok(ticketService.listTickets(request));
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchTickets(
            @AuthenticationPrincipal User actor,
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "session_id", required = false) String sessionId) throws Exception {

            Requests.SearchTicketsRequest request = new Requests.SearchTicketsRequest();
            request.setActorId(actor.getId());
            request.setQ(q);
            request.setSessionId(sessionId);
            return ResponseEntity.ok(ticketService.searchTickets(request));
    }

    @GetMapping("/queue")
    public ResponseEntity<Object> getQueue(@AuthenticationPrincipal User actor) throws Exception {

            Requests.BaseRequest request = new Requests.BaseRequest();
            request.setActorId(actor.getId());
            return ResponseEntity.ok(ticketService.getQueue(request));
    }

    @GetMapping("/overdue")
    public ResponseEntity<Object> getOverdue(@AuthenticationPrincipal User actor) throws Exception {

            Requests.BaseRequest request = new Requests.BaseRequest();
            request.setActorId(actor.getId());
            return ResponseEntity.ok(ticketService.getOverdue(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getTicket(
            @AuthenticationPrincipal User actor,
            @PathVariable("id") String id) throws Exception {

            Requests.TicketIdRequest request = new Requests.TicketIdRequest();
            request.setActorId(actor.getId());
            request.setId(id);
            return ResponseEntity.ok(ticketService.getTicket(request));
    }

    @PostMapping("/{id}/assign")
    public ResponseEntity<Object> assignVehicle(
            @AuthenticationPrincipal User actor,
            @PathVariable("id") String id,
            @RequestBody(required = false) Requests.AssignVehicleRequest body) throws Exception {

            Requests.AssignVehicleRequest request = body != null ? body : new Requests.AssignVehicleRequest();
            request.setActorId(actor.getId());
            request.setId(id);
            return ResponseEntity.ok(ticketService.assignVehicle(request));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Object> cancelTicket(
            @AuthenticationPrincipal User actor,
            @PathVariable("id") String id,
            @RequestBody(required = false) Requests.CancelTicketRequest body) throws Exception {

            Requests.CancelTicketRequest request = body != null ? body : new Requests.CancelTicketRequest();
            request.setActorId(actor.getId());
            request.setId(id);
            return ResponseEntity.ok(ticketService.cancelTicket(request));
    }

    @PostMapping("/{id}/change-vehicle")
    public ResponseEntity<Object> changeVehicle(
            @AuthenticationPrincipal User actor,
            @PathVariable("id") String id,
            @RequestBody(required = false) Requests.ChangeVehicleRequest body) throws Exception {

            Requests.ChangeVehicleRequest request = body != null ? body : new Requests.ChangeVehicleRequest();
            request.setActorId(actor.getId());
            request.setId(id);
            return ResponseEntity.ok(ticketService.changeVehicle(request));
    }

    @PostMapping("/{id}/extend")
    public ResponseEntity<Object> extendTicket(
            @AuthenticationPrincipal User actor,
            @PathVariable("id") String id,
            @RequestBody(required = false) Requests.ExtendTicketRequest body) throws Exception {

            Requests.ExtendTicketRequest request = body != null ? body : new Requests.ExtendTicketRequest();
            request.setActorId(actor.getId());
            request.setId(id);
            return ResponseEntity.ok(ticketService.extendTicket(request));
    }

    @PostMapping("/{id}/return")
    public ResponseEntity<Object> returnTicket(
            @AuthenticationPrincipal User actor,
            @PathVariable("id") String id,
            @RequestBody(required = false) Requests.ReturnTicketRequest body) throws Exception {

            Requests.ReturnTicketRequest request = body != null ? body : new Requests.ReturnTicketRequest();
            request.setActorId(actor.getId());
            request.setId(id);
            return ResponseEntity.ok(ticketService.returnTicket(request));
    }
}
