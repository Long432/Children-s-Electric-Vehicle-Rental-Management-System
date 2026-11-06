package org.group6.childrenelectricvehiclerentalmanagement.Services;

import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Ticket.AssignTicketRequest;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Ticket.CancelTicketRequest;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Ticket.CancelTicketResponse;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Ticket.ChangeVehicleRequest;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Ticket.ChangeVehicleResponse;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Ticket.ExtendTicketRequest;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Ticket.ExtendTicketResponse;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Ticket.NextTicketResponse;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Ticket.ReturnTicketRequest;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Ticket.ReturnTicketResponse;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Ticket.TicketResponse;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Ticket.TicketVehicleResponse;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.PaymentMethod;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.PaymentType;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.ReleasedVehicleStatus;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.SessionStatus;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.TicketStatus;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.VehicleStatus;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.AbstractException;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.ConflictException;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.NoActiveSessionException;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.TicketNotExits;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.ValidationException;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.VehicleNotExits;
import org.group6.childrenelectricvehiclerentalmanagement.Models.Payment;
import org.group6.childrenelectricvehiclerentalmanagement.Models.Session;
import org.group6.childrenelectricvehiclerentalmanagement.Models.Ticket;
import org.group6.childrenelectricvehiclerentalmanagement.Models.TicketVehicleHistory;
import org.group6.childrenelectricvehiclerentalmanagement.Models.Vehicle;
import org.group6.childrenelectricvehiclerentalmanagement.Repositories.PaymentRepository;
import org.group6.childrenelectricvehiclerentalmanagement.Repositories.SessionVehicleRepository;
import org.group6.childrenelectricvehiclerentalmanagement.Repositories.SesssionRepository;
import org.group6.childrenelectricvehiclerentalmanagement.Repositories.TicketRepository;
import org.group6.childrenelectricvehiclerentalmanagement.Repositories.TicketVehicleHistoryRepository;
import org.group6.childrenelectricvehiclerentalmanagement.Repositories.UserRepository;
import org.group6.childrenelectricvehiclerentalmanagement.Repositories.VehicleRepository;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.BusinessTime;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.Security.AuthenticatedUser;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.Validators.AssignTicketRequestValidator;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.Validators.CancelTicketRequestValidator;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.TicketPricing;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.Validators.ChangeVehicleRequestValidator;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.Validators.ExtendTicketRequestValidator;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.Validators.ReturnTicketRequestValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class TicketActionService {

    private static final String ENTITY_TYPE = "ticket";

    private static final AssignTicketRequestValidator ASSIGN_VALIDATOR = new AssignTicketRequestValidator();
    private static final CancelTicketRequestValidator CANCEL_VALIDATOR = new CancelTicketRequestValidator();
    private static final ChangeVehicleRequestValidator CHANGE_VEHICLE_VALIDATOR = new ChangeVehicleRequestValidator();
    private static final ExtendTicketRequestValidator EXTEND_VALIDATOR = new ExtendTicketRequestValidator();
    private static final ReturnTicketRequestValidator RETURN_VALIDATOR = new ReturnTicketRequestValidator();

    private final SesssionRepository sessionRepository;
    private final TicketRepository ticketRepository;
    private final VehicleRepository vehicleRepository;
    private final SessionVehicleRepository sessionVehicleRepository;
    private final TicketVehicleHistoryRepository historyRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final SettingsService settingsService;
    private final AuditLogService auditLogService;
    private final BusinessTime businessTime;

    public TicketActionService(SesssionRepository sessionRepository,
                               TicketRepository ticketRepository,
                               VehicleRepository vehicleRepository,
                               SessionVehicleRepository sessionVehicleRepository,
                               TicketVehicleHistoryRepository historyRepository,
                               PaymentRepository paymentRepository,
                               UserRepository userRepository,
                               SettingsService settingsService,
                               AuditLogService auditLogService,
                               BusinessTime businessTime) {
        this.sessionRepository = sessionRepository;
        this.ticketRepository = ticketRepository;
        this.vehicleRepository = vehicleRepository;
        this.sessionVehicleRepository = sessionVehicleRepository;
        this.historyRepository = historyRepository;
        this.paymentRepository = paymentRepository;
        this.userRepository = userRepository;
        this.settingsService = settingsService;
        this.auditLogService = auditLogService;
        this.businessTime = businessTime;
    }

    @Transactional(rollbackFor = Exception.class)
    public TicketResponse assign(AuthenticatedUser actor, long ticketId, AssignTicketRequest request) throws AbstractException {
        ASSIGN_VALIDATOR.validate(request);
        String slug = request.vehicle_slug().trim();
        Session session = sessionRepository.findByStatusForUpdate(SessionStatus.InProgress).orElseThrow(NoActiveSessionException::new);
        Ticket ticket = ticketRepository.findByIdForUpdate(ticketId).orElseThrow(TicketNotExits::new);
        requireInSession(session, ticket);
        if (ticket.getStatus() != TicketStatus.Waiting) {
            throw new ConflictException("TICKET_NOT_WAITING", "ticket status is " + ticket.getStatus(),
                    "Chỉ gán xe được cho vé đang chờ");
        }
        Vehicle vehicle = vehicleRepository.findActiveBySlugsForUpdate(List.of(slug)).stream().findFirst()
                .orElseThrow(() -> new ValidationException("vehicle_slug", "Không tìm thấy xe: " + slug));
        if (!sessionVehicleRepository.existsBySessionIdAndVehicleIdAndRemovedAtIsNull(session.getId(), vehicle.getId())) {
            throw new ConflictException("VEHICLE_NOT_IN_SESSION", "vehicle " + slug + " is not in session " + session.getId(),
                    "Xe này không thuộc phiên đang làm");
        }
        if (vehicle.getStatus() != VehicleStatus.Online) {
            throw new ConflictException("VEHICLE_NOT_AVAILABLE", "vehicle status is " + vehicle.getStatus(),
                    "Xe không sẵn sàng, chỉ gán được xe đang ở trạng thái Online");
        }
        LocalDateTime now = businessTime.nowUtc();
        vehicle.setStatus(VehicleStatus.Inused);
        vehicleRepository.save(vehicle);
        ticket.setVehicle(vehicle);
        ticket.setStatus(TicketStatus.Inused);
        ticket.setStartedAt(now);
        ticket.setDueAt(now.plusMinutes(ticket.getTotalMinutes()));
        ticket.setUpdatedAt(now);
        Ticket saved = ticketRepository.save(ticket);
        historyRepository.save(new TicketVehicleHistory(saved.getId(), vehicle.getId(), now));
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("ticket_no", saved.getTicketNo());
        payload.put("vehicle", slug);
        payload.put("started_at", now.toString());
        payload.put("due_at", saved.getDueAt().toString());
        auditLogService.log(actor, "ticket.assign", ENTITY_TYPE, saved.getId(), payload);
        return TicketResponse.from(saved);
    }

    @Transactional(rollbackFor = Exception.class)
    public CancelTicketResponse cancel(AuthenticatedUser actor, long ticketId, CancelTicketRequest request) throws AbstractException {
        CANCEL_VALIDATOR.validate(request);
        Session session = sessionRepository.findByStatusForUpdate(SessionStatus.InProgress).orElseThrow(NoActiveSessionException::new);
        Ticket ticket = ticketRepository.findByIdForUpdate(ticketId).orElseThrow(TicketNotExits::new);
        requireInSession(session, ticket);
        if (ticket.getStatus() != TicketStatus.Waiting) {
            throw new ConflictException("TICKET_NOT_CANCELABLE", "ticket status is " + ticket.getStatus(),
                    notCancelableMessage(ticket.getStatus()));
        }
        PaymentMethod refundMethod = request == null || request.refund_method() == null
                ? ticket.getPaymentMethod()
                : PaymentMethod.valueOf(request.refund_method().trim());
        LocalDateTime now = businessTime.nowUtc();
        int refund = ticket.getAmountPaid();
        ticket.setStatus(TicketStatus.Canceled);
        ticket.setCanceledAt(now);
        ticket.setRefundAmount(refund);
        ticket.setUpdatedAt(now);
        Ticket saved = ticketRepository.save(ticket);
        Payment payment = new Payment();
        payment.setSessionId(session.getId());
        payment.setTicketId(saved.getId());
        payment.setType(PaymentType.Refund);
        payment.setMethod(refundMethod);
        payment.setAmount(refund);
        payment.setMinutes(saved.getTotalMinutes());
        payment.setCreatedBy(userRepository.getReferenceById(actor.id()));
        payment.setCreatedAt(now);
        paymentRepository.save(payment);
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("ticket_no", saved.getTicketNo());
        payload.put("refund_amount", refund);
        payload.put("refund_method", refundMethod.name());
        auditLogService.log(actor, "ticket.cancel", ENTITY_TYPE, saved.getId(), payload);
        return new CancelTicketResponse(saved.getTicketNo(), refund, refundMethod, TicketResponse.from(saved));
    }

    @Transactional(rollbackFor = Exception.class)
    public ChangeVehicleResponse changeVehicle(AuthenticatedUser actor, long ticketId, ChangeVehicleRequest request) throws AbstractException {
        CHANGE_VEHICLE_VALIDATOR.validate(request);
        String slug = request.vehicle_slug().trim();
        ReleasedVehicleStatus oldStatus = ReleasedVehicleStatus.valueOf(request.old_vehicle_status().trim());
        Session session = sessionRepository.findByStatusForUpdate(SessionStatus.InProgress).orElseThrow(NoActiveSessionException::new);
        Ticket ticket = ticketRepository.findByIdForUpdate(ticketId).orElseThrow(TicketNotExits::new);
        requireInSession(session, ticket);
        if (ticket.getStatus() != TicketStatus.Inused && ticket.getStatus() != TicketStatus.Overdue) {
            throw new ConflictException("TICKET_NOT_IN_USE", "ticket status is " + ticket.getStatus(),
                    notChangeableMessage(ticket.getStatus()));
        }
        Vehicle current = ticket.getVehicle();
        if (current == null) {
            throw new ConflictException("TICKET_NOT_IN_USE", "ticket " + ticketId + " has no vehicle", "Vé chưa được gán xe");
        }
        Vehicle replacement = vehicleRepository.findActiveBySlugsForUpdate(List.of(slug)).stream().findFirst()
                .orElseThrow(() -> new ValidationException("vehicle_slug", "Không tìm thấy xe: " + slug));
        if (replacement.getId().equals(current.getId())) {
            throw new ConflictException("VEHICLE_UNCHANGED", "ticket already uses vehicle " + slug, "Vé đang dùng chính xe này");
        }
        if (!sessionVehicleRepository.existsBySessionIdAndVehicleIdAndRemovedAtIsNull(session.getId(), replacement.getId())) {
            throw new ConflictException("VEHICLE_NOT_IN_SESSION", "vehicle " + slug + " is not in session " + session.getId(),
                    "Xe này không thuộc phiên đang làm");
        }
        if (replacement.getStatus() != VehicleStatus.Online) {
            throw new ConflictException("VEHICLE_NOT_AVAILABLE", "vehicle status is " + replacement.getStatus(),
                    "Xe không sẵn sàng, chỉ đổi sang được xe đang ở trạng thái Online");
        }
        Vehicle previous = vehicleRepository.findByIdForUpdate(current.getId()).orElseThrow(VehicleNotExits::new);
        LocalDateTime now = businessTime.nowUtc();
        previous.setStatus(VehicleStatus.valueOf(oldStatus.name()));
        replacement.setStatus(VehicleStatus.Inused);
        vehicleRepository.saveAll(List.of(previous, replacement));
        ticket.setVehicle(replacement);
        ticket.setUpdatedAt(now);
        Ticket saved = ticketRepository.save(ticket);
        TicketVehicleHistory released = historyRepository
                .findFirstByTicketIdAndVehicleIdAndReleasedAtIsNullOrderByIdDesc(saved.getId(), previous.getId())
                .orElseGet(() -> new TicketVehicleHistory(saved.getId(), previous.getId(),
                        saved.getStartedAt() != null ? saved.getStartedAt() : now));
        released.setReleasedAt(now);
        released.setReleasedStatus(oldStatus);
        released.setUpdatedAt(now);
        historyRepository.save(released);
        historyRepository.save(new TicketVehicleHistory(saved.getId(), replacement.getId(), now));
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("ticket_no", saved.getTicketNo());
        payload.put("old_vehicle", previous.getSlug());
        payload.put("old_vehicle_status", oldStatus.name());
        payload.put("new_vehicle", replacement.getSlug());
        auditLogService.log(actor, "ticket.change_vehicle", ENTITY_TYPE, saved.getId(), payload);
        return new ChangeVehicleResponse(TicketResponse.from(saved), TicketVehicleResponse.from(previous));
    }

    @Transactional(rollbackFor = Exception.class)
    public ExtendTicketResponse extend(AuthenticatedUser actor, long ticketId, ExtendTicketRequest request) throws AbstractException {
        EXTEND_VALIDATOR.validate(request);
        int minutes = request.minutes();
        PaymentMethod method = PaymentMethod.valueOf(request.payment_method().trim());
        Session session = sessionRepository.findByStatusForUpdate(SessionStatus.InProgress).orElseThrow(NoActiveSessionException::new);
        Ticket ticket = ticketRepository.findByIdForUpdate(ticketId).orElseThrow(TicketNotExits::new);
        requireInSession(session, ticket);
        requireRunning(ticket);
        if (!settingsService.getSettings().isExtendEnabled()) {
            throw new ConflictException("EXTEND_DISABLED", "extend is disabled in settings", "Chức năng gia hạn đang tắt trong cài đặt");
        }
        int amount = TicketPricing.charge(minutes, ticket.getPriceAmount(), ticket.getPriceMinutes());
        long newTotalMinutes = (long) ticket.getTotalMinutes() + minutes;
        long newAmountPaid = (long) ticket.getAmountPaid() + amount;
        if (newTotalMinutes > Integer.MAX_VALUE || newAmountPaid > Integer.MAX_VALUE) {
            throw new ConflictException("PRICE_TOO_LARGE", "extension overflow", "Số tiền hoặc thời gian vượt giới hạn cho phép");
        }
        LocalDateTime now = businessTime.nowUtc();
        LocalDateTime newDueAt = ticket.getDueAt().plusMinutes(minutes);
        ticket.setTotalMinutes((int) newTotalMinutes);
        ticket.setAmountPaid((int) newAmountPaid);
        ticket.setDueAt(newDueAt);
        ticket.setStatus(newDueAt.isAfter(now) ? TicketStatus.Inused : TicketStatus.Overdue);
        ticket.setUpdatedAt(now);
        Ticket saved = ticketRepository.save(ticket);
        savePayment(actor, session, saved, PaymentType.Extension, method, amount, minutes, now);
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("ticket_no", saved.getTicketNo());
        payload.put("extended_minutes", minutes);
        payload.put("amount", amount);
        payload.put("payment_method", method.name());
        payload.put("due_at", newDueAt.toString());
        auditLogService.log(actor, "ticket.extend", ENTITY_TYPE, saved.getId(), payload);
        return new ExtendTicketResponse(saved.getTicketNo(), minutes, amount, method, TicketResponse.from(saved));
    }

    @Transactional(rollbackFor = Exception.class)
    public ReturnTicketResponse returnVehicle(AuthenticatedUser actor, long ticketId, ReturnTicketRequest request) throws AbstractException {
        RETURN_VALIDATOR.validate(request);
        Session session = sessionRepository.findByStatusForUpdate(SessionStatus.InProgress).orElseThrow(NoActiveSessionException::new);
        Ticket ticket = ticketRepository.findByIdForUpdate(ticketId).orElseThrow(TicketNotExits::new);
        requireInSession(session, ticket);
        requireRunning(ticket);
        Vehicle vehicle = vehicleRepository.findByIdForUpdate(ticket.getVehicle().getId()).orElseThrow(VehicleNotExits::new);
        LocalDateTime now = businessTime.nowUtc();
        long lateSeconds = Duration.between(ticket.getDueAt(), now).getSeconds();

        int overdueMinutes = 0;
        int chargeableMinutes = 0;
        int overdueFee = 0;
        int refund = 0;
        int remainingMinutes = 0;
        if (lateSeconds > 0) {
            overdueMinutes = (int) Math.min((lateSeconds + 59) / 60, Integer.MAX_VALUE);
            chargeableMinutes = Math.max(0, overdueMinutes - ticket.getOverdueGraceMinutes());
            long fee = (long) chargeableMinutes * ticket.getOverdueFeePerMinute();
            if (fee > Integer.MAX_VALUE) {
                throw new ConflictException("PRICE_TOO_LARGE", "overdue fee overflow", "Số tiền vượt giới hạn cho phép");
            }
            overdueFee = (int) fee;
        } else if (lateSeconds < 0 && Boolean.TRUE.equals(ticket.getRefundEnabled())) {
            remainingMinutes = (int) Math.min((-lateSeconds) / 60, Integer.MAX_VALUE);
            refund = TicketPricing.proportionalRefund(ticket.getAmountPaid(), ticket.getTotalMinutes(), remainingMinutes);
        }

        PaymentMethod feeMethod = null;
        if (overdueFee > 0) {
            feeMethod = request == null || request.payment_method() == null
                    ? ticket.getPaymentMethod()
                    : PaymentMethod.valueOf(request.payment_method().trim());
        }
        PaymentMethod refundMethod = null;
        if (refund > 0) {
            refundMethod = request == null || request.refund_method() == null
                    ? ticket.getPaymentMethod()
                    : PaymentMethod.valueOf(request.refund_method().trim());
        }

        vehicle.setStatus(VehicleStatus.Online);
        vehicleRepository.save(vehicle);
        ticket.setStatus(TicketStatus.Completed);
        ticket.setReturnedAt(now);
        ticket.setOverdueMinutes(overdueMinutes);
        ticket.setOverdueFee(overdueFee);
        ticket.setRefundAmount(refund);
        ticket.setUpdatedAt(now);
        Ticket saved = ticketRepository.save(ticket);

        TicketVehicleHistory released = historyRepository
                .findFirstByTicketIdAndVehicleIdAndReleasedAtIsNullOrderByIdDesc(saved.getId(), vehicle.getId())
                .orElseGet(() -> new TicketVehicleHistory(saved.getId(), vehicle.getId(),
                        saved.getStartedAt() != null ? saved.getStartedAt() : now));
        released.setReleasedAt(now);
        released.setReleasedStatus(ReleasedVehicleStatus.Online);
        released.setUpdatedAt(now);
        historyRepository.save(released);

        if (overdueFee > 0) {
            savePayment(actor, session, saved, PaymentType.OverdueFee, feeMethod, overdueFee, chargeableMinutes, now);
        }
        if (refund > 0) {
            savePayment(actor, session, saved, PaymentType.Refund, refundMethod, refund, remainingMinutes, now);
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("ticket_no", saved.getTicketNo());
        payload.put("vehicle", vehicle.getSlug());
        payload.put("overdue_minutes", overdueMinutes);
        payload.put("chargeable_overdue_minutes", chargeableMinutes);
        payload.put("overdue_fee", overdueFee);
        payload.put("refund_amount", refund);
        auditLogService.log(actor, "ticket.return", ENTITY_TYPE, saved.getId(), payload);

        Ticket next = ticketRepository
                .findFirstBySessionIdAndStatusOrderByQueuedAtAscIdAsc(session.getId(), TicketStatus.Waiting)
                .orElse(null);
        return new ReturnTicketResponse(saved.getTicketNo(), saved.getStatus(), overdueMinutes, chargeableMinutes,
                overdueFee, feeMethod, refund, refundMethod, TicketVehicleResponse.from(vehicle),
                NextTicketResponse.from(next), TicketResponse.from(saved));
    }

    private void savePayment(AuthenticatedUser actor, Session session, Ticket ticket, PaymentType type,
                             PaymentMethod method, int amount, int minutes, LocalDateTime now) {
        Payment payment = new Payment();
        payment.setSessionId(session.getId());
        payment.setTicketId(ticket.getId());
        payment.setType(type);
        payment.setMethod(method);
        payment.setAmount(amount);
        payment.setMinutes(minutes);
        payment.setCreatedBy(userRepository.getReferenceById(actor.id()));
        payment.setCreatedAt(now);
        paymentRepository.save(payment);
    }

    private void requireRunning(Ticket ticket) throws ConflictException {
        boolean running = ticket.getStatus() == TicketStatus.Inused || ticket.getStatus() == TicketStatus.Overdue;
        if (!running || ticket.getVehicle() == null || ticket.getDueAt() == null) {
            throw new ConflictException("TICKET_NOT_IN_USE", "ticket status is " + ticket.getStatus(),
                    notRunningMessage(ticket.getStatus()));
        }
    }

    private String notRunningMessage(TicketStatus status) {
        return switch (status) {
            case Waiting -> "Vé chưa được gán xe";
            case Canceled -> "Vé này đã được hủy";
            case Completed -> "Vé này đã hoàn tất";
            case Inused, Overdue -> "Vé chưa có xe hoặc chưa có giờ kết thúc";
        };
    }

    private void requireInSession(Session session, Ticket ticket) throws ConflictException {
        if (!session.getId().equals(ticket.getSessionId())) {
            throw new ConflictException("TICKET_NOT_IN_ACTIVE_SESSION", "ticket belongs to session " + ticket.getSessionId(),
                    "Vé này không thuộc phiên đang làm");
        }
    }

    private String notChangeableMessage(TicketStatus status) {
        return switch (status) {
            case Waiting -> "Vé chưa được gán xe, hãy gán xe thay vì đổi xe";
            case Canceled -> "Vé này đã được hủy";
            case Completed -> "Vé này đã hoàn tất";
            case Inused, Overdue -> "Vé đang được sử dụng";
        };
    }

    private String notCancelableMessage(TicketStatus status) {
        return switch (status) {
            case Inused, Overdue -> "Vé đang được sử dụng, không thể hủy";
            case Canceled -> "Vé này đã được hủy";
            case Completed -> "Vé này đã hoàn tất, không thể hủy";
            case Waiting -> "Vé đang chờ";
        };
    }
}
