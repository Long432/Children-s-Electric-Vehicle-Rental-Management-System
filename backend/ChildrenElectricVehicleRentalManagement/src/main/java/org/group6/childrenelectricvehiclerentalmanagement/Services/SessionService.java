package org.group6.childrenelectricvehiclerentalmanagement.Services;

import org.group6.childrenelectricvehiclerentalmanagement.DTOs.PageResponse;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Session.CreateSessionRequest;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Session.SessionDetailResponse;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Session.SessionListQuery;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Session.SessionResponse;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Session.StartSessionRequest;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Session.UpdateSessionRequest;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Vehicle.VehicleResponse;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.SessionStatus;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.TicketStatus;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.VehicleStatus;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.AbstractException;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.ConflictException;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.NoActiveSessionException;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.SessionAlreadyInProgressException;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.SessionNotExits;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.ValidationException;
import org.group6.childrenelectricvehiclerentalmanagement.Models.Session;
import org.group6.childrenelectricvehiclerentalmanagement.Models.SessionVehicle;
import org.group6.childrenelectricvehiclerentalmanagement.Models.Vehicle;
import org.group6.childrenelectricvehiclerentalmanagement.Repositories.SessionVehicleRepository;
import org.group6.childrenelectricvehiclerentalmanagement.Repositories.SesssionRepository;
import org.group6.childrenelectricvehiclerentalmanagement.Repositories.TicketRepository;
import org.group6.childrenelectricvehiclerentalmanagement.Repositories.UserRepository;
import org.group6.childrenelectricvehiclerentalmanagement.Repositories.VehicleRepository;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.BusinessTime;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.SessionTimeRules;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.Security.AuthenticatedUser;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.Validators.CreateSessionRequestValidator;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.Validators.PageRules;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.Validators.SessionListQueryValidator;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.Validators.StartSessionRequestValidator;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.Validators.UpdateSessionRequestValidator;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class SessionService {

    private static final Set<String> IN_PROGRESS_INDEXES = Set.of("uq_sessions_one_in_progress", "uq_sessions_single_in_progress");
    private static final String UNIQUE_VIOLATION = "23505";
    private static final int MAX_CAUSE_DEPTH = 16;
    private static final String ENTITY_TYPE = "session";
    private static final List<TicketStatus> OPEN_TICKET_STATUSES = List.of(TicketStatus.Waiting, TicketStatus.Inused, TicketStatus.Overdue);

    private static final CreateSessionRequestValidator CREATE_VALIDATOR = new CreateSessionRequestValidator();
    private static final UpdateSessionRequestValidator UPDATE_VALIDATOR = new UpdateSessionRequestValidator();
    private static final StartSessionRequestValidator START_VALIDATOR = new StartSessionRequestValidator();
    private static final SessionListQueryValidator LIST_VALIDATOR = new SessionListQueryValidator();

    private final SesssionRepository sessionRepository;
    private final VehicleRepository vehicleRepository;
    private final SessionVehicleRepository sessionVehicleRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;
    private final BusinessTime businessTime;

    public SessionService(SesssionRepository sessionRepository,
                          VehicleRepository vehicleRepository,
                          SessionVehicleRepository sessionVehicleRepository,
                          TicketRepository ticketRepository,
                          UserRepository userRepository,
                          AuditLogService auditLogService,
                          BusinessTime businessTime) {
        this.sessionRepository = sessionRepository;
        this.vehicleRepository = vehicleRepository;
        this.sessionVehicleRepository = sessionVehicleRepository;
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
        this.businessTime = businessTime;
    }

    @Transactional(rollbackFor = Exception.class)
    public SessionResponse create(AuthenticatedUser actor, CreateSessionRequest request) throws AbstractException {
        CREATE_VALIDATOR.validate(request);
        LocalDate date = SessionTimeRules.parseDate("date", request.date());
        LocalTime time = SessionTimeRules.parseTime("time", request.time());
        requireNotPast(date);
        Session session = newSession(actor, date, time, false);
        Session saved = sessionRepository.save(session);
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("scheduled_date", date.toString());
        payload.put("scheduled_time", time.toString());
        auditLogService.log(actor, "session.create", ENTITY_TYPE, saved.getId(), payload);
        return SessionResponse.from(saved);
    }

    @Transactional(rollbackFor = Exception.class)
    public SessionDetailResponse start(AuthenticatedUser actor, StartSessionRequest request) throws AbstractException {
        START_VALIDATOR.validate(request);
        if (sessionRepository.existsByStatus(SessionStatus.InProgress)) {
            throw new SessionAlreadyInProgressException();
        }
        List<String> slugs = request.vehicle_slugs().stream().map(String::trim).toList();
        LocalDateTime now = businessTime.nowUtc();
        LocalDateTime plannedEnd = plannedEnd(request.end_time());
        Session session;
        if (request.session_id() != null) {
            session = sessionRepository.findByIdForUpdate(request.session_id()).orElseThrow(SessionNotExits::new);
            if (session.getStatus() != SessionStatus.Waiting) {
                throw new ConflictException("SESSION_NOT_STARTABLE", "session is not waiting", "Chỉ có thể bắt đầu phiên đang ở trạng thái chờ");
            }
        } else {
            session = newSession(actor, businessTime.today(), businessTime.nowLocalTime(), true);
        }
        List<Vehicle> vehicles = lockAvailableVehicles(slugs);
        session.setStatus(SessionStatus.InProgress);
        session.setStartedAt(now);
        session.setPlannedEndAt(plannedEnd);
        session.setUpdatedAt(now);
        Session saved = saveChecked(session);
        for (Vehicle vehicle : vehicles) {
            if (vehicle.getStatus() != VehicleStatus.Online) {
                vehicle.setStatus(VehicleStatus.Online);
            }
            sessionVehicleRepository.save(new SessionVehicle(saved.getId(), vehicle.getId(), now));
        }
        vehicleRepository.saveAll(vehicles);
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("is_instant", saved.getIsInstant());
        payload.put("vehicles", slugs);
        payload.put("planned_end_at", plannedEnd.toString());
        auditLogService.log(actor, "session.start", ENTITY_TYPE, saved.getId(), payload);
        return detail(saved);
    }

    @Transactional(rollbackFor = Exception.class)
    public SessionResponse end(AuthenticatedUser actor) throws AbstractException {
        Session session = sessionRepository.findByStatusForUpdate(SessionStatus.InProgress).orElseThrow(NoActiveSessionException::new);
        long open = ticketRepository.countBySessionIdAndStatusIn(session.getId(), OPEN_TICKET_STATUSES);
        if (open > 0) {
            throw new ConflictException("SESSION_HAS_OPEN_TICKETS", "open tickets: " + open, "Còn " + open + " vé chưa hoàn tất, không thể kết thúc phiên");
        }
        LocalDateTime now = businessTime.nowUtc();
        List<Vehicle> vehicles = vehicleRepository.findActiveBySessionIdForUpdate(session.getId(), VehicleStatus.Deleted);
        List<String> released = new ArrayList<>();
        for (Vehicle vehicle : vehicles) {
            if (vehicle.getStatus() == VehicleStatus.Online) {
                vehicle.setStatus(VehicleStatus.Offline);
                released.add(vehicle.getSlug());
            }
        }
        vehicleRepository.saveAll(vehicles);
        session.setStatus(SessionStatus.Ended);
        session.setEndedAt(now);
        session.setUpdatedAt(now);
        Session saved = sessionRepository.save(session);
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("released_vehicles", released);
        auditLogService.log(actor, "session.end", ENTITY_TYPE, saved.getId(), payload);
        return SessionResponse.from(saved);
    }

    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public SessionDetailResponse current() throws AbstractException {
        Session session = sessionRepository.findFirstByStatus(SessionStatus.InProgress).orElseThrow(NoActiveSessionException::new);
        return detail(session);
    }

    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public SessionDetailResponse get(long id) throws AbstractException {
        Session session = sessionRepository.findById(id).orElseThrow(SessionNotExits::new);
        return detail(session);
    }

    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public PageResponse<SessionResponse> list(SessionListQuery query) throws AbstractException {
        LIST_VALIDATOR.validate(query);
        int page = PageRules.page(query.page());
        int limit = PageRules.limit(query.limit());
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.DESC, "id"));
        Page<Session> result = (query.status() == null || query.status().isBlank())
                ? sessionRepository.findAll(pageable)
                : sessionRepository.findByStatus(SessionStatus.valueOf(query.status().trim()), pageable);
        List<SessionResponse> items = result.getContent().stream().map(SessionResponse::from).toList();
        return new PageResponse<>(items, page, limit, result.getTotalElements());
    }

    @Transactional(rollbackFor = Exception.class)
    public SessionResponse update(AuthenticatedUser actor, long id, UpdateSessionRequest request) throws AbstractException {
        UPDATE_VALIDATOR.validate(request);
        Session session = sessionRepository.findByIdForUpdate(id).orElseThrow(SessionNotExits::new);
        requireWaiting(session);
        if (request.date() != null) {
            LocalDate date = SessionTimeRules.parseDate("date", request.date());
            requireNotPast(date);
            session.setScheduledDate(date);
        }
        if (request.time() != null) {
            session.setScheduledTime(SessionTimeRules.parseTime("time", request.time()));
        }
        session.setUpdatedAt(businessTime.nowUtc());
        Session saved = sessionRepository.save(session);
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("scheduled_date", saved.getScheduledDate().toString());
        payload.put("scheduled_time", saved.getScheduledTime().toString());
        auditLogService.log(actor, "session.update", ENTITY_TYPE, saved.getId(), payload);
        return SessionResponse.from(saved);
    }

    @Transactional(rollbackFor = Exception.class)
    public SessionResponse cancel(AuthenticatedUser actor, long id) throws AbstractException {
        Session session = sessionRepository.findByIdForUpdate(id).orElseThrow(SessionNotExits::new);
        requireWaiting(session);
        session.setStatus(SessionStatus.Canceled);
        session.setUpdatedAt(businessTime.nowUtc());
        Session saved = sessionRepository.save(session);
        auditLogService.log(actor, "session.cancel", ENTITY_TYPE, saved.getId(), new LinkedHashMap<>());
        return SessionResponse.from(saved);
    }

    private Session newSession(AuthenticatedUser actor, LocalDate date, LocalTime time, boolean instant) {
        LocalDateTime now = businessTime.nowUtc();
        Session session = new Session();
        session.setStatus(SessionStatus.Waiting);
        session.setScheduledDate(date);
        session.setScheduledTime(time);
        session.setIsInstant(instant);
        session.setNextTicketNo(1);
        session.setCreatedBy(userRepository.getReferenceById(actor.id()));
        session.setCreatedAt(now);
        session.setUpdatedAt(now);
        return session;
    }

    private LocalDateTime plannedEnd(String endTime) throws ValidationException {
        if (endTime == null || endTime.isBlank()) {
            return businessTime.defaultSessionEndUtc();
        }
        return businessTime.nextOccurrenceUtc(SessionTimeRules.parseTime("end_time", endTime));
    }

    private void requireNotPast(LocalDate date) throws ValidationException {
        if (date.isBefore(businessTime.today())) {
            throw new ValidationException("date", "Ngày của phiên không được ở quá khứ");
        }
    }

    private void requireWaiting(Session session) throws ConflictException {
        if (session.getStatus() != SessionStatus.Waiting) {
            throw new ConflictException("SESSION_NOT_EDITABLE", "session is not waiting", "Chỉ sửa hoặc hủy được phiên đang ở trạng thái chờ");
        }
    }

    private List<Vehicle> lockAvailableVehicles(List<String> slugs) throws AbstractException {
        List<Vehicle> found = vehicleRepository.findActiveBySlugsForUpdate(slugs);
        Set<String> foundSlugs = new LinkedHashSet<>();
        for (Vehicle vehicle : found) {
            foundSlugs.add(vehicle.getSlug());
        }
        List<String> missing = slugs.stream().filter(slug -> !foundSlugs.contains(slug)).toList();
        if (!missing.isEmpty()) {
            throw new ValidationException("vehicle_slugs", "Không tìm thấy xe: " + String.join(", ", missing));
        }
        List<String> unavailable = found.stream()
                .filter(vehicle -> vehicle.getStatus() != VehicleStatus.Online && vehicle.getStatus() != VehicleStatus.Offline)
                .map(Vehicle::getSlug)
                .toList();
        if (!unavailable.isEmpty()) {
            throw new ConflictException("VEHICLE_NOT_AVAILABLE", "vehicles not available: " + unavailable,
                    "Xe không sẵn sàng để bắt đầu phiên: " + String.join(", ", unavailable));
        }
        return found;
    }

    private SessionDetailResponse detail(Session session) {
        List<VehicleResponse> vehicles = vehicleRepository.findActiveBySessionId(session.getId()).stream().map(VehicleResponse::from).toList();
        Map<String, Long> counts = new LinkedHashMap<>();
        for (TicketStatus status : TicketStatus.values()) {
            counts.put(status.name(), 0L);
        }
        for (Object[] row : ticketRepository.countGroupedByStatus(session.getId())) {
            counts.put(String.valueOf(row[0]), ((Number) row[1]).longValue());
        }
        return SessionDetailResponse.from(session, vehicles, counts);
    }

    private Session saveChecked(Session session) throws AbstractException {
        try {
            return sessionRepository.saveAndFlush(session);
        } catch (DataIntegrityViolationException e) {
            if (violatesInProgressIndex(e)) {
                throw new SessionAlreadyInProgressException();
            }
            throw e;
        }
    }

    private boolean violatesInProgressIndex(Throwable error) {
        Throwable current = error;
        for (int depth = 0; current != null && depth < MAX_CAUSE_DEPTH; depth++) {
            if (current instanceof ConstraintViolationException violation && violation.getConstraintName() != null
                    && IN_PROGRESS_INDEXES.contains(violation.getConstraintName().toLowerCase(Locale.ROOT))) {
                return true;
            }
            if (current instanceof SQLException sql && UNIQUE_VIOLATION.equals(sql.getSQLState()) && sql.getMessage() != null) {
                String message = sql.getMessage().toLowerCase(Locale.ROOT);
                if (IN_PROGRESS_INDEXES.stream().anyMatch(message::contains)) {
                    return true;
                }
            }
            current = current.getCause() == current ? null : current.getCause();
        }
        return false;
    }
}
