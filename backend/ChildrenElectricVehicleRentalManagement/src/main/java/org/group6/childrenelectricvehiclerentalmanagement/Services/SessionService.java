package org.group6.childrenelectricvehiclerentalmanagement.Services;

import org.group6.childrenelectricvehiclerentalmanagement.DTOs.SessionController.CreateSessionRequest;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.SessionStatus;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.CollideWithOtherSession;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.NoActiveSession;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.SessionNotFound;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.UserException;
import org.group6.childrenelectricvehiclerentalmanagement.Models.Session;
import org.group6.childrenelectricvehiclerentalmanagement.Models.Ticket;
import org.group6.childrenelectricvehiclerentalmanagement.Models.User;
import org.group6.childrenelectricvehiclerentalmanagement.Repositories.SesssionRepository;
import org.group6.childrenelectricvehiclerentalmanagement.Repositories.TicketRepository;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class SessionService {

    private final SesssionRepository sessionRepository;
    private final TicketRepository ticketRepository;

    public SessionService(SesssionRepository sessionRepository, TicketRepository ticketRepository) {
        this.sessionRepository = sessionRepository;
        this.ticketRepository = ticketRepository;
    }

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
        List<Ticket> leftOverTickets = ticketRepository.getInUsedTicketsWSessionId(target.getId());
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
        // Minimal implementation - mark as handled
        return true;
    }

    public boolean removeVehicleFromCurrentSession(Long vehicleId) {
        // Minimal implementation - mark as handled
        return true;
    }
}