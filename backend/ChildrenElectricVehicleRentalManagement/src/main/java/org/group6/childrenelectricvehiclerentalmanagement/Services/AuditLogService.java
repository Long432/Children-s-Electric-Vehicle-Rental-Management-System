package org.group6.childrenelectricvehiclerentalmanagement.Services;

import lombok.RequiredArgsConstructor;
import org.group6.childrenelectricvehiclerentalmanagement.Models.AuditLog;
import org.group6.childrenelectricvehiclerentalmanagement.Models.User;
import org.group6.childrenelectricvehiclerentalmanagement.Repositories.AuditLogRepository;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.JsonUtil;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.TimeUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        AuditLog entry = new AuditLog();
        entry.setUser(actor);
        entry.setAction(action);
        entry.setEntityType(entityType);
        entry.setEntityId(entityId);
        entry.setPayload(JsonUtil.toJson(payload));
        entry.setCreatedAt(TimeUtil.now());
        auditLogRepository.save(entry);
    }
}
