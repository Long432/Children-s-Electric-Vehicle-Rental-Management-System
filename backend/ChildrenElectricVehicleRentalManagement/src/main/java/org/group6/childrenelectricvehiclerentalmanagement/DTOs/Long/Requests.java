package org.group6.childrenelectricvehiclerentalmanagement.DTOs.Long;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

public final class Requests {
    private Requests() {}

    @Getter
    @Setter
    public static class BaseRequest {
        private Long actorId;
    }

    @Getter
    @Setter
    public static class ToggleSettingRequest extends BaseRequest {
        private String enabled;
    }

    @Getter
    @Setter
    public static class PackageItem {
        private String amount;
        private String minutes;
        private String isDefault;
    }

    @Getter
    @Setter
    public static class UpdatePackagesRequest extends BaseRequest {
        private List<PackageItem> packages;
    }

    @Getter
    @Setter
    public static class SetOverdueFeeRequest extends BaseRequest {
        private String amountPerMinute;
    }

    @Getter
    @Setter
    public static class SetMinutesRequest extends BaseRequest {
        private String minutes;
    }

    @Getter
    @Setter
    public static class CreateTicketRequest extends BaseRequest {
        private String phone;
        private String minutes;
        private String paymentMethod;
    }

    @Getter
    @Setter
    public static class ListTicketsRequest extends BaseRequest {
        private String sessionId;
        private String status;
        private String page;
        private String limit;
    }

    @Getter
    @Setter
    public static class SearchTicketsRequest extends BaseRequest {
        private String q;
        private String sessionId;
    }

    @Getter
    @Setter
    public static class TicketIdRequest extends BaseRequest {
        private String id;
    }

    @Getter
    @Setter
    public static class AssignVehicleRequest extends BaseRequest {
        private String id;
        private String vehicleSlug;
    }

    @Getter
    @Setter
    public static class CancelTicketRequest extends BaseRequest {
        private String id;
        private String refundMethod;
    }

    @Getter
    @Setter
    public static class ChangeVehicleRequest extends BaseRequest {
        private String id;
        private String vehicleSlug;
        private String oldVehicleStatus;
    }

    @Getter
    @Setter
    public static class ExtendTicketRequest extends BaseRequest {
        private String id;
        private String minutes;
        private String paymentMethod;
    }

    @Getter
    @Setter
    public static class ReturnTicketRequest extends BaseRequest {
        private String id;
        private String paymentMethod;
        private String refundMethod;
    }

    @Getter
    @Setter
    public static class SessionStatsRequest extends BaseRequest {
        private String sessionId;
    }
}
