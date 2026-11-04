package org.group6.childrenelectricvehiclerentalmanagement.Services;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.RequiredTypes;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.PaymentMethod;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.PaymentType;
import org.group6.childrenelectricvehiclerentalmanagement.Models.Payment;
import org.group6.childrenelectricvehiclerentalmanagement.Models.Session;
import org.group6.childrenelectricvehiclerentalmanagement.Models.Ticket;
import org.group6.childrenelectricvehiclerentalmanagement.Models.User;
import org.group6.childrenelectricvehiclerentalmanagement.Repositories.PaymentRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private Session currentSession;

    public void newTicket(Ticket newTicket, User actor){
        paymentRepository.save(new Payment(currentSession, newTicket, actor));
    }
}
