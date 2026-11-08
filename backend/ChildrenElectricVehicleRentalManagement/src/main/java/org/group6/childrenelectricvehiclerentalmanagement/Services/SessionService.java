package org.group6.childrenelectricvehiclerentalmanagement.Services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Long.Requests;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Long.Responses;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.SessionController.AddSessionVehiclesRequest;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.SessionController.CreateSessionRequest;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.SessionController.SessionBasicResponse;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.SessionController.SessionDetailResponse;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.SessionController.SessionPageResponse;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.SessionController.StartSessionRequest;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.SessionController.UpdateSessionRequest;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.VehicleController.vehicleInfoRespond;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.*;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.*;
import org.group6.childrenelectricvehiclerentalmanagement.Models.*;
import org.group6.childrenelectricvehiclerentalmanagement.Models.LinkingObject.SessionVehicle;
import org.group6.childrenelectricvehiclerentalmanagement.Repositories.*;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.Validators.IdValidator;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.TimeUtil;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Slf4j
@Service
@AllArgsConstructor
public class SessionService {

    private static final List<SessionStatus> ACTIVE = List.of(SessionStatus.Waiting, SessionStatus.InProgress);
    private static final int DEFAULT_PAGE_LIMIT = 20;
    private static final int MAX_PAGE_LIMIT = 100;

    private final SessionRepository sessionRepository;
    private final TicketRepository ticketRepository;
    private final PaymentRepository paymentRepository;
    private final SessionVehicleRepository sessionVehicleRepository;
    private final TicketService ticketService;
    private final VehicleRepository vehicleRepository;
    private final AuditLogService auditLogService;

    // ======================================================================
    // Tạo phiên
    // ======================================================================

    /**
     * POST /sessions. Phiên thường: cần đủ start_date, start_time, end_date, end_time.
     * Phiên instant: bắt đầu ngay, end_* không bắt buộc (mặc định 00:00 hôm sau).
     */
    @Transactional(rollbackFor = Exception.class)
    public SessionDetailResponse createSession(CreateSessionRequest request, User actor) throws Exception {
        requireActor(actor);
        if (request == null) throw new ValidationException("body", "Thiếu dữ liệu tạo phiên");

        boolean instant = Boolean.TRUE.equals(request.is_instant());
        LocalDateTime now = now();

        LocalDateTime start;
        LocalDateTime end;

        if (instant) {
            start = now;
            if (request.end_date() == null && request.end_time() == null) {
                end = defaultEnd(now);
            } else {
                end = LocalDateTime.of(
                        required(request.end_date(), "end_date"),
                        required(request.end_time(), "end_time"));
            }
        } else {
            start = LocalDateTime.of(
                    required(request.start_date(), "start_date"),
                    required(request.start_time(), "start_time"));
            end = LocalDateTime.of(
                    required(request.end_date(), "end_date"),
                    required(request.end_time(), "end_time"));

            if (start.isBefore(now.truncatedTo(ChronoUnit.MINUTES)))
                throw new ValidationException("start_date", "Thời điểm bắt đầu phải từ hiện tại trở đi");
        }

        Session session = persistNewSession(start, end, instant, actor);
        audit(actor, "session.create", session, null, snapshot(session));
        return toDetail(session);
    }

    // ======================================================================
    // Bắt đầu phiên
    // ======================================================================

