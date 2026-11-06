package org.group6.childrenelectricvehiclerentalmanagement.Services;

import org.group6.childrenelectricvehiclerentalmanagement.Repositories.AuditLogRepository;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.JsonUtil;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.Security.AuthenticatedUser;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void log(AuthenticatedUser actor, String action, String entityType, Long entityId, Map<String, Object> payload) {
        auditLogRepository.insert(actor.id(), action, entityType, entityId, JsonUtil.toJson(payload));
    }
}
