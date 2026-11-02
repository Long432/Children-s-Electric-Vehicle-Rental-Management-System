package org.group6.childrenelectricvehiclerentalmanagement.Services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Long.Requests;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Long.Responses;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.SessionController.CreateSessionRequest;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.*;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.*;
import org.group6.childrenelectricvehiclerentalmanagement.Models.Payment;
import org.group6.childrenelectricvehiclerentalmanagement.Models.Session;
import org.group6.childrenelectricvehiclerentalmanagement.Models.Ticket;
import org.group6.childrenelectricvehiclerentalmanagement.Models.User;
import org.group6.childrenelectricvehiclerentalmanagement.Repositories.PaymentRepository;
import org.group6.childrenelectricvehiclerentalmanagement.Repositories.SesssionRepository;
import org.group6.childrenelectricvehiclerentalmanagement.Repositories.SessionVehicleRepository;
import org.group6.childrenelectricvehiclerentalmanagement.Repositories.TicketRepository;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.IdValidator;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.TimeUtil;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@AllArgsConstructor
public class SessionService {

    private final SesssionRepository sessionRepository;
    private final TicketRepository ticketRepository;
    private final PaymentRepository paymentRepository;
    private final SessionVehicleRepository sessionVehicleRepository;
    private final TicketService ticketService;

    public Session createSession(CreateSessionRequest request, User user) throws Exception {
        LocalDateTime plannedStartTime = null;
        LocalDateTime plannedEndTime = LocalDateTime.of(request.end_date(),request.end_time());

        if (request.is_instant()) plannedStartTime=LocalDateTime.now();
        else plannedStartTime = LocalDateTime.of(request.start_date(),request.start_time());

        if (request.start_date().isBefore(LocalDate.now())) throw new UserException("start date is in the past","Ngày bắt đầu phải sau ngày hiện tại");
        if (request.start_date().isAfter(request.end_date())) throw new UserException("start date is later than end date", "Ngày bắt đầu phải sớm hơn ngày kết thúc");
        if (request.start_date().isEqual(request.end_date())&&
                request.start_time().isAfter(request.end_time())) throw new UserException("start time is later than end time","Giờ bắt đầu phiên phải sớm hơn giờ kết thúc phiên");

        List<Session> allSessions = new ArrayList<Session>(sessionRepository.findAllByScheduledDateIs(plannedStartTime.toLocalDate()));

        if (!request.start_date().isEqual(request.end_date())) allSessions.addAll(sessionRepository.findAllByPlannedEndAt_Date(plannedEndTime.toLocalDate()));

        for(Session curr:allSessions){
            LocalDateTime currStartTime=LocalDateTime.of(curr.getScheduledDate(),curr.getScheduledTime());
            if(currStartTime.isAfter(plannedStartTime)&&currStartTime.isBefore(plannedEndTime)) throw new CollideWithOtherSession();
            if(curr.getPlannedEndAt().isAfter(plannedStartTime)&&curr.getPlannedEndAt().isBefore(plannedEndTime)) throw new CollideWithOtherSession();
            if(plannedStartTime.isAfter(currStartTime)&& plannedEndTime.isBefore(curr.getEndedAt())) throw new CollideWithOtherSession();
        }

        return sessionRepository.save(new Session(plannedStartTime,plannedEndTime,request.is_instant(),user));
    }

    public Session startSession(Long sessionId) throws Exception {
        Session target = sessionRepository.findById(sessionId).orElseThrow(() -> new UserException("Session not found with id: " + sessionId,"Phiên không tồn tại"));
        switch (target.getStatus()){
            case Ended -> throw new UserException("session already closed","Phiên này đã hoàn thành từ trước");
            case Canceled -> throw new UserException("can not start a canceled session","Phiên này đã bị hủy");
            case InProgress -> throw new UserException("the session already started","Phiên này đang được thực hiện");
        }
        target.setStatus(SessionStatus.InProgress);
        return sessionRepository.save(target);
    }

    public boolean endCurrentSession() throws Exception {
        Session target = sessionRepository.findByStatus(SessionStatus.InProgress).orElseThrow(NoActiveSession::new);
        List<Ticket> leftOverTickets = ticketRepository.findBySession_IdAndStatus(target.getId(),TicketStatus.Inused);
        return true;
    }

    public Session getCurrentSession() throws Exception {
        return sessionRepository.findByStatus(SessionStatus.InProgress).orElseThrow(NoActiveSession::new);
    }

    public List<Session> getAllSessions() {
        return sessionRepository.findAll();
    }

    public Session getSessionDetail(Long id) {
        return sessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Session not found with id: " + id));
    }

    public Session updateSession(Long Id, Session sessionDetails)throws Exception {
         return sessionRepository.findById(Id).map(session -> {
            session.setScheduledDate(sessionDetails.getScheduledDate());
            session.setScheduledTime(sessionDetails.getScheduledTime());
            session.setPlannedEndAt(sessionDetails.getPlannedEndAt());
            session.setIsInstant(sessionDetails.getIsInstant());
            session.setNextTicketNo(sessionDetails.getNextTicketNo());
            return sessionRepository.save(session);
        }).orElseThrow(SessionNotFound::new);
    }

    public boolean cancelSession(Long id) {
        sessionRepository.findById(id).ifPresent(session -> {
            session.setStatus(SessionStatus.Canceled);
            sessionRepository.save(session);
        });
        return true;
    }

    public boolean addVehicleToCurrentSession(Long vehicleId) {
        return true;
    }

    public boolean removeVehicleFromCurrentSession(Long vehicleId) {
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    public Responses.SessionStatsResponse getCurrentStats(Requests.SessionStatsRequest request) throws Exception {
        Session session = sessionRepository.findFirstByStatusOrderByIdDesc(SessionStatus.InProgress)
                .orElseThrow(NoActiveSession::new);
        return buildStats(session);
    }

    @Transactional(rollbackFor = Exception.class)
    public Responses.SessionStatsResponse getStats(Requests.SessionStatsRequest request) throws Exception {
        Long id = IdValidator.validate("id", request.getSessionId());
        Session session = sessionRepository.findById(id)
                .orElseThrow(SessionNotFound::new);
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
}