    /**
     * POST /sessions/start.
     * Có session_id: Waiting -> InProgress. Không có: tạo phiên instant InProgress.
     * Các xe trong vehicle_slugs được đưa vào phiên và chuyển Online.
     */
    @Transactional(rollbackFor = Exception.class)
    public SessionDetailResponse startSession(StartSessionRequest request, User actor) throws Exception {
        requireActor(actor);
        if (request == null) throw new ValidationException("body", "Thiếu dữ liệu bắt đầu phiên");

        List<String> slugs = normalizeSlugs(request.vehicle_slugs());
        LocalDateTime now = now();
        Session session;

        if (request.session_id() == null) {
            LocalDateTime end = request.planned_end_at() != null ? request.planned_end_at() : defaultEnd(now);
            session = persistNewSession(now, end, true, actor);
            audit(actor, "session.create", session, null, snapshot(session));
        } else {
            session = sessionRepository.findById(request.session_id()).orElseThrow(SessionNotFound::new);

            switch (session.getStatus()) {
                case Ended -> throw new AbstractException("session already ended", "SESSION_ENDED",
                        "Phiên này đã hoàn thành từ trước", 409);
                case Canceled -> throw new AbstractException("can not start a canceled session", "SESSION_CANCELED",
                        "Phiên này đã bị hủy", 409);
                case InProgress -> throw new AbstractException("session already in progress", "SESSION_INPROGRESS",
                        "Phiên này đang được thực hiện", 409);
                default -> { }
            }

            LocalDateTime scheduled = LocalDateTime.of(session.getScheduledDate(), session.getScheduledTime());
            if (now.isBefore(scheduled))
                throw new AbstractException("session started earlier than its scheduled time",
                        "START_SESSION_EARLY", "Chưa tới giờ bắt đầu", 409);

            if (sessionRepository.existsByStatus(SessionStatus.InProgress)) throw new SessionAlreadyInProgress();

            Map<String, Object> before = snapshot(session);
            session.setStatus(SessionStatus.InProgress);
            session.setStartedAt(now);
            try {
                session = sessionRepository.saveAndFlush(session);
            } catch (DataIntegrityViolationException e) { // unique index: chỉ một phiên InProgress
                throw new SessionAlreadyInProgress();
            }
            audit(actor, "session.start", session, before, snapshot(session));
        }

        attachVehicles(session, slugs);
        return toDetail(session);
    }

    // ======================================================================
    // Kết thúc phiên hiện tại
    // ======================================================================

    @Transactional(rollbackFor = Exception.class)
    public SessionDetailResponse endCurrentSession(User actor) throws Exception {
        requireActor(actor);
        Session target = currentSessionLocked();

        if (ticketRepository.existsBySession_IdAndStatusIn(target.getId(),
                List.of(TicketStatus.Waiting, TicketStatus.Inused, TicketStatus.Overdue)))
            throw new SessionHasOpenTickets();

        List<SessionVehicle> sessionVehicles = sessionVehicleRepository.findBySessionIdAndRemovedAtIsNull(target.getId());

        // Kiểm tra hết trước khi đổi bất cứ thứ gì
        for (SessionVehicle sv : sessionVehicles) {
            if (sv.getVehicle().getStatus() == VehicleStatus.Inused)
                throw new VehicleInUse(sv.getVehicle().getSlug());
        }
        for (SessionVehicle sv : sessionVehicles) {
            Vehicle v = sv.getVehicle();
            if (v.getStatus() == VehicleStatus.Online) v.setStatus(VehicleStatus.Offline);
        }

        Map<String, Object> before = snapshot(target);
        target.setStatus(SessionStatus.Ended);
        target.setEndedAt(now());
        Session saved = sessionRepository.save(target);

        audit(actor, "session.end", saved, before, snapshot(saved));
        return toDetail(saved);
    }

    // ======================================================================
    // Đọc
    // ======================================================================

    @Transactional(readOnly = true)
    public SessionDetailResponse getCurrentSession() throws Exception {
        return toDetail(currentSession());
    }

    @Transactional(readOnly = true)
    public SessionDetailResponse getSessionDetail(Long id) throws Exception {
        if (id == null) throw new ValidationException("id", "id phiên không được để trống");
        return toDetail(sessionRepository.findById(id).orElseThrow(SessionNotFound::new));
    }

