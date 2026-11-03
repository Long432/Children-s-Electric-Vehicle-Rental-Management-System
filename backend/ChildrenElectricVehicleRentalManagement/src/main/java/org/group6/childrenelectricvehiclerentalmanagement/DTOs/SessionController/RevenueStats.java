package org.group6.childrenelectricvehiclerentalmanagement.DTOs.SessionController;

import java.util.Map;

public record RevenueStats(long ticketAmount, long extensionAmount, long overdueFeeAmount,
                           long refundAmount, long gross, long net,
                           Map<String, MethodRevenue> byMethod) {
}
