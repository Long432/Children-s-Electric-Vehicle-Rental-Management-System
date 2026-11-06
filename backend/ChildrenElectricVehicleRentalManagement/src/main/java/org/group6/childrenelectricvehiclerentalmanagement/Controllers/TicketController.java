package org.group6.childrenelectricvehiclerentalmanagement.Controllers;

import org.group6.childrenelectricvehiclerentalmanagement.DTOs.ApiResponse;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Ticket.AssignTicketRequest;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Ticket.CancelTicketRequest;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Ticket.ChangeVehicleRequest;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Ticket.CreateTicketRequest;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Ticket.ExtendTicketRequest;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Ticket.ReturnTicketRequest;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Ticket.TicketListQuery;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Ticket.TicketSearchQuery;
import org.group6.childrenelectricvehiclerentalmanagement.Services.TicketActionService;
import org.group6.childrenelectricvehiclerentalmanagement.Services.TicketService;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.Security.AuthenticatedUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tickets")
public class TicketController extends BaseController {

    private final TicketService ticketService;
    private final TicketActionService ticketActionService;

    public TicketController(TicketService ticketService, TicketActionService ticketActionService) {
        this.ticketService = ticketService;
        this.ticketActionService = ticketActionService;
    }

    @PostMapping({"", "/"})
    public ResponseEntity<ApiResponse> create(@AuthenticationPrincipal AuthenticatedUser actor, @RequestBody(required = false) CreateTicketRequest request) throws Exception {
        return ResponseEntity.ok(ApiResponse.success(ticketService.create(actor, request)));
    }

    @GetMapping({"", "/"})
    public ResponseEntity<ApiResponse> list(@RequestParam(required = false) String status,
                                            @RequestParam(name = "session_id", required = false) Long sessionId,
                                            @RequestParam(required = false) Integer page,
                                            @RequestParam(required = false) Integer limit) throws Exception {
        return ResponseEntity.ok(ApiResponse.success(ticketService.list(new TicketListQuery(status, sessionId, page, limit))));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse> search(@RequestParam(required = false) String q,
                                              @RequestParam(name = "session_id", required = false) Long sessionId) throws Exception {
        return ResponseEntity.ok(ApiResponse.success(ticketService.search(new TicketSearchQuery(q, sessionId))));
    }

    @GetMapping("/queue")
    public ResponseEntity<ApiResponse> queue() {
        return ResponseEntity.ok(ApiResponse.success(ticketService.queue()));
    }

    @GetMapping("/overdue")
    public ResponseEntity<ApiResponse> overdue() {
        return ResponseEntity.ok(ApiResponse.success(ticketService.overdue()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> get(@PathVariable long id) throws Exception {
        return ResponseEntity.ok(ApiResponse.success(ticketService.get(id)));
    }

    @PostMapping("/{id}/assign")
    public ResponseEntity<ApiResponse> assign(@AuthenticationPrincipal AuthenticatedUser actor,
                                              @PathVariable long id,
                                              @RequestBody(required = false) AssignTicketRequest request) throws Exception {
        return ResponseEntity.ok(ApiResponse.success(ticketActionService.assign(actor, id, request)));
    }

    @PostMapping("/{id}/change-vehicle")
    public ResponseEntity<ApiResponse> changeVehicle(@AuthenticationPrincipal AuthenticatedUser actor,
                                                     @PathVariable long id,
                                                     @RequestBody(required = false) ChangeVehicleRequest request) throws Exception {
        return ResponseEntity.ok(ApiResponse.success(ticketActionService.changeVehicle(actor, id, request)));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse> cancel(@AuthenticationPrincipal AuthenticatedUser actor,
                                              @PathVariable long id,
                                              @RequestBody(required = false) CancelTicketRequest request) throws Exception {
        return ResponseEntity.ok(ApiResponse.success(ticketActionService.cancel(actor, id, request)));
    }

    @PostMapping("/{id}/extend")
    public ResponseEntity<ApiResponse> extend(@AuthenticationPrincipal AuthenticatedUser actor,
                                              @PathVariable long id,
                                              @RequestBody(required = false) ExtendTicketRequest request) throws Exception {
        return ResponseEntity.ok(ApiResponse.success(ticketActionService.extend(actor, id, request)));
    }

    @PostMapping("/{id}/return")
    public ResponseEntity<ApiResponse> returnVehicle(@AuthenticationPrincipal AuthenticatedUser actor,
                                                     @PathVariable long id,
                                                     @RequestBody(required = false) ReturnTicketRequest request) throws Exception {
        return ResponseEntity.ok(ApiResponse.success(ticketActionService.returnVehicle(actor, id, request)));
    }
}