    /** GET /sessions?status=&page=&limit= (mới nhất trước). */
    @Transactional(readOnly = true)
    public SessionPageResponse listSessions(String rawStatus, Integer rawPage, Integer rawLimit) throws Exception {
        int page = rawPage == null ? 1 : rawPage;
        int limit = rawLimit == null ? DEFAULT_PAGE_LIMIT : rawLimit;
        if (page < 1) throw new ValidationException("page", "page phải từ 1 trở lên");
        if (limit < 1 || limit > MAX_PAGE_LIMIT)
            throw new ValidationException("limit", "limit phải nằm trong khoảng 1 đến " + MAX_PAGE_LIMIT);

        SessionStatus status = null;
        if (rawStatus != null && !rawStatus.isBlank()) {
            try {
                status = SessionStatus.valueOf(rawStatus.trim());
            } catch (IllegalArgumentException e) {
                throw new ValidationException("status", "status chỉ nhận Waiting, InProgress, Ended hoặc Canceled");
            }
        }

        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.DESC, "id"));
        Page<Session> result = status == null
                ? sessionRepository.findAll(pageable)
                : sessionRepository.findByStatus(status, pageable);

        List<SessionBasicResponse> items = new ArrayList<>();
        for (Session s : result.getContent()) items.add(new SessionBasicResponse(s));
        return new SessionPageResponse(items, page, limit, result.getTotalElements(), result.getTotalPages());
    }

    // ======================================================================
    // Sửa phiên (chỉ Waiting) - nhận nguyên request
    // ======================================================================

    @Transactional(rollbackFor = Exception.class)
    public SessionDetailResponse updateSession(Long id, UpdateSessionRequest request, User actor) throws Exception {
        requireActor(actor);
        if (id == null) throw new ValidationException("id", "id phiên không được để trống");
        if (request == null) throw new ValidationException("body", "Thiếu dữ liệu cập nhật phiên");

        Session session = sessionRepository.findById(id).orElseThrow(SessionNotFound::new);
        if (session.getStatus() != SessionStatus.Waiting) throw new SessionNotEditable();

        boolean instant = required(request.is_instant(), "is_instant");
        if (instant)
            throw new ValidationException("is_instant",
                    "Phiên đã lên lịch không thể chuyển thành phiên instant, hãy bắt đầu phiên bằng API start");

        LocalDate date = required(request.scheduled_date(), "scheduled_date");
        LocalTime time = required(request.scheduled_time(), "scheduled_time");
        LocalDateTime newEnd = required(request.planned_end_at(), "planned_end_at");

        LocalDateTime oldStart = LocalDateTime.of(session.getScheduledDate(), session.getScheduledTime());
        LocalDateTime newStart = LocalDateTime.of(date, time);

        // Chỉ cấm quá khứ khi người dùng thực sự dời giờ bắt đầu
        if (!newStart.equals(oldStart) && newStart.isBefore(now().truncatedTo(ChronoUnit.MINUTES)))
            throw new ValidationException("scheduled_date", "Thời điểm bắt đầu phải từ hiện tại trở đi");
        if (!newStart.isBefore(newEnd))
            throw new ValidationException("planned_end_at", "Thời điểm kết thúc phải muộn hơn thời điểm bắt đầu");

        assertNoCollision(newStart, newEnd, session.getId());

        Map<String, Object> before = snapshot(session);
        session.setScheduledDate(date);
        session.setScheduledTime(time);
        session.setPlannedEndAt(newEnd);
        Session saved = sessionRepository.save(session);

        audit(actor, "session.update", saved, before, snapshot(saved));
        return toDetail(saved);
    }

    // ======================================================================
    // Hủy phiên (chỉ Waiting)
    // ======================================================================

    @Transactional(rollbackFor = Exception.class)
    public SessionDetailResponse cancelSession(Long id, User actor) throws Exception {
        requireActor(actor);
        if (id == null) throw new ValidationException("id", "id phiên không được để trống");

        Session target = sessionRepository.findById(id).orElseThrow(SessionNotFound::new);
        switch (target.getStatus()) {
            case InProgress -> throw new AbstractException("can't cancel an in progress session",
                    "SESSION_INPROGRESS", "Phiên đang làm không được hủy", 409);
            case Ended -> throw new AbstractException("can not cancel a session that is ended",
                    "SESSION_ENDED", "Phiên đã kết thúc từ trước", 409);
            case Canceled -> throw new AbstractException("session is canceled before",
                    "SESSION_CANCELED", "Phiên đã bị hủy từ trước", 409);
            default -> { }
        }

        Map<String, Object> before = snapshot(target);
        target.setStatus(SessionStatus.Canceled);
        Session saved = sessionRepository.saveAndFlush(target);

        audit(actor, "session.cancel", saved, before, snapshot(saved));
        return toDetail(saved);
    }

    // ======================================================================
    // Thêm / bớt xe trong phiên đang làm
    // ======================================================================

    @Transactional(rollbackFor = Exception.class)
    public SessionDetailResponse addVehiclesToCurrentSession(AddSessionVehiclesRequest request, User actor) throws Exception {
        requireActor(actor);
        if (request == null) throw new ValidationException("body", "Thiếu danh sách xe");

        List<String> slugs = normalizeSlugs(request.vehicle_slugs());
        if (slugs.isEmpty()) throw new ValidationException("vehicle_slugs", "Cần ít nhất một xe");

        Session session = currentSessionLocked();
        attachVehicles(session, slugs);

        Map<String, Object> after = new LinkedHashMap<>();
        after.put("vehicle_slugs", slugs);
        audit(actor, "session.add_vehicle", session, null, after);
        return toDetail(session);
    }

    @Transactional(rollbackFor = Exception.class)
    public SessionDetailResponse removeVehicleFromCurrentSession(Long vehicleId, User actor) throws Exception {
        requireActor(actor);
        if (vehicleId == null) throw new ValidationException("vehicle_id", "id xe không được để trống");

        Session session = currentSessionLocked();

        Vehicle found = vehicleRepository.findById(vehicleId).orElseThrow(VehicleNotExits::new);
        SessionVehicle link = sessionVehicleRepository.findBySessionIdAndVehicleId(session.getId(), vehicleId)
                .filter(sv -> sv.getRemovedAt() == null)
                .orElseThrow(VehicleNotInSession::new);

        // Khóa xe để không đổi chỗ với thao tác gán vé đang chạy
        Vehicle vehicle = vehicleRepository.lockBySlug(found.getSlug()).orElseThrow(VehicleNotExits::new);
        if (vehicle.getStatus() == VehicleStatus.Inused) throw new VehicleInUse(vehicle.getSlug());

        link.setRemovedAt(now());
        sessionVehicleRepository.save(link);
        if (vehicle.getStatus() == VehicleStatus.Online) vehicle.setStatus(VehicleStatus.Offline);

        Map<String, Object> before = new LinkedHashMap<>();
        before.put("vehicle_slug", vehicle.getSlug());
        audit(actor, "session.remove_vehicle", session, before, null);
        return toDetail(session);
    }

    // ======================================================================
    // Thống kê
    // ======================================================================

    @Transactional(rollbackFor = Exception.class)
    public Responses.SessionStatsResponse getCurrentStats(Requests.SessionStatsRequest request) throws Exception {
        Session session = currentSession();
        return buildStats(session);
    }

    @Transactional(rollbackFor = Exception.class)
    public Responses.SessionStatsResponse getStats(Requests.SessionStatsRequest request) throws Exception {
        Long id = IdValidator.validate("id", request.getSessionId());
        Session session = sessionRepository.findById(id).orElseThrow(SessionNotFound::new);
        return buildStats(session);
    }

    private Responses.SessionStatsResponse buildStats(Session session) {
        ticketService.refreshOverdueIfRunning(session, TimeUtil.now());
        Long sessionId = session.getId();

        List<Ticket> tickets = ticketRepository.findBySessionId(sessionId);
        Map<String, Integer> ticketsByStatus = new LinkedHashMap<>();
        for (TicketStatus status : TicketStatus.values()) {
            ticketsByStatus.put(status.name(), 0);
        }
        long minutesSold = 0;
        for (Ticket t : tickets) {
            ticketsByStatus.merge(t.getStatus().name(), 1, Integer::sum);
            if (t.getStatus() != TicketStatus.Canceled) {
                minutesSold += t.getTotalMinutes();
            }
        }

        Map<PaymentType, Long> byType = new EnumMap<>(PaymentType.class);
        Map<PaymentMethod, long[]> byMethod = new EnumMap<>(PaymentMethod.class);
        for (PaymentType type : PaymentType.values()) {
            byType.put(type, 0L);
        }
        for (PaymentMethod method : PaymentMethod.values()) {
            byMethod.put(method, new long[]{0L, 0L});
        }
        for (Payment p : paymentRepository.findBySession_Id(sessionId)) {
            byType.merge(p.getType(), (long) p.getAmount(), Long::sum);
            long[] sums = byMethod.get(p.getMethod());
            if (p.getType() == PaymentType.Refund) {
                sums[1] += p.getAmount();
            } else {
                sums[0] += p.getAmount();
            }
        }
        long ticketAmount = byType.get(PaymentType.Ticket);
        long extensionAmount = byType.get(PaymentType.Extension);
        long overdueFeeAmount = byType.get(PaymentType.OverdueFee);
        long refundAmount = byType.get(PaymentType.Refund);
        long gross = ticketAmount + extensionAmount + overdueFeeAmount;

        Map<String, Responses.MethodRevenue> revenueByMethod = new LinkedHashMap<>();
        for (PaymentMethod method : PaymentMethod.values()) {
            long[] sums = byMethod.get(method);
            revenueByMethod.put(method.name(), new Responses.MethodRevenue(sums[0], sums[1], sums[0] - sums[1]));
        }

        Map<String, Integer> vehiclesByStatus = new LinkedHashMap<>();
        for (VehicleStatus status : VehicleStatus.values()) {
            vehiclesByStatus.put(status.name(), 0);
        }
        int vehicleTotal = 0;
        for (var sv : sessionVehicleRepository.findBySessionIdAndRemovedAtIsNull(sessionId)) {
            vehiclesByStatus.merge(sv.getVehicle().getStatus().name(), 1, Integer::sum);
            vehicleTotal++;
        }

        Responses.SessionBrief brief = new Responses.SessionBrief(
                session.getId(), session.getStatus().name(),
                session.getScheduledDate().toString(), session.getScheduledTime().toString(),
                TimeUtil.iso(session.getPlannedEndAt()), TimeUtil.iso(session.getStartedAt()),
                TimeUtil.iso(session.getEndedAt()), session.getIsInstant());

        return new Responses.SessionStatsResponse(
                brief,
                new Responses.TicketStats(tickets.size(), ticketsByStatus, minutesSold),
                new Responses.RevenueStats(ticketAmount, extensionAmount, overdueFeeAmount, refundAmount,
                        gross, gross - refundAmount, revenueByMethod),
                new Responses.VehicleStats(vehicleTotal, vehiclesByStatus));
    }

    // ======================================================================
    // Trợ giúp
    // ======================================================================

    /** Một nguồn thời gian duy nhất cho logic phiên (giờ máy chủ, khớp với giờ người dùng nhập). */
    private LocalDateTime now() {
        return LocalDateTime.now();
    }

    private static LocalDateTime defaultEnd(LocalDateTime now) {
        return now.toLocalDate().plusDays(1).atStartOfDay(); // 00:00 hôm sau
    }

    private static void requireActor(User actor) throws UnauthorizedException {
        if (actor == null) throw new UnauthorizedException();
    }

    private static <T> T required(T value, String field) throws ValidationException {
        if (value == null) throw new ValidationException(field, "Trường " + field + " không được để trống");
        return value;
    }

    private Session currentSession() throws NoActiveSession {
        return sessionRepository.findFirstByStatusOrderByIdDesc(SessionStatus.InProgress)
                .orElseThrow(NoActiveSession::new);
    }

    /** Khóa ghi phiên đang làm, cùng cách với TicketService.createTicket để hai thao tác không chen nhau. */
    private Session currentSessionLocked() throws NoActiveSession {
        return sessionRepository.lockByStatus(SessionStatus.InProgress).stream().findFirst()
                .orElseThrow(NoActiveSession::new);
    }

    private Session persistNewSession(LocalDateTime start, LocalDateTime end, boolean instant, User actor) throws Exception {
        if (!start.isBefore(end))
            throw new ValidationException("planned_end_at", "Thời điểm kết thúc phải muộn hơn thời điểm bắt đầu");
        if (instant && sessionRepository.existsByStatus(SessionStatus.InProgress))
            throw new SessionAlreadyInProgress();

        assertNoCollision(start, end, null);

        Session session = new Session(start, end, instant, actor);
        if (instant) session.setStartedAt(start);

        try {
            return sessionRepository.saveAndFlush(session);
        } catch (DataIntegrityViolationException e) {
            if (instant) throw new SessionAlreadyInProgress(); // unique index: chỉ một phiên InProgress
            throw e;
        }
    }

    /** Hai khoảng [start,end) trùng nhau khi curr.start < end và curr.end > start. Phiên nối tiếp nhau không tính là trùng. */
    private void assertNoCollision(LocalDateTime start, LocalDateTime end, Long excludeId) throws CollideWithOtherSession {
        for (Session curr : sessionRepository.findAllByStatusInAndPlannedEndAtAfter (ACTIVE, start)) {
            if (curr.getId().equals(excludeId)) continue;
            LocalDateTime currStart = LocalDateTime.of(curr.getScheduledDate(), curr.getScheduledTime());
            if (currStart.isBefore(end)) throw new CollideWithOtherSession();
        }
    }

    private List<String> normalizeSlugs(List<String> raw) throws ValidationException {
        if (raw == null) return new ArrayList<>();
        Set<String> result = new LinkedHashSet<>();
        for (String slug : raw) {
            if (slug == null || slug.isBlank())
                throw new ValidationException("vehicle_slugs", "Tên xe không được để trống");
            result.add(slug.trim());
        }
        return new ArrayList<>(result);
    }

    /** Đưa xe vào phiên và chuyển Online. Xe Repairing hoặc Inused không được chọn. */
    private void attachVehicles(Session session, List<String> slugs) throws Exception {
        LocalDateTime now = now();
        for (String slug : slugs) {
            Vehicle vehicle = vehicleRepository.lockBySlug(slug)
                    .orElseThrow(() -> new AbstractException("vehicle not found: " + slug,
                            "VEHICLE_NOT_EXITS", "Không tìm thấy xe " + slug, 404));

            if (vehicle.getStatus() != VehicleStatus.Online && vehicle.getStatus() != VehicleStatus.Offline)
                throw new AbstractException("vehicle not available: " + slug + " is " + vehicle.getStatus(),
                        "VEHICLE_NOT_AVAILABLE",
                        "Xe " + slug + " hiện không sẵn sàng (đang sửa chữa hoặc đang sử dụng)", 409);

            Optional<SessionVehicle> existing =
                    sessionVehicleRepository.findBySessionIdAndVehicleId(session.getId(), vehicle.getId());
            if (existing.isPresent() && existing.get().getRemovedAt() == null)
                throw new AbstractException("vehicle already in session: " + slug,
                        "VEHICLE_ALREADY_IN_SESSION", "Xe " + slug + " đã có trong phiên", 409);

            // Cặp (session_id, vehicle_id) là unique: xe từng bị bớt thì dùng lại dòng cũ
            SessionVehicle link = existing.orElseGet(SessionVehicle::new);
            if (link.getId() == null) {
                link.setSession(session);
                link.setVehicle(vehicle);
            }
            link.setAddedAt(now);
            link.setRemovedAt(null);
            sessionVehicleRepository.save(link);

            vehicle.setStatus(VehicleStatus.Online);
        }
    }

    private SessionDetailResponse toDetail(Session session) {
        List<vehicleInfoRespond> vehicles = new ArrayList<>();
        for (SessionVehicle sv : sessionVehicleRepository.findBySessionIdAndRemovedAtIsNull(session.getId())) {
            vehicles.add(new vehicleInfoRespond(sv.getVehicle()));
        }
        return new SessionDetailResponse(session, vehicles);
    }

    private Map<String, Object> snapshot(Session s) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("status", s.getStatus());
        m.put("scheduled_date", s.getScheduledDate());
        m.put("scheduled_time", s.getScheduledTime());
        m.put("planned_end_at", s.getPlannedEndAt());
        m.put("is_instant", s.getIsInstant());
        return m;
    }

    private void audit(User actor, String action, Session session, Map<String, Object> before, Map<String, Object> after) {
        auditLogService.log(actor, action, "session", session.getId(), before, after);
    }
}
