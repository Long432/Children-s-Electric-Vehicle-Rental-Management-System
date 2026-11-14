package org.group6.childrenelectricvehiclerentalmanagement.Services;

import lombok.RequiredArgsConstructor;
import org.group6.childrenelectricvehiclerentalmanagement.Models.AuditLog;
import org.group6.childrenelectricvehiclerentalmanagement.Models.User;
import org.group6.childrenelectricvehiclerentalmanagement.Repositories.AuditLogRepository;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.JsonUtil;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.TimeUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuditLogService {
    private final AuditLogRepository auditLogRepository;

    @Transactional
    public void log(User actor, String action, String entityType, Long entityId, Map<String, Object> before, Map<String, Object> after) {
        Map<String, Object> payload = new LinkedHashMap<>();
        if (before != null) {
            payload.put("before", before);
        }
        if (after != null) {
            payload.put("after", after);
        }

        AuditLog log = new AuditLog();
        log.setUser(actor);
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setPayload(payload.toString());
        log.setCreatedAt(LocalDateTime.now());
        auditLogRepository.save(log);
    }
}
