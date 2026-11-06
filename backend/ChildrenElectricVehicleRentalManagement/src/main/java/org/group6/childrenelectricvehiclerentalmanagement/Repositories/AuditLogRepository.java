package org.group6.childrenelectricvehiclerentalmanagement.Repositories;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Ghi nhật ký thao tác vào bảng audit_logs.
 * Dùng SQL trực tiếp với CAST(? AS json) để không phụ thuộc cách Hibernate ánh xạ cột json.
 * JdbcTemplate dùng chung DataSource nên nằm trong cùng transaction với service gọi nó.
 */
@Repository
public class AuditLogRepository {
    private static final String INSERT_SQL =
            "INSERT INTO audit_logs (user_id, action, entity_type, entity_id, payload) "
                    + "VALUES (?, ?, ?, ?, CAST(? AS json))";

    private final JdbcTemplate jdbcTemplate;

    public AuditLogRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(Long userId, String action, String entityType, Long entityId, String payloadJson) {
        jdbcTemplate.update(INSERT_SQL, userId, action, entityType, entityId, payloadJson);
    }
}
