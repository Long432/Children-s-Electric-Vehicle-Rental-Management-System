package org.group6.childrenelectricvehiclerentalmanagement.DTOs.TicketController;

import lombok.Getter;
import lombok.Setter;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Common.BaseRequest;

@Getter
@Setter
public class CreateTicketRequest extends BaseRequest {
    private String phone;
    private String minutes;
    private String paymentMethod;
}
