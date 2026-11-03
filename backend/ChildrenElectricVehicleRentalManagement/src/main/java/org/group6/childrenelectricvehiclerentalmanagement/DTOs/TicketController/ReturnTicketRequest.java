package org.group6.childrenelectricvehiclerentalmanagement.DTOs.TicketController;

import lombok.Getter;
import lombok.Setter;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Common.BaseRequest;

@Getter
@Setter
public class ReturnTicketRequest extends BaseRequest {
    private String id;
    private String paymentMethod;
    private String refundMethod;
}
