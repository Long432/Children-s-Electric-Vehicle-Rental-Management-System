package org.group6.childrenelectricvehiclerentalmanagement.integration;

import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Long.Requests;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.SettingsController.ToggleRequest;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.PaymentMethod;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.TicketStatus;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.VehicleStatus;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.TicketNotFound;
import org.group6.childrenelectricvehiclerentalmanagement.Models.Ticket;
import org.group6.childrenelectricvehiclerentalmanagement.Models.Vehicle;
import org.group6.childrenelectricvehiclerentalmanagement.Services.TicketService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test cho TicketService với H2 in-memory database.
 * Luồng kiểm tra: tạo vé → gán xe → hủy → đổi xe → gia hạn → trả xe → queue/overdue.
 */
class TicketIntegrationTest extends AbstractIntegrationTest {

    @Autowired private TicketService ticketService;

    @Test
    void createTicket_success() throws Exception {
        Requests.CreateTicketRequest req = new Requests.CreateTicketRequest();
        req.setActorId(seller.getId());
        req.setPhone("0901234567");
        req.setMinutes("30");
        req.setPaymentMethod("Cash");

        var res = ticketService.createTicket(req);

        assertNotNull(res);
        assertEquals(1, res.ticket().ticketNo());
        assertEquals(TicketStatus.Waiting, TicketStatus.valueOf(res.ticket().status()));
        assertEquals("0901234567", res.ticket().phone());
        assertEquals(15000, res.ticket().amountPaid());
    }

    @Test
    void assignVehicle_toWaitingTicket_shouldBeInused() throws Exception {
        var ticketRes = ticketService.createTicket(newRequest("0901234567", "30", "Cash"));

        Requests.AssignVehicleRequest req = new Requests.AssignVehicleRequest();
        req.setActorId(seller.getId());
        req.setId(String.valueOf(ticketRes.ticket().id()));
        req.setVehicleSlug("xe-01");

        var res = ticketService.assignVehicle(req);

        assertNotNull(res);
        assertEquals(TicketStatus.Inused, TicketStatus.valueOf(res.status()));
        assertEquals("xe-01", res.vehicle().slug());
        assertEquals(VehicleStatus.Inused, vehicle1.getStatus());
    }

    @Test
    void changeVehicle_forInusedTicket_shouldSwitch() throws Exception {
        // Tạo xe mới trong cùng session
        Vehicle newVeh = createVehicle("xe-02", VehicleStatus.Online);
        org.group6.childrenelectricvehiclerentalmanagement.Models.LinkingObject.SessionVehicle sv =
                new org.group6.childrenelectricvehiclerentalmanagement.Models.LinkingObject.SessionVehicle();
        sv.setSession(session1);
        sv.setVehicle(newVeh);
        sv.setAddedAt(java.time.LocalDateTime.now());
        sessionVehicleRepository.save(sv);

        var ticketRes = ticketService.createTicket(newRequest("0901234567", "30", "Cash"));
        ticketService.assignVehicle(newAssignRequest(ticketRes.ticket().id(), "xe-01"));

        Requests.ChangeVehicleRequest req = new Requests.ChangeVehicleRequest();
        req.setActorId(seller.getId());
        req.setId(String.valueOf(ticketRes.ticket().id()));
        req.setVehicleSlug("xe-02");
        req.setOldVehicleStatus("Online");

        var res = ticketService.changeVehicle(req);

        assertNotNull(res);
        assertEquals("xe-02", res.ticket().vehicle().slug());
    }

    @Test
    void extendTicket_whenEnabled_shouldAddMinutes() throws Exception {
        var ticketRes = ticketService.createTicket(newRequest("0901234567", "30", "Cash"));
        ticketService.assignVehicle(newAssignRequest(ticketRes.ticket().id(), "xe-01"));

        Requests.ExtendTicketRequest req = new Requests.ExtendTicketRequest();
        req.setActorId(seller.getId());
        req.setId(String.valueOf(ticketRes.ticket().id()));
        req.setMinutes("30");
        req.setPaymentMethod("Cash");

        var res = ticketService.extendTicket(req);

        assertNotNull(res);
        assertEquals(60, res.ticket().totalMinutes());
    }

