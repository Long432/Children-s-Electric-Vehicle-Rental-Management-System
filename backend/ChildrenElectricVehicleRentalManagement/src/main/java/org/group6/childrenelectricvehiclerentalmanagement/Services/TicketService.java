package org.group6.childrenelectricvehiclerentalmanagement.Services;

import org.group6.childrenelectricvehiclerentalmanagement.DTOs.PageResponse;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.SettingsResponse;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Ticket.CreateTicketRequest;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Ticket.CreateTicketResponse;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Ticket.OverdueTicketResponse;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Ticket.QueueTicketResponse;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Ticket.TicketListQuery;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Ticket.TicketOverdueResponse;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Ticket.TicketQueueResponse;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Ticket.TicketResponse;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Ticket.TicketSearchQuery;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Ticket.TicketVehicleResponse;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.PaymentMethod;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.PaymentType;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.SessionStatus;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.TicketStatus;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.VehicleStatus;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.AbstractException;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.BelowMinimumTimeException;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.NoActiveSessionException;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.SessionNotExits;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.TicketNotExits;
import org.group6.childrenelectricvehiclerentalmanagement.Models.Payment;
import org.group6.childrenelectricvehiclerentalmanagement.Models.Session;
import org.group6.childrenelectricvehiclerentalmanagement.Models.Ticket;
import org.group6.childrenelectricvehiclerentalmanagement.Repositories.PaymentRepository;
import org.group6.childrenelectricvehiclerentalmanagement.Repositories.SesssionRepository;
import org.group6.childrenelectricvehiclerentalmanagement.Repositories.TicketRepository;
import org.group6.childrenelectricvehiclerentalmanagement.Repositories.UserRepository;
import org.group6.childrenelectricvehiclerentalmanagement.Repositories.VehicleRepository;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.BusinessTime;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.Security.AuthenticatedUser;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.TicketPricing;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.Validators.CreateTicketRequestValidator;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.Validators.PageRules;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.Validators.TicketListQueryValidator;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.Validators.TicketSearchQueryValidator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class TicketService {

    private static final String ENTITY_TYPE = "ticket";
    private static final int SEARCH_LIMIT = 50;

    private static final CreateTicketRequestValidator CREATE_VALIDATOR = new CreateTicketRequestValidator();
    private static final TicketListQueryValidator LIST_VALIDATOR = new TicketListQueryValidator();
    private static final TicketSearchQueryValidator SEARCH_VALIDATOR = new TicketSearchQueryValidator();

    private final SesssionRepository sessionRepository;
    private final TicketRepository ticketRepository;
    private final PaymentRepository paymentRepository;
    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;
    private final SettingsService settingsService;
    private final AuditLogService auditLogService;
    private final BusinessTime businessTime;

    public TicketService(SesssionRepository sessionRepository,
                         TicketRepository ticketRepository,
                         PaymentRepository paymentRepository,
                         VehicleRepository vehicleRepository,
                         UserRepository userRepository,
                         SettingsService settingsService,
                         AuditLogService auditLogService,
                         BusinessTime businessTime) {
        this.sessionRepository = sessionRepository;
        this.ticketRepository = ticketRepository;
        this.paymentRepository = paymentRepository;
        this.vehicleRepository = vehicleRepository;
        this.userRepository = userRepository;
        this.settingsService = settingsService;
        this.auditLogService = auditLogService;
        this.businessTime = businessTime;
    }

    @Transactional(rollbackFor = Exception.class)
    public CreateTicketResponse create(AuthenticatedUser actor, CreateTicketRequest request) throws AbstractException {
        CREATE_VALIDATOR.validate(request);
        String phone = request.phone().trim();
        int minutes = request.minutes();
        PaymentMethod method = PaymentMethod.valueOf(request.payment_method().trim());
        Session session = sessionRepository.findByStatusForUpdate(SessionStatus.InProgress).orElseThrow(NoActiveSessionException::new);
        SettingsResponse settings = settingsService.getSettings();
        if (minutes < settings.getMinimumMinutes()) {
            throw new BelowMinimumTimeException(settings.getMinimumMinutes());
        }
        TicketPricing.Quote quote = TicketPricing.quote(minutes, settings.getPackages());
        LocalDateTime now = businessTime.nowUtc();
        int ticketNo = session.getNextTicketNo();

        Ticket ticket = new Ticket();
        ticket.setSessionId(session.getId());
        ticket.setTicketNo(ticketNo);
        ticket.setPhone(phone);
        ticket.setStatus(TicketStatus.Waiting);
        ticket.setPaymentMethod(method);
        ticket.setTotalMinutes(minutes);
        ticket.setAmountPaid(quote.amount());
        ticket.setPriceAmount(quote.priceAmount());
        ticket.setPriceMinutes(quote.priceMinutes());
        ticket.setOverdueFeePerMinute(settings.getOverdueFeePerMinute());
        ticket.setOverdueGraceMinutes(settings.getOverdueGraceMinutes());
        ticket.setRefundEnabled(settings.isRefundEnabled());
        ticket.setQueuedAt(now);
        ticket.setOverdueMinutes(0);
        ticket.setOverdueFee(0);
        ticket.setRefundAmount(0);
        ticket.setCreatedBy(userRepository.getReferenceById(actor.id()));
        ticket.setCreatedAt(now);
        ticket.setUpdatedAt(now);
        Ticket saved = ticketRepository.saveAndFlush(ticket);

        session.setNextTicketNo(ticketNo + 1);
        session.setUpdatedAt(now);
        sessionRepository.save(session);

        Payment payment = new Payment();
        payment.setSessionId(session.getId());
        payment.setTicketId(saved.getId());
        payment.setType(PaymentType.Ticket);
        payment.setMethod(method);
        payment.setAmount(quote.amount());
        payment.setMinutes(minutes);
        payment.setCreatedBy(userRepository.getReferenceById(actor.id()));
        payment.setCreatedAt(now);
        paymentRepository.save(payment);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("ticket_no", ticketNo);
        payload.put("minutes", minutes);
        payload.put("amount_paid", quote.amount());
        payload.put("payment_method", method.name());
        auditLogService.log(actor, "ticket.create", ENTITY_TYPE, saved.getId(), payload);

        List<TicketVehicleResponse> suggested = vehicleRepository
                .findByStatusAndSessionId(VehicleStatus.Online.name(), session.getId()).stream()
                .map(TicketVehicleResponse::from)
                .toList();
        return new CreateTicketResponse(ticketNo, quote.amount(), TicketResponse.from(saved), suggested);
    }

    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public TicketResponse get(long id) throws AbstractException {
        return TicketResponse.from(ticketRepository.findDetailById(id).orElseThrow(TicketNotExits::new));
    }

    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public PageResponse<TicketResponse> list(TicketListQuery query) throws AbstractException {
        LIST_VALIDATOR.validate(query);
        int page = PageRules.page(query.page());
        int limit = PageRules.limit(query.limit());
        Optional<Long> sessionId = resolveSessionId(query.session_id());
        if (sessionId.isEmpty()) {
            return PageResponse.empty(page, limit);
        }
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<Ticket> result = (query.status() == null || query.status().isBlank())
                ? ticketRepository.findBySessionIdOrderByQueuedAtDescIdDesc(sessionId.get(), pageable)
                : ticketRepository.findBySessionIdAndStatusOrderByQueuedAtDescIdDesc(sessionId.get(), TicketStatus.valueOf(query.status().trim()), pageable);
        List<TicketResponse> items = result.getContent().stream().map(TicketResponse::from).toList();
        return new PageResponse<>(items, page, limit, result.getTotalElements());
    }

    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public List<TicketResponse> search(TicketSearchQuery query) throws AbstractException {
        SEARCH_VALIDATOR.validate(query);
        Optional<Long> sessionId = resolveSessionId(query.session_id());
        if (sessionId.isEmpty()) {
            return List.of();
        }
        String value = query.q().trim();
        List<Ticket> found = value.length() <= TicketSearchQueryValidator.MAX_TICKET_NO_DIGITS
                ? ticketRepository.findBySessionIdAndTicketNo(sessionId.get(), Integer.parseInt(value))
                : ticketRepository.searchByPhone(sessionId.get(), value, PageRequest.of(0, SEARCH_LIMIT));
        return found.stream().map(TicketResponse::from).toList();
    }

    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public TicketQueueResponse queue() {
        Optional<Session> session = sessionRepository.findFirstByStatus(SessionStatus.InProgress);
        if (session.isEmpty()) {
            return new TicketQueueResponse(null, 0, List.of());
        }
        List<Ticket> waiting = ticketRepository.findBySessionIdAndStatusOrderByQueuedAtAscIdAsc(session.get().getId(), TicketStatus.Waiting);
        List<QueueTicketResponse> items = new ArrayList<>();
        for (int index = 0; index < waiting.size(); index++) {
            items.add(new QueueTicketResponse(index + 1, TicketResponse.from(waiting.get(index))));
        }
        return new TicketQueueResponse(session.get().getId(), items.size(), items);
    }

    @Transactional(readOnly = true)
    public TicketOverdueResponse overdue() {
        Optional<Session> session = sessionRepository.findFirstByStatus(SessionStatus.InProgress);
        if (session.isEmpty()) {
            return new TicketOverdueResponse(null, 0, List.of());
        }
        LocalDateTime now = businessTime.nowUtc();
        List<OverdueTicketResponse> items = ticketRepository
                .findOverdueTickets(session.get().getId(), TicketStatus.Overdue, TicketStatus.Inused, now).stream()
                .map(ticket -> toOverdueItem(ticket, now))
                .toList();
        return new TicketOverdueResponse(session.get().getId(), items.size(), items);
    }

    private OverdueTicketResponse toOverdueItem(Ticket ticket, LocalDateTime now) {
        long lateSeconds = ticket.getDueAt() == null ? 0 : Duration.between(ticket.getDueAt(), now).getSeconds();
        int overdueMinutes = TicketPricing.overdueMinutes(lateSeconds);
        int chargeableMinutes = TicketPricing.chargeableOverdueMinutes(overdueMinutes, ticket.getOverdueGraceMinutes());
        int estimatedFee = TicketPricing.estimatedOverdueFee(chargeableMinutes, ticket.getOverdueFeePerMinute());
        return new OverdueTicketResponse(TicketResponse.from(ticket), overdueMinutes, chargeableMinutes, estimatedFee);
    }

    private Optional<Long> resolveSessionId(Long requested) throws SessionNotExits {
        if (requested != null) {
            if (!sessionRepository.existsById(requested)) {
                throw new SessionNotExits();
            }
            return Optional.of(requested);
        }
        return sessionRepository.findFirstByStatus(SessionStatus.InProgress).map(Session::getId);
    }
}
