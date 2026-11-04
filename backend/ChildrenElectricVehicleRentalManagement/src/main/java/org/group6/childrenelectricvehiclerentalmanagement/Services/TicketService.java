package org.group6.childrenelectricvehiclerentalmanagement.Services;

import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Long.Requests;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Long.Responses;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.PaymentMethod;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.PaymentType;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.ReleasedVehicleStatus;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.SessionStatus;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.TicketStatus;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.VehicleStatus;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.*;
import org.group6.childrenelectricvehiclerentalmanagement.Models.Payment;
import org.group6.childrenelectricvehiclerentalmanagement.Models.Session;
import org.group6.childrenelectricvehiclerentalmanagement.Models.Settings;
import org.group6.childrenelectricvehiclerentalmanagement.Models.Ticket;
import org.group6.childrenelectricvehiclerentalmanagement.Models.LinkingObject.TicketVehicleHistory;
import org.group6.childrenelectricvehiclerentalmanagement.Models.User;
import org.group6.childrenelectricvehiclerentalmanagement.Models.Vehicle;
import org.group6.childrenelectricvehiclerentalmanagement.Repositories.PackageRepository;
import org.group6.childrenelectricvehiclerentalmanagement.Repositories.PaymentRepository;
import org.group6.childrenelectricvehiclerentalmanagement.Repositories.SessionRepository;
import org.group6.childrenelectricvehiclerentalmanagement.Repositories.SessionVehicleRepository;
import org.group6.childrenelectricvehiclerentalmanagement.Repositories.TicketRepository;
import org.group6.childrenelectricvehiclerentalmanagement.Repositories.TicketVehicleHistoryRepository;
import org.group6.childrenelectricvehiclerentalmanagement.Repositories.UserRepository;
import org.group6.childrenelectricvehiclerentalmanagement.Repositories.VehicleRepository;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.*;
import lombok.RequiredArgsConstructor;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.Validators.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TicketService {
    private static final int SUGGESTED_VEHICLE_LIMIT = 5;

    private final TicketRepository ticketRepository;
    private final SessionRepository sessionRepository;
    private final SessionVehicleRepository sessionVehicleRepository;
    private final VehicleRepository vehicleRepository;
    private final PaymentRepository paymentRepository;
    private final TicketVehicleHistoryRepository historyRepository;
    private final PackageRepository packageRepository;
    private final UserRepository userRepository;
    private final SettingsService settingsService;

    @Transactional(rollbackFor = Exception.class)
    public Responses.CreateTicketResponse createTicket(Requests.CreateTicketRequest request) throws Exception {
        String phone = PhoneValidator.validate(request.getPhone());
        int minutes = MinutesValidator.validatePositive("minutes", request.getMinutes());
        PaymentMethod method = PaymentMethodValidator.validate(request.getPaymentMethod());

        Settings settings = settingsService.loadSettings();
        if (minutes < settings.getMinimumMinutes()) {
            throw new BelowMinimumTime("Thời gian thuê tối thiểu là " + settings.getMinimumMinutes() + " phút");
        }

        Session session = sessionRepository.lockByStatus(SessionStatus.InProgress).stream().findFirst()
                .orElseThrow(NoActiveSession::new);

        List<ValidPackage> packages = packageRepository.findByActiveTrueOrderByMinutesAsc().stream()
                .map(p -> new ValidPackage(p.getAmount(), p.getMinutes(), Boolean.TRUE.equals(p.getIsDefault())))
                .toList();
        ValidPackage defaultPackage = packages.stream().filter(ValidPackage::isDefault).findFirst()
                .orElseThrow(NoPricePackage::new);

        int price = PricingUtil.priceForNewTicket(minutes, packages, defaultPackage);
        LocalDateTime now = TimeUtil.now();
        User actor = userRepository.getReferenceById(request.getActorId());

        Ticket ticket = new Ticket();
        ticket.setSession(session);
        ticket.setTicketNo(session.getNextTicketNo());
        ticket.setPhone(phone);
        ticket.setStatus(TicketStatus.Waiting);
        ticket.setVehicle(null);
        ticket.setPaymentMethod(method);
        ticket.setTotalMinutes(minutes);
        ticket.setAmountPaid(price);
        ticket.setPriceAmount(defaultPackage.amount());
        ticket.setPriceMinutes(defaultPackage.minutes());
        ticket.setOverdueFeePerMinute(settings.getOverdueFeePerMinute());
        ticket.setOverdueGraceMinutes(settings.getOverdueGraceMinutes());
        ticket.setRefundEnabled(settings.getRefundEnabled());
        ticket.setQueuedAt(now);
        ticket.setCreatedBy(actor);
        ticketRepository.save(ticket);

        session.setNextTicketNo(session.getNextTicketNo() + 1);
        sessionRepository.save(session);

        recordPayment(session, ticket, PaymentType.Ticket, method, price, minutes, actor);

        List<Responses.VehicleBrief> suggested = sessionVehicleRepository
                .findBySessionIdAndRemovedAtIsNull(session.getId()).stream()
                .map(sv -> sv.getVehicle())
                .filter(v -> v.getDeletedAt() == null && v.getStatus() == VehicleStatus.Online)
                .limit(SUGGESTED_VEHICLE_LIMIT)
                .map(this::toVehicleBrief)
                .toList();

        return new Responses.CreateTicketResponse(toResponse(ticket, now), suggested);
    }

    @Transactional(rollbackFor = Exception.class)
    public Responses.PageResponse<Responses.TicketResponse> listTickets(Requests.ListTicketsRequest request)
            throws Exception {
        Long sessionId = IdValidator.validateOptional("session_id", request.getSessionId());
        TicketStatus status = TicketStatusValidator.validateOptional(request.getStatus());
        PageParams params = PageValidator.validate(request.getPage(), request.getLimit());

        Optional<Session> session = resolveSession(sessionId);
        if (session.isEmpty()) {
            return new Responses.PageResponse<>(List.of(), params.page(), params.limit(), 0, 0, null);
        }
        LocalDateTime now = TimeUtil.now();
        refreshOverdueIfRunning(session.get(), now);

        Pageable pageable = PageRequest.of(params.page() - 1, params.limit(), Sort.by(Sort.Direction.DESC, "id"));
        Page<Ticket> page = status == null
                ? ticketRepository.findBySessionId(session.get().getId(), pageable)
                : ticketRepository.findBySessionIdAndStatus(session.get().getId(), status, pageable);

        List<Responses.TicketResponse> items = page.getContent().stream().map(t -> toResponse(t, now)).toList();
        return new Responses.PageResponse<>(items, params.page(), params.limit(), page.getTotalElements(),
                page.getTotalPages(), session.get().getId());
    }

    @Transactional(rollbackFor = Exception.class)
    public Responses.TicketListResponse searchTickets(Requests.SearchTicketsRequest request) throws Exception {
        String q = SearchQueryValidator.validate(request.getQ());
        Long sessionId = IdValidator.validateOptional("session_id", request.getSessionId());

        Optional<Session> session = resolveSession(sessionId);
        if (session.isEmpty()) {
            return new Responses.TicketListResponse(List.of(), 0, null);
        }
        LocalDateTime now = TimeUtil.now();
        refreshOverdueIfRunning(session.get(), now);

        Map<Long, Ticket> found = new LinkedHashMap<>();
        for (Ticket t : ticketRepository.findBySessionIdAndPhoneContainingOrderByIdDesc(session.get().getId(), q)) {
            found.put(t.getId(), t);
        }
        if (q.length() <= 9 && q.chars().allMatch(Character::isDigit)) {
            for (Ticket t : ticketRepository.findBySessionIdAndTicketNo(session.get().getId(), Integer.parseInt(q))) {
                found.putIfAbsent(t.getId(), t);
            }
        }
        List<Responses.TicketResponse> items = found.values().stream().map(t -> toResponse(t, now)).toList();
        return new Responses.TicketListResponse(items, items.size(), session.get().getId());
    }

    @Transactional(rollbackFor = Exception.class)
    public Responses.TicketListResponse getQueue(Requests.BaseRequest request) throws Exception {
        Optional<Session> session = resolveSession(null);
        if (session.isEmpty()) {
            return new Responses.TicketListResponse(List.of(), 0, null);
        }
        LocalDateTime now = TimeUtil.now();
        List<Responses.TicketResponse> items = ticketRepository
                .findBySessionIdAndStatusOrderByQueuedAtAscIdAsc(session.get().getId(), TicketStatus.Waiting).stream()
                .map(t -> toResponse(t, now)).toList();
        return new Responses.TicketListResponse(items, items.size(), session.get().getId());
    }

    @Transactional(rollbackFor = Exception.class)
    public Responses.TicketListResponse getOverdue(Requests.BaseRequest request) throws Exception {
        Optional<Session> session = resolveSession(null);
        if (session.isEmpty()) {
            return new Responses.TicketListResponse(List.of(), 0, null);
        }
        LocalDateTime now = TimeUtil.now();
        refreshOverdueIfRunning(session.get(), now);
        List<Responses.TicketResponse> items = ticketRepository
                .findBySessionIdAndStatusOrderByDueAtAsc(session.get().getId(), TicketStatus.Overdue).stream()
                .map(t -> toResponse(t, now)).toList();
        return new Responses.TicketListResponse(items, items.size(), session.get().getId());
    }

    @Transactional(rollbackFor = Exception.class)
    public Responses.TicketDetailResponse getTicket(Requests.TicketIdRequest request) throws Exception {
        Long id = IdValidator.validate("id", request.getId());
        Ticket ticket = ticketRepository.findById(id).orElseThrow(TicketNotFound::new);
        LocalDateTime now = TimeUtil.now();
        refreshOverdueIfRunning(ticket.getSession(), now);

        List<Responses.PaymentResponse> payments = paymentRepository.findByIdOrderByIdAsc(id).stream()
                .map(p -> new Responses.PaymentResponse(p.getId(), p.getType().name(), p.getMethod().name(),
                        p.getAmount(), p.getMinutes(), TimeUtil.iso(p.getCreatedAt())))
                .toList();
        List<Responses.VehicleHistoryResponse> history = historyRepository.findByTicketIdOrderByIdAsc(id).stream()
                .map(h -> new Responses.VehicleHistoryResponse(toVehicleBrief(h.getVehicle()),
                        TimeUtil.iso(h.getAssignedAt()), TimeUtil.iso(h.getReleasedAt()),
                        h.getReleasedStatus()))
                .toList();
        return new Responses.TicketDetailResponse(toResponse(ticket, now), payments, history);
    }

    @Transactional(rollbackFor = Exception.class)
    public Responses.TicketResponse assignVehicle(Requests.AssignVehicleRequest request) throws Exception {
        Long id = IdValidator.validate("id", request.getId());
        String slug = VehicleSlugValidator.validate(request.getVehicleSlug());

        Ticket ticket = lockTicket(id);
        requireSessionRunning(ticket);
        if (ticket.getStatus() != TicketStatus.Waiting) {
            throw new TicketNotAssignable();
        }
        Vehicle vehicle = requireAvailableVehicle(slug, ticket.getSession());

        LocalDateTime now = TimeUtil.now();
        vehicle.setStatus(VehicleStatus.Inused);
        vehicleRepository.save(vehicle);

        ticket.setVehicle(vehicle);
        ticket.setStatus(TicketStatus.Inused);
        ticket.setStartedAt(now);
        ticket.setDueAt(now.plusMinutes(ticket.getTotalMinutes()));
        ticketRepository.save(ticket);

        openHistory(ticket, vehicle, now);
        return toResponse(ticket, now);
    }

    @Transactional(rollbackFor = Exception.class)
    public Responses.CancelTicketResponse cancelTicket(Requests.CancelTicketRequest request) throws Exception {
        Long id = IdValidator.validate("id", request.getId());
        PaymentMethod refundMethod = PaymentMethodValidator.validateOptional(request.getRefundMethod());

        Ticket ticket = lockTicket(id);
        requireSessionRunning(ticket);
        if (ticket.getStatus() != TicketStatus.Waiting) {
            throw new TicketNotCancelable();
        }
        LocalDateTime now = TimeUtil.now();
        User actor = userRepository.getReferenceById(request.getActorId());
        PaymentMethod method = refundMethod != null ? refundMethod : ticket.getPaymentMethod();

        int refund = ticket.getAmountPaid();
        ticket.setStatus(TicketStatus.Canceled);
        ticket.setCanceledAt(now);
        ticket.setRefundAmount(refund);
        ticketRepository.save(ticket);

        if (refund > 0) {
            recordPayment(ticket.getSession(), ticket, PaymentType.Refund, method, refund, ticket.getTotalMinutes(), actor);
        }
        return new Responses.CancelTicketResponse(ticket.getId(), ticket.getTicketNo(), ticket.getStatus().name(),
                refund, method.name());
    }

    @Transactional(rollbackFor = Exception.class)
    public Responses.ChangeVehicleResponse changeVehicle(Requests.ChangeVehicleRequest request)
            throws Exception {
        Long id = IdValidator.validate("id", request.getId());
        String slug = VehicleSlugValidator.validate(request.getVehicleSlug());
        ReleasedVehicleStatus oldStatus = ReleasedVehicleStatusValidator.validate(request.getOldVehicleStatus());

        Ticket ticket = lockTicket(id);
        requireSessionRunning(ticket);
        requireActive(ticket, "TICKET_NOT_ACTIVE", "Chỉ đổi xe được cho vé đang sử dụng");
        Vehicle oldVehicle = ticket.getVehicle();
        if (oldVehicle != null && oldVehicle.getSlug().equals(slug)) {
            throw new SameVehicle();
        }
        Vehicle newVehicle = requireAvailableVehicle(slug, ticket.getSession());

        LocalDateTime now = TimeUtil.now();
        closeHistory(ticket, now, oldStatus.name());
        if (oldVehicle != null) {
            oldVehicle.setStatus(VehicleStatus.valueOf(oldStatus.name()));
            vehicleRepository.save(oldVehicle);
        }
        newVehicle.setStatus(VehicleStatus.Inused);
        vehicleRepository.save(newVehicle);

        ticket.setVehicle(newVehicle);
        ticketRepository.save(ticket);
        openHistory(ticket, newVehicle, now);

        return new Responses.ChangeVehicleResponse(toResponse(ticket, now), toVehicleBrief(oldVehicle));
    }

    @Transactional(rollbackFor = Exception.class)
    public Responses.ExtendTicketResponse extendTicket(Requests.ExtendTicketRequest request) throws Exception {
        Long id = IdValidator.validate("id", request.getId());
        int minutes = MinutesValidator.validatePositive("minutes", request.getMinutes());
        PaymentMethod method = PaymentMethodValidator.validate(request.getPaymentMethod());

        if (!Boolean.TRUE.equals(settingsService.loadSettings().getExtendEnabled())) {
            throw new ExtendDisabled();
        }
        Ticket ticket = lockTicket(id);
        requireSessionRunning(ticket);
        requireActive(ticket, "TICKET_NOT_ACTIVE", "Chỉ gia hạn được vé đang sử dụng");

        LocalDateTime now = TimeUtil.now();
        User actor = userRepository.getReferenceById(request.getActorId());
        int price = PricingUtil.priceByRate(minutes, ticket.getPriceAmount(), ticket.getPriceMinutes());

        ticket.setTotalMinutes(ticket.getTotalMinutes() + minutes);
        ticket.setAmountPaid(ticket.getAmountPaid() + price);
        ticket.setDueAt(ticket.getDueAt().plusMinutes(minutes));
        if (ticket.getStatus() == TicketStatus.Overdue && ticket.getDueAt().isAfter(now)) {
            ticket.setStatus(TicketStatus.Inused);
        }
        ticketRepository.save(ticket);

        recordPayment(ticket.getSession(), ticket, PaymentType.Extension, method, price, minutes, actor);
        return new Responses.ExtendTicketResponse(toResponse(ticket, now), minutes, price);
    }

    @Transactional(rollbackFor = Exception.class)
    public Responses.ReturnTicketResponse returnTicket(Requests.ReturnTicketRequest request) throws Exception {
        Long id = IdValidator.validate("id", request.getId());
        PaymentMethod feeMethodInput = PaymentMethodValidator.validateOptional(request.getPaymentMethod());
        PaymentMethod refundMethodInput = PaymentMethodValidator.validateOptional(request.getRefundMethod());

        Ticket ticket = lockTicket(id);
        requireSessionRunning(ticket);
        requireActive(ticket, "TICKET_NOT_RETURNABLE", "Chỉ trả xe được cho vé đang sử dụng");

        LocalDateTime now = TimeUtil.now();
        User actor = userRepository.getReferenceById(request.getActorId());

        int overdueMinutes = PricingUtil.overdueMinutes(ticket.getDueAt(), now);
        int chargeable = PricingUtil.chargeableMinutes(overdueMinutes, ticket.getOverdueGraceMinutes());
        int fee = PricingUtil.overdueFee(chargeable, ticket.getOverdueFeePerMinute());

        int refund = 0;
        int remaining = 0;
        if (overdueMinutes == 0 && Boolean.TRUE.equals(ticket.getRefundEnabled())) {
            remaining = PricingUtil.remainingMinutes(ticket.getDueAt(), now);
            refund = PricingUtil.refundForEarlyReturn(remaining, ticket.getPriceAmount(), ticket.getPriceMinutes(),
                    ticket.getAmountPaid());
        }

        if (fee > 0) {
            PaymentMethod feeMethod = feeMethodInput != null ? feeMethodInput : ticket.getPaymentMethod();
            recordPayment(ticket.getSession(), ticket, PaymentType.OverdueFee, feeMethod, fee, chargeable, actor);
        }
        if (refund > 0) {
            PaymentMethod refundMethod = refundMethodInput != null ? refundMethodInput : ticket.getPaymentMethod();
            recordPayment(ticket.getSession(), ticket, PaymentType.Refund, refundMethod, refund, remaining, actor);
        }

        ticket.setStatus(TicketStatus.Completed);
        ticket.setReturnedAt(now);
        ticket.setOverdueMinutes(overdueMinutes);
        ticket.setOverdueFee(fee);
        ticket.setRefundAmount(refund);
        ticketRepository.save(ticket);

        Vehicle vehicle = ticket.getVehicle();
        if (vehicle != null) {
            vehicle.setStatus(VehicleStatus.Online);
            vehicleRepository.save(vehicle);
        }
        closeHistory(ticket, now, ReleasedVehicleStatus.Online.name());

        Responses.NextTicket next = ticketRepository
                .findFirstBySessionIdAndStatusOrderByQueuedAtAscIdAsc(ticket.getSession().getId(), TicketStatus.Waiting)
                .map(t -> new Responses.NextTicket(t.getTicketNo(), t.getPhone()))
                .orElse(null);

        return new Responses.ReturnTicketResponse(ticket.getId(), ticket.getTicketNo(), ticket.getStatus().name(),
                overdueMinutes, chargeable, fee, refund, toVehicleBrief(vehicle), next);
    }

    @Transactional(rollbackFor = Exception.class)
    public void refreshOverdueIfRunning(Session session, LocalDateTime now) {
        if (session == null || session.getStatus() != SessionStatus.InProgress) {
            return;
        }
        List<Ticket> late = ticketRepository.findBySessionIdAndStatusAndDueAtBefore(
                session.getId(), TicketStatus.Inused, now);
        if (late.isEmpty()) {
            return;
        }
        for (Ticket t : late) {
            t.setStatus(TicketStatus.Overdue);
        }
        ticketRepository.saveAll(late);
    }

    private Optional<Session> resolveSession(Long sessionId) throws Exception {
        if (sessionId != null) {
            Session session = sessionRepository.findById(sessionId)
                    .orElseThrow(() -> new SessionNotFound());
            return Optional.of(session);
        }
        return sessionRepository.findFirstByStatusOrderByIdDesc(SessionStatus.InProgress);
    }

    private Ticket lockTicket(Long id) throws Exception {
        return ticketRepository.lockById(id).orElseThrow(TicketNotFound::new);
    }

    private void requireSessionRunning(Ticket ticket) throws SessionNotActive {
        if (ticket.getSession().getStatus() != SessionStatus.InProgress) {
            throw new SessionNotActive();
        }
    }

    private void requireActive(Ticket ticket, String code, String message) throws TicketNotActive {
        if (ticket.getStatus() != TicketStatus.Inused && ticket.getStatus() != TicketStatus.Overdue) {
            throw new TicketNotActive();
        }
    }

    private Vehicle requireAvailableVehicle(String slug, Session session) throws Exception {
        Vehicle vehicle = vehicleRepository.lockBySlug(slug)
                .orElseThrow(() -> new VehicleNotFound());
        if (vehicle.getStatus() != VehicleStatus.Online) {
            throw new VehicleNotAvailable();
        }
        if (!sessionVehicleRepository.existsBySessionIdAndVehicleIdAndRemovedAtIsNull(session.getId(), vehicle.getId())) {
            throw new VehicleNotInSession();
        }
        return vehicle;
    }

    private void openHistory(Ticket ticket, Vehicle vehicle, LocalDateTime now) {
        TicketVehicleHistory history = new TicketVehicleHistory();
        history.setTicket(ticket);
        history.setVehicle(vehicle);
        history.setAssignedAt(now);
        historyRepository.save(history);
    }

    private void closeHistory(Ticket ticket, LocalDateTime now, String releasedStatus) {
        historyRepository.findFirstByTicketIdAndReleasedAtIsNull(ticket.getId()).ifPresent(h -> {
            h.setReleasedAt(now);
            h.setReleasedStatus(releasedStatus);
            historyRepository.save(h);
        });
    }

    private void recordPayment(Session session, Ticket ticket, PaymentType type, PaymentMethod method,
                               int amount, Integer minutes, User actor) {
        Payment payment = new Payment();
        payment.setSession(session);
        payment.setTicket(ticket);
        payment.setType(type);
        payment.setMethod(method);
        payment.setAmount(amount);
        payment.setMinutes(minutes);
        payment.setCreatedBy(actor);
        paymentRepository.save(payment);
    }

    private Responses.VehicleBrief toVehicleBrief(Vehicle vehicle) {
        if (vehicle == null) {
            return null;
        }
        return new Responses.VehicleBrief(vehicle.getId(), vehicle.getSlug(), vehicle.getStatus().name());
    }

    private Responses.TicketResponse toResponse(Ticket t, LocalDateTime now) {
        boolean active = t.getStatus() == TicketStatus.Inused || t.getStatus() == TicketStatus.Overdue;
        Integer remaining = null;
        int overdueMinutes = t.getOverdueMinutes();
        int fee = t.getOverdueFee();
        if (active) {
            remaining = PricingUtil.remainingMinutes(t.getDueAt(), now);
            overdueMinutes = PricingUtil.overdueMinutes(t.getDueAt(), now);
            fee = PricingUtil.overdueFee(
                    PricingUtil.chargeableMinutes(overdueMinutes, t.getOverdueGraceMinutes()),
                    t.getOverdueFeePerMinute());
        }
        int chargeable = PricingUtil.chargeableMinutes(overdueMinutes, t.getOverdueGraceMinutes());
        return new Responses.TicketResponse(
                t.getId(), t.getTicketNo(), t.getPhone(), t.getStatus().name(), t.getSession().getId(),
                toVehicleBrief(t.getVehicle()), t.getPaymentMethod().name(), t.getTotalMinutes(), t.getAmountPaid(),
                TimeUtil.iso(t.getQueuedAt()), TimeUtil.iso(t.getStartedAt()), TimeUtil.iso(t.getDueAt()),
                TimeUtil.iso(t.getReturnedAt()), remaining, overdueMinutes, chargeable, fee, t.getRefundAmount());
    }
}