    @Test
    void returnTicket_overdue_shouldChargeFee() throws Exception {
        var ticketRes = ticketService.createTicket(newRequest("0901234567", "30", "Cash"));
        ticketService.assignVehicle(newAssignRequest(ticketRes.ticket().id(), "xe-01"));

        // Đặt dueAt vào quá khứ để tạo overdue
        Ticket overdueTicket = ticketRepository.findById(ticketRes.ticket().id()).orElseThrow();
        overdueTicket.setDueAt(java.time.LocalDateTime.now().minusMinutes(12));
        ticketRepository.save(overdueTicket);

        Requests.ReturnTicketRequest req = new Requests.ReturnTicketRequest();
        req.setActorId(seller.getId());
        req.setId(String.valueOf(ticketRes.ticket().id()));

        var res = ticketService.returnTicket(req);

        assertNotNull(res);
        assertEquals(TicketStatus.Completed, TicketStatus.valueOf(res.status()));
        assertTrue(res.overdueMinutes() > 0);
        assertTrue(res.overdueFee() > 0);
        assertEquals(VehicleStatus.Online, vehicle1.getStatus());
    }

    @Test
    void returnTicket_earlyWithRefund_shouldRefund() throws Exception {
        ToggleRequest toggleReq = new ToggleRequest();
        toggleReq.setEnabled(true);
        settingsService.toggleRefund(seller, toggleReq);

        var ticketRes = ticketService.createTicket(newRequest("0901234567", "60", "Cash"));
        ticketService.assignVehicle(newAssignRequest(ticketRes.ticket().id(), "xe-01"));

        // Trả sớm: dueAt ở tương lai
        Ticket t = ticketRepository.findById(ticketRes.ticket().id()).orElseThrow();
        t.setDueAt(java.time.LocalDateTime.now().plusMinutes(30)); // còn 30 phút
        ticketRepository.save(t);

        Requests.ReturnTicketRequest req = new Requests.ReturnTicketRequest();
        req.setActorId(seller.getId());
        req.setId(String.valueOf(ticketRes.ticket().id()));

        var res = ticketService.returnTicket(req);

        assertNotNull(res);
        assertEquals(TicketStatus.Completed, TicketStatus.valueOf(res.status()));
    }

    @Test
    void getQueue_shouldReturnWaitingTickets() throws Exception {
        ticketService.createTicket(newRequest("0901234567", "30", "Cash"));
        ticketService.createTicket(newRequest("0901234568", "30", "Cash"));

        Requests.BaseRequest req = new Requests.BaseRequest();
        req.setActorId(seller.getId());

        var res = ticketService.getQueue(req);

        assertNotNull(res);
        assertEquals(2, res.items().size());
    }

    @Test
    void getOverdue_shouldReturnOverdueTickets() throws Exception {
        var ticketRes = ticketService.createTicket(newRequest("0901234567", "30", "Cash"));
        ticketService.assignVehicle(newAssignRequest(ticketRes.ticket().id(), "xe-01"));

        Ticket t = ticketRepository.findById(ticketRes.ticket().id()).orElseThrow();
        t.setStatus(TicketStatus.Overdue);
        t.setDueAt(java.time.LocalDateTime.now().minusMinutes(10));
        ticketRepository.save(t);

        Requests.BaseRequest req = new Requests.BaseRequest();
        req.setActorId(seller.getId());

        var res = ticketService.getOverdue(req);

        assertNotNull(res);
        assertEquals(1, res.items().size());
    }

    // ---------- Helpers ----------

    private Requests.CreateTicketRequest newRequest(String phone, String minutes, String method) {
        Requests.CreateTicketRequest req = new Requests.CreateTicketRequest();
        req.setActorId(seller.getId());
        req.setPhone(phone);
        req.setMinutes(minutes);
        req.setPaymentMethod(method);
        return req;
    }

    private Requests.AssignVehicleRequest newAssignRequest(Long ticketId, String slug) {
        Requests.AssignVehicleRequest req = new Requests.AssignVehicleRequest();
        req.setActorId(seller.getId());
        req.setId(String.valueOf(ticketId));
        req.setVehicleSlug(slug);
        return req;
    }
}
