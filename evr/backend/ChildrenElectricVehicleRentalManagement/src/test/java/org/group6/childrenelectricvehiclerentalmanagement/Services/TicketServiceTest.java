package org.group6.childrenelectricvehiclerentalmanagement.Services;

import org.group6.childrenelectricvehiclerentalmanagement.DTOs.PackageResponse;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.PageResponse;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.SettingsResponse;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Ticket.CreateTicketRequest;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Ticket.CreateTicketResponse;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Ticket.TicketListQuery;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Ticket.TicketOverdueResponse;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Ticket.TicketQueueResponse;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Ticket.TicketResponse;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Ticket.TicketSearchQuery;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.PaymentMethod;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.PaymentType;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.SessionStatus;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.TicketStatus;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.VehicleStatus;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.BelowMinimumTimeException;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.ConflictException;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.NoActiveSessionException;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.SessionNotExits;
import org.group6.childrenelectricvehiclerentalmanagement.Models.Payment;
import org.group6.childrenelectricvehiclerentalmanagement.Models.Session;
import org.group6.childrenelectricvehiclerentalmanagement.Models.Ticket;
import org.group6.childrenelectricvehiclerentalmanagement.Models.Vehicle;
import org.group6.childrenelectricvehiclerentalmanagement.Repositories.PaymentRepository;
import org.group6.childrenelectricvehiclerentalmanagement.Repositories.SesssionRepository;
import org.group6.childrenelectricvehiclerentalmanagement.Repositories.TicketRepository;
import org.group6.childrenelectricvehiclerentalmanagement.Repositories.UserRepository;
import org.group6.childrenelectricvehiclerentalmanagement.Repositories.VehicleRepository;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.BusinessTime;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.Security.AuthenticatedUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class TicketServiceTest {

    private static final Instant NOW = Instant.parse("2026-11-05T10:00:00Z");

    private final SesssionRepository sessionRepository = mock(SesssionRepository.class);
    private final TicketRepository ticketRepository = mock(TicketRepository.class);
    private final PaymentRepository paymentRepository = mock(PaymentRepository.class);
    private final VehicleRepository vehicleRepository = mock(VehicleRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final SettingsService settingsService = mock(SettingsService.class);
    private final AuditLogService auditLogService = mock(AuditLogService.class);
    private final BusinessTime businessTime = new BusinessTime(Clock.fixed(NOW, ZoneOffset.UTC), "Asia/Ho_Chi_Minh");
    private final AuthenticatedUser actor = new AuthenticatedUser(1L, "banhang");
    private TicketService service;

    @BeforeEach
    void setUp() {
        service = new TicketService(sessionRepository, ticketRepository, paymentRepository, vehicleRepository,
                userRepository, settingsService, auditLogService, businessTime);
        SettingsResponse settings = new SettingsResponse(true, true, 2000, 5, 15,
                List.of(new PackageResponse(1L, 30000, 15, false),
                        new PackageResponse(2L, 50000, 30, true),
                        new PackageResponse(3L, 90000, 60, false)),
                "2026-11-05T00:00:00Z");
        when(settingsService.getSettings()).thenReturn(settings);
        when(ticketRepository.saveAndFlush(any(Ticket.class))).thenAnswer(call -> {
            Ticket ticket = call.getArgument(0);
            ReflectionTestUtils.setField(ticket, "id", 10L);
            return ticket;
        });
    }

    private Session activeSession(int nextTicketNo) {
        Session session = new Session();
        session.setId(1L);
        session.setStatus(SessionStatus.InProgress);
        session.setScheduledDate(LocalDate.of(2026, 11, 5));
        session.setScheduledTime(LocalTime.of(9, 0));
        session.setIsInstant(false);
        session.setNextTicketNo(nextTicketNo);
        session.setCreatedAt(LocalDateTime.of(2026, 11, 5, 1, 0));
        session.setUpdatedAt(LocalDateTime.of(2026, 11, 5, 1, 0));
        return session;
    }

    private Ticket ticket(long id, int no, TicketStatus status) {
        LocalDateTime now = LocalDateTime.of(2026, 11, 5, 10, 0);
        Ticket ticket = new Ticket();
        ticket.setId(id);
        ticket.setSessionId(1L);
        ticket.setTicketNo(no);
        ticket.setPhone("0912345678");
        ticket.setStatus(status);
        ticket.setPaymentMethod(PaymentMethod.Cash);
        ticket.setTotalMinutes(30);
        ticket.setAmountPaid(50000);
        ticket.setPriceAmount(50000);
        ticket.setPriceMinutes(30);
        ticket.setOverdueFeePerMinute(2000);
        ticket.setOverdueGraceMinutes(5);
        ticket.setRefundEnabled(true);
        ticket.setQueuedAt(now);
        ticket.setOverdueMinutes(0);
        ticket.setOverdueFee(0);
        ticket.setRefundAmount(0);
        ticket.setCreatedAt(now);
        ticket.setUpdatedAt(now);
        return ticket;
    }

    @Test
    void createSnapshotsSettingsAndAdvancesTheCounter() throws Exception {
        Session session = activeSession(4);
        when(sessionRepository.findByStatusForUpdate(SessionStatus.InProgress)).thenReturn(Optional.of(session));
        Vehicle online = new Vehicle("xe-gau-01");
        ReflectionTestUtils.setField(online, "id", 3L);
        online.setStatus(VehicleStatus.Online);
        when(vehicleRepository.findByStatusAndSessionId("Online", 1L)).thenReturn(List.of(online));

        CreateTicketResponse response = service.create(actor, new CreateTicketRequest(" 0912345678 ", 30, "Cash", null));

        assertEquals(4, response.ticket_no());
        assertEquals(50000, response.amount_paid());
        assertEquals(5, session.getNextTicketNo());
        TicketResponse ticket = response.ticket();
        assertEquals(TicketStatus.Waiting, ticket.status());
        assertEquals("0912345678", ticket.phone());
        assertEquals(2000, ticket.overdue_fee_per_minute());
        assertEquals(5, ticket.overdue_grace_minutes());
        assertTrue(ticket.refund_enabled());
        assertEquals(50000, ticket.price_amount());
        assertEquals(30, ticket.price_minutes());
        assertEquals(1, response.suggested_vehicles().size());
        assertEquals("xe-gau-01", response.suggested_vehicles().get(0).slug());

        ArgumentCaptor<Payment> payment = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(payment.capture());
        assertEquals(PaymentType.Ticket, payment.getValue().getType());
        assertEquals(50000, payment.getValue().getAmount());
        assertEquals(10L, payment.getValue().getTicketId());
        assertEquals(1L, payment.getValue().getSessionId());
        assertEquals(PaymentMethod.Cash, payment.getValue().getMethod());
    }

    @Test
    void createPricesOddMinutesWithTheDefaultPackage() throws Exception {
        when(sessionRepository.findByStatusForUpdate(SessionStatus.InProgress)).thenReturn(Optional.of(activeSession(1)));
        CreateTicketResponse response = service.create(actor, new CreateTicketRequest("0912345678", 45, "Bank", null));
        assertEquals(75000, response.amount_paid());
        assertEquals(PaymentMethod.Bank, response.ticket().payment_method());
    }

    @Test
    void createRejectsMinutesBelowTheMinimum() {
        when(sessionRepository.findByStatusForUpdate(SessionStatus.InProgress)).thenReturn(Optional.of(activeSession(1)));
        BelowMinimumTimeException error = assertThrows(BelowMinimumTimeException.class,
                () -> service.create(actor, new CreateTicketRequest("0912345678", 10, "Cash", null)));
        assertEquals("BELOW_MINIMUM_TIME", error.getCode());
        verifyNoInteractions(ticketRepository);
    }

    @Test
    void createIsBlockedOncePlannedEndHasPassed() {
        Session session = activeSession(1);
        session.setPlannedEndAt(LocalDateTime.of(2026, 11, 5, 10, 0));
        when(sessionRepository.findByStatusForUpdate(SessionStatus.InProgress)).thenReturn(Optional.of(session));
        ConflictException error = assertThrows(ConflictException.class,
                () -> service.create(actor, new CreateTicketRequest("0912345678", 30, "Cash", null)));
        assertEquals("SESSION_PAST_PLANNED_END", error.getCode());
        assertEquals(1, session.getNextTicketNo());
        verifyNoInteractions(ticketRepository);
    }

    @Test
    void createIsAllowedBeforePlannedEnd() throws Exception {
        Session session = activeSession(1);
        session.setPlannedEndAt(LocalDateTime.of(2026, 11, 5, 10, 1));
        when(sessionRepository.findByStatusForUpdate(SessionStatus.InProgress)).thenReturn(Optional.of(session));
        CreateTicketResponse response = service.create(actor, new CreateTicketRequest("0912345678", 30, "Cash", null));
        assertEquals(1, response.ticket_no());
        assertEquals(50000, response.amount_paid());
    }

    @Test
    void createStoresNormalizedPhone() throws Exception {
        Session session = activeSession(4);
        when(sessionRepository.findByStatusForUpdate(SessionStatus.InProgress)).thenReturn(Optional.of(session));

        CreateTicketResponse response = service.create(actor, new CreateTicketRequest("+84 912-345-678", 30, "Cash", null));

        assertEquals("0912345678", response.ticket().phone());
    }

    @Test
    void createWithoutQuantityLeavesTheTicketOutOfAnyGroup() throws Exception {
        when(sessionRepository.findByStatusForUpdate(SessionStatus.InProgress)).thenReturn(Optional.of(activeSession(4)));

        CreateTicketResponse response = service.create(actor, new CreateTicketRequest("0912345678", 30, "Cash", null));

        assertEquals(1, response.quantity());
        assertEquals(1, response.tickets().size());
        assertNull(response.ticket().group_id());
        assertNull(response.ticket().group_size());
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    void createWithQuantityBuildsOneGroupOfConsecutiveTickets() throws Exception {
        Session session = activeSession(4);
        when(sessionRepository.findByStatusForUpdate(SessionStatus.InProgress)).thenReturn(Optional.of(session));

        CreateTicketResponse response = service.create(actor, new CreateTicketRequest("0912345678", 30, "Cash", 3));

        assertEquals(3, response.quantity());
        assertEquals(4, response.ticket_no());
        assertEquals(150000, response.amount_paid());
        assertEquals(7, session.getNextTicketNo());
        assertEquals(3, response.tickets().size());
        for (int index = 0; index < 3; index++) {
            TicketResponse item = response.tickets().get(index);
            assertEquals(4 + index, item.ticket_no());
            assertEquals(50000, item.amount_paid());
            assertEquals(TicketStatus.Waiting, item.status());
            assertEquals(10L, item.group_id());
            assertEquals(3, item.group_size());
        }
        verify(paymentRepository, times(3)).save(any(Payment.class));
    }

    @Test
    void searchNormalizesPhoneBeforeQuerying() throws Exception {
        when(sessionRepository.findFirstByStatus(SessionStatus.InProgress)).thenReturn(Optional.of(activeSession(8)));
        when(ticketRepository.searchByPhone(eq(1L), eq("0912345678"), any(Pageable.class)))
                .thenReturn(List.of(ticket(70L, 7, TicketStatus.Waiting)));

        assertEquals(1, service.search(new TicketSearchQuery("+84 912 345 678", null)).size());
    }

    @Test
    void createRequiresAnActiveSession() {
        when(sessionRepository.findByStatusForUpdate(SessionStatus.InProgress)).thenReturn(Optional.empty());
        assertThrows(NoActiveSessionException.class,
                () -> service.create(actor, new CreateTicketRequest("0912345678", 30, "Cash", null)));
        verifyNoInteractions(ticketRepository);
    }

    @Test
    void shortNumbersSearchTicketNumberAndPhoneWithTicketFirst() throws Exception {
        when(sessionRepository.findFirstByStatus(SessionStatus.InProgress)).thenReturn(Optional.of(activeSession(8)));
        when(ticketRepository.findBySessionIdAndTicketNo(1L, 7)).thenReturn(List.of(ticket(70L, 7, TicketStatus.Waiting)));
        when(ticketRepository.searchByPhone(eq(1L), eq("7"), any(Pageable.class)))
                .thenReturn(List.of(ticket(90L, 9, TicketStatus.Waiting), ticket(80L, 8, TicketStatus.Waiting)));
        List<TicketResponse> found = service.search(new TicketSearchQuery("7", null));
        assertEquals(3, found.size());
        assertEquals(7, found.get(0).ticket_no());
        assertEquals(9, found.get(1).ticket_no());
        assertEquals(8, found.get(2).ticket_no());
    }

    @Test
    void shortNumberMatchingBothWaysListsTicketOnce() throws Exception {
        when(sessionRepository.findFirstByStatus(SessionStatus.InProgress)).thenReturn(Optional.of(activeSession(8)));
        Ticket seven = ticket(70L, 7, TicketStatus.Waiting);
        when(ticketRepository.findBySessionIdAndTicketNo(1L, 7)).thenReturn(List.of(seven));
        when(ticketRepository.searchByPhone(eq(1L), eq("7"), any(Pageable.class))).thenReturn(List.of(seven));
        assertEquals(1, service.search(new TicketSearchQuery("7", null)).size());
    }

    @Test
    void fourDigitsFindPhonesContainingThem() throws Exception {
        when(sessionRepository.findFirstByStatus(SessionStatus.InProgress)).thenReturn(Optional.of(activeSession(8)));
        when(ticketRepository.findBySessionIdAndTicketNo(1L, 912)).thenReturn(List.of());
        when(ticketRepository.searchByPhone(eq(1L), eq("0912"), any(Pageable.class)))
                .thenReturn(List.of(ticket(70L, 7, TicketStatus.Waiting)));
        List<TicketResponse> found = service.search(new TicketSearchQuery("0912", null));
        assertEquals(1, found.size());
        assertEquals(7, found.get(0).ticket_no());
    }

    @Test
    void longNumbersSearchByPhone() throws Exception {
        when(sessionRepository.findFirstByStatus(SessionStatus.InProgress)).thenReturn(Optional.of(activeSession(8)));
        when(ticketRepository.searchByPhone(eq(1L), eq("12345"), any(Pageable.class)))
                .thenReturn(List.of(ticket(70L, 7, TicketStatus.Waiting)));
        assertEquals(1, service.search(new TicketSearchQuery("12345", null)).size());
        verify(ticketRepository, never()).findBySessionIdAndTicketNo(anyLong(), anyInt());
    }

    @Test
    void searchWithoutAnySessionReturnsNothing() throws Exception {
        when(sessionRepository.findFirstByStatus(SessionStatus.InProgress)).thenReturn(Optional.empty());
        assertTrue(service.search(new TicketSearchQuery("7", null)).isEmpty());
        verifyNoInteractions(ticketRepository);
    }

    @Test
    void searchInUnknownSessionIsRejected() {
        when(sessionRepository.existsById(99L)).thenReturn(false);
        assertThrows(SessionNotExits.class, () -> service.search(new TicketSearchQuery("7", 99L)));
    }

    @Test
    void queueListsWaitingTicketsInOrderWithPositions() {
        when(sessionRepository.findFirstByStatus(SessionStatus.InProgress)).thenReturn(Optional.of(activeSession(4)));
        when(ticketRepository.findBySessionIdAndStatusOrderByQueuedAtAscIdAsc(1L, TicketStatus.Waiting))
                .thenReturn(List.of(ticket(1L, 1, TicketStatus.Waiting), ticket(2L, 2, TicketStatus.Waiting)));
        TicketQueueResponse queue = service.queue();
        assertEquals(1L, queue.session_id());
        assertEquals(2, queue.total());
        assertEquals(1, queue.items().get(0).position());
        assertEquals(1, queue.items().get(0).ticket().ticket_no());
        assertEquals(2, queue.items().get(1).position());
    }

    @Test
    void queueWithoutSessionIsEmpty() {
        when(sessionRepository.findFirstByStatus(SessionStatus.InProgress)).thenReturn(Optional.empty());
        TicketQueueResponse queue = service.queue();
        assertEquals(0, queue.total());
        assertTrue(queue.items().isEmpty());
    }

    private Ticket dueTicket(long id, int no, TicketStatus status, LocalDateTime dueAt) {
        Ticket ticket = ticket(id, no, status);
        ticket.setStartedAt(dueAt.minusMinutes(30));
        ticket.setDueAt(dueAt);
        return ticket;
    }

    private static final LocalDateTime NOW_UTC = LocalDateTime.of(2026, 11, 5, 10, 0);

    @Test
    void overdueComputesMinutesGraceAndEstimatedFee() {
        when(sessionRepository.findFirstByStatus(SessionStatus.InProgress)).thenReturn(Optional.of(activeSession(4)));
        Ticket late = dueTicket(1L, 1, TicketStatus.Inused, NOW_UTC.minusMinutes(12));
        Ticket withinGrace = dueTicket(2L, 2, TicketStatus.Overdue, NOW_UTC.minusMinutes(3).minusSeconds(10));
        when(ticketRepository.findOverdueTickets(1L, TicketStatus.Overdue, TicketStatus.Inused, NOW_UTC))
                .thenReturn(List.of(late, withinGrace));
        TicketOverdueResponse response = service.overdue();
        assertEquals(1L, response.session_id());
        assertEquals(2, response.total());
        assertEquals(12, response.items().get(0).overdue_minutes());
        assertEquals(7, response.items().get(0).chargeable_overdue_minutes());
        assertEquals(14000, response.items().get(0).estimated_overdue_fee());
        assertEquals(1, response.items().get(0).ticket().ticket_no());
        assertEquals(4, response.items().get(1).overdue_minutes());
        assertEquals(0, response.items().get(1).chargeable_overdue_minutes());
        assertEquals(0, response.items().get(1).estimated_overdue_fee());
    }

    @Test
    void overdueRoundsPartialMinutesUp() {
        when(sessionRepository.findFirstByStatus(SessionStatus.InProgress)).thenReturn(Optional.of(activeSession(2)));
        Ticket ticket = dueTicket(1L, 1, TicketStatus.Inused, NOW_UTC.minusMinutes(6).minusSeconds(1));
        when(ticketRepository.findOverdueTickets(1L, TicketStatus.Overdue, TicketStatus.Inused, NOW_UTC))
                .thenReturn(List.of(ticket));
        TicketOverdueResponse response = service.overdue();
        assertEquals(7, response.items().get(0).overdue_minutes());
        assertEquals(2, response.items().get(0).chargeable_overdue_minutes());
        assertEquals(4000, response.items().get(0).estimated_overdue_fee());
    }

    @Test
    void overdueUsesTheTicketSnapshotNotTheCurrentSettings() {
        when(sessionRepository.findFirstByStatus(SessionStatus.InProgress)).thenReturn(Optional.of(activeSession(2)));
        Ticket ticket = dueTicket(1L, 1, TicketStatus.Overdue, NOW_UTC.minusMinutes(10));
        ticket.setOverdueFeePerMinute(3000);
        ticket.setOverdueGraceMinutes(0);
        when(ticketRepository.findOverdueTickets(1L, TicketStatus.Overdue, TicketStatus.Inused, NOW_UTC))
                .thenReturn(List.of(ticket));
        TicketOverdueResponse response = service.overdue();
        assertEquals(10, response.items().get(0).chargeable_overdue_minutes());
        assertEquals(30000, response.items().get(0).estimated_overdue_fee());
    }

    @Test
    void overdueWithoutSessionIsEmpty() {
        when(sessionRepository.findFirstByStatus(SessionStatus.InProgress)).thenReturn(Optional.empty());
        TicketOverdueResponse response = service.overdue();
        assertEquals(0, response.total());
        assertTrue(response.items().isEmpty());
        verifyNoInteractions(ticketRepository);
    }

    @Test
    void listDefaultsToTheCurrentSessionAndPaginates() throws Exception {
        when(sessionRepository.findFirstByStatus(SessionStatus.InProgress)).thenReturn(Optional.of(activeSession(3)));
        when(ticketRepository.findBySessionIdOrderByQueuedAtDescIdDesc(eq(1L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(ticket(2L, 2, TicketStatus.Waiting)), PageRequest.of(0, 20), 1));
        PageResponse<TicketResponse> page = service.list(new TicketListQuery(null, null, null, null));
        assertEquals(1, page.page());
        assertEquals(20, page.limit());
        assertEquals(1L, page.total());
        assertEquals(2, page.items().get(0).ticket_no());
    }

    @Test
    void listFiltersByStatus() throws Exception {
        when(sessionRepository.existsById(1L)).thenReturn(true);
        when(ticketRepository.findBySessionIdAndStatusOrderByQueuedAtDescIdDesc(eq(1L), eq(TicketStatus.Completed), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(1, 10), 0));
        PageResponse<TicketResponse> page = service.list(new TicketListQuery("Completed", 1L, 2, 10));
        assertEquals(2, page.page());
        assertEquals(10, page.limit());
        assertTrue(page.items().isEmpty());
    }
}
