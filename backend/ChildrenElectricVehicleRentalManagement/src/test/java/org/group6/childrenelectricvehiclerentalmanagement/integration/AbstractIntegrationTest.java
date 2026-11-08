package org.group6.childrenelectricvehiclerentalmanagement.integration;

import org.group6.childrenelectricvehiclerentalmanagement.Enum.PaymentMethod;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.SessionStatus;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.TicketStatus;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.VehicleStatus;
import org.group6.childrenelectricvehiclerentalmanagement.Models.Package;
import org.group6.childrenelectricvehiclerentalmanagement.Models.Session;
import org.group6.childrenelectricvehiclerentalmanagement.Models.Settings;
import org.group6.childrenelectricvehiclerentalmanagement.Models.Ticket;
import org.group6.childrenelectricvehiclerentalmanagement.Models.User;
import org.group6.childrenelectricvehiclerentalmanagement.Models.Vehicle;
import org.group6.childrenelectricvehiclerentalmanagement.Models.LinkingObject.SessionVehicle;
import org.group6.childrenelectricvehiclerentalmanagement.Repositories.*;
import org.group6.childrenelectricvehiclerentalmanagement.Services.SettingsService;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
public abstract class AbstractIntegrationTest {

    @Autowired protected SessionRepository sessionRepository;
    @Autowired protected TicketRepository ticketRepository;
    @Autowired protected VehicleRepository vehicleRepository;
    @Autowired protected PackageRepository packageRepository;
    @Autowired protected SettingsRepository settingsRepository;
    @Autowired protected UserRepository userRepository;
    @Autowired protected SessionVehicleRepository sessionVehicleRepository;
    @Autowired protected PaymentRepository paymentRepository;
    @Autowired protected TicketVehicleHistoryRepository historyRepository;
    @Autowired protected SettingsService settingsService;

    protected User seller;
    protected Session session;
    protected Vehicle vehicle;

    @BeforeEach
    void setUp() {
        seller = new User("seller", "password");
        seller.setUsername("seller");
        seller.setPasswordHash("hash");
        userRepository.save(seller);

        session = new Session(
                LocalDateTime.of(2026, 11, 10, 8, 0),
                LocalDateTime.of(2026, 11, 10, 17, 0),
                true,
                seller
        );
        session.setNextTicketNo(1);
        sessionRepository.save(session);

        vehicle = new Vehicle("xe-01");
        vehicle.setStatus(VehicleStatus.Online);
        vehicleRepository.save(vehicle);

        SessionVehicle sv = new SessionVehicle();
        sv.setSession(session);
        sv.setVehicle(vehicle);
        sv.setAddedAt(LocalDateTime.now());
        sv.setRemovedAt(null);
        sessionVehicleRepository.save(sv);

        // Tạo settings mặc định
        Settings settings = new Settings();
        settings.setId((short) 1);
        settings.setRefundEnabled(false);
        settings.setExtendEnabled(true);
        settings.setOverdueFeePerMinute(2000);
        settings.setOverdueGraceMinutes(5);
        settings.setMinimumMinutes(10);
        settings.setCreatedAt(LocalDateTime.now());
        settings.setUpdatedAt(LocalDateTime.now());
        settingsRepository.save(settings);

        // Tạo package mặc định
        Package pkg = new Package();
        pkg.setAmount(15000);
        pkg.setMinutes(30);
        pkg.setIsDefault(true);
        pkg.setActive(true);
        pkg.setCreatedAt(LocalDateTime.now());
        pkg.setUpdatedAt(LocalDateTime.now());
        packageRepository.save(pkg);
    }

    protected Session createWaitingSession() {
        LocalDateTime now = LocalDateTime.now();
        Session s = new Session(
                now,
                now.plusHours(9),
                false,
                seller
        );
        s.setNextTicketNo(1);
        return sessionRepository.save(s);
    }

    protected Vehicle createVehicle(String slug, VehicleStatus status) {
        Vehicle v = new Vehicle(slug);
        v.setStatus(status);
        return vehicleRepository.save(v);
    }

    protected Ticket createWaitingTicket(Session sess, String phone, int minutes, PaymentMethod method) {
        Ticket t = new Ticket();
        t.setSession(sess);
        t.setTicketNo(sess.getNextTicketNo());
        t.setPhone(phone);
        t.setStatus(TicketStatus.Waiting);
        t.setVehicle(null);
        t.setPaymentMethod(method);
        t.setTotalMinutes(minutes);
        t.setAmountPaid(15000);
        t.setPriceAmount(15000);
        t.setPriceMinutes(30);
        t.setOverdueFeePerMinute(2000);
        t.setOverdueGraceMinutes(5);
        t.setRefundEnabled(false);
        t.setQueuedAt(LocalDateTime.now());
        t.setCreatedBy(seller);
        ticketRepository.save(t);
        sess.setNextTicketNo(sess.getNextTicketNo() + 1);
        sessionRepository.save(sess);
        return t;
    }

    protected Ticket createInusedTicket(Session sess, String phone, int minutes, PaymentMethod method, LocalDateTime dueAt) {
        Ticket t = new Ticket();
        t.setSession(sess);
        t.setTicketNo(sess.getNextTicketNo());
        t.setPhone(phone);
        t.setStatus(TicketStatus.Inused);
        t.setVehicle(vehicle);
        t.setPaymentMethod(method);
        t.setTotalMinutes(minutes);
        t.setAmountPaid(15000);
        t.setPriceAmount(15000);
        t.setPriceMinutes(30);
        t.setOverdueFeePerMinute(2000);
        t.setOverdueGraceMinutes(5);
        t.setRefundEnabled(false);
        t.setQueuedAt(LocalDateTime.now());
        t.setStartedAt(LocalDateTime.now());
        t.setDueAt(dueAt);
        t.setCreatedBy(seller);
        ticketRepository.save(t);
        sess.setNextTicketNo(sess.getNextTicketNo() + 1);
        sessionRepository.save(sess);
        return t;
    }
}
