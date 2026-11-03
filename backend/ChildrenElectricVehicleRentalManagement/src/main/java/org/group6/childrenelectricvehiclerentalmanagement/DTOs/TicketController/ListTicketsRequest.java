package org.group6.childrenelectricvehiclerentalmanagement.DTOs.TicketController;

import lombok.Getter;
import lombok.Setter;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Common.BaseRequest;

@Getter
@Setter
public class ListTicketsRequest extends BaseRequest {
    private String sessionId;
    private String status;
    private String page;
    private String limit;
}
