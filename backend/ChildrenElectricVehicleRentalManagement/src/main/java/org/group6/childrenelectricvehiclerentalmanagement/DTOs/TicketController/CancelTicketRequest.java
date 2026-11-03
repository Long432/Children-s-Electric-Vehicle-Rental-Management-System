package org.group6.childrenelectricvehiclerentalmanagement.DTOs.TicketController;

import lombok.Getter;
import lombok.Setter;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Common.BaseRequest;

@Getter
@Setter
public class CancelTicketRequest extends BaseRequest {
    private String id;
    private String refundMethod;
}
