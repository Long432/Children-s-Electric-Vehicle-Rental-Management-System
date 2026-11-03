package org.group6.childrenelectricvehiclerentalmanagement.DTOs.SessionController;

public record SessionStatsResponse(SessionBrief session, TicketStats tickets, RevenueStats revenue,
                                   VehicleStats vehicles) {
}
