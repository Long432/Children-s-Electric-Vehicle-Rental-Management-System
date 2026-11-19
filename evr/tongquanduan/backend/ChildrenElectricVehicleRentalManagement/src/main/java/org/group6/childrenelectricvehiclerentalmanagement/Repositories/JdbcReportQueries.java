package org.group6.childrenelectricvehiclerentalmanagement.Repositories;

import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Report.IncidentRow;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Report.ParticipantRow;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Report.TimelineEvent;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Report.UserRef;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Report.VehicleReportRow;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.TimeUtil;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class JdbcReportQueries implements ReportQueries {

    private static final String SESSIONS = """
            SELECT s.id, s.scheduled_date, s.status, s.started_at, s.ended_at,
                   s.created_by AS created_id, cu.username AS created_name,
                   s.started_by AS started_id, su.username AS started_name,
                   s.ended_by AS ended_id, eu.username AS ended_name,
                   (SELECT COUNT(*) FROM tickets t WHERE t.session_id = s.id) AS ticket_count,
                   (SELECT COUNT(*) FROM tickets t WHERE t.session_id = s.id AND t.status = 'Canceled') AS canceled_count,
                   (SELECT COALESCE(SUM(CASE WHEN p.type = 'Refund' THEN -p.amount ELSE p.amount END), 0)
                      FROM payments p WHERE p.session_id = s.id AND p.method = 'Cash') AS cash,
                   (SELECT COALESCE(SUM(CASE WHEN p.type = 'Refund' THEN -p.amount ELSE p.amount END), 0)
                      FROM payments p WHERE p.session_id = s.id AND p.method = 'Bank') AS bank,
                   (SELECT COALESCE(SUM(p.amount), 0) FROM payments p WHERE p.session_id = s.id AND p.type = 'OverdueFee') AS overdue_fee,
                   (SELECT COALESCE(SUM(p.amount), 0) FROM payments p WHERE p.session_id = s.id AND p.type = 'Refund') AS refund_total,
                   (SELECT COUNT(*) FROM vehicle_incidents i WHERE i.session_id = s.id) AS incident_count
            FROM sessions s
            JOIN users su ON su.id = s.started_by
            JOIN users cu ON cu.id = s.created_by
            LEFT JOIN users eu ON eu.id = s.ended_by
            WHERE s.started_by IS NOT NULL""";
    private static final String SESSIONS_BY_ID = " AND s.id = :sessionId";
    private static final String SESSIONS_FROM = " AND s.scheduled_date >= :fromDate";
    private static final String SESSIONS_TO = " AND s.scheduled_date <= :toDate";
    private static final String SESSIONS_BY_OWNER = " AND s.started_by = :scopeUserId";
    private static final String SESSIONS_ORDER = " ORDER BY s.started_at DESC, s.id DESC";

    private static final String USERS = "SELECT u.id, u.username, u.role, u.status FROM users u WHERE 1 = 1";
    private static final String USERS_BY_ID = " AND u.id = :scopeUserId";
    private static final String USERS_ORDER = " ORDER BY u.username";

    private static final String PARTICIPANTS = """
            SELECT u.id, u.username,
                   (SELECT COUNT(*) FROM tickets t WHERE t.session_id = :sessionId AND t.created_by = u.id) AS tickets_created,
                   (SELECT COUNT(*) FROM tickets t WHERE t.session_id = :sessionId AND t.returned_by = u.id) AS tickets_returned,
                   (SELECT COUNT(*) FROM tickets t WHERE t.session_id = :sessionId AND t.canceled_by = u.id) AS tickets_canceled,
                   (SELECT COALESCE(SUM(CASE WHEN p.type = 'Refund' THEN -p.amount ELSE p.amount END), 0)
                      FROM payments p WHERE p.session_id = :sessionId AND p.created_by = u.id AND p.method = 'Cash') AS cash,
                   (SELECT COALESCE(SUM(CASE WHEN p.type = 'Refund' THEN -p.amount ELSE p.amount END), 0)
                      FROM payments p WHERE p.session_id = :sessionId AND p.created_by = u.id AND p.method = 'Bank') AS bank
            FROM users u
            WHERE u.id IN (
                SELECT s.started_by FROM sessions s WHERE s.id = :sessionId
                UNION SELECT s.ended_by FROM sessions s WHERE s.id = :sessionId
                UNION SELECT t.created_by FROM tickets t WHERE t.session_id = :sessionId
                UNION SELECT t.returned_by FROM tickets t WHERE t.session_id = :sessionId
                UNION SELECT t.canceled_by FROM tickets t WHERE t.session_id = :sessionId
                UNION SELECT p.created_by FROM payments p WHERE p.session_id = :sessionId)
            ORDER BY u.username""";

    private static final String INCIDENTS = """
            SELECT i.id, i.created_at, i.vehicle_id, v.slug, i.session_id, i.ticket_id, t.ticket_no, i.source,
                   i.reported_by, ru.username AS reported_name, s.started_by AS charge_id, su.username AS charge_name
            FROM vehicle_incidents i
            JOIN vehicles v ON v.id = i.vehicle_id
            JOIN users ru ON ru.id = i.reported_by
            LEFT JOIN tickets t ON t.id = i.ticket_id
            LEFT JOIN sessions s ON s.id = i.session_id
            LEFT JOIN users su ON su.id = s.started_by
            WHERE 1 = 1""";
    private static final String INCIDENTS_FROM = " AND i.created_at >= :fromUtc";
    private static final String INCIDENTS_TO = " AND i.created_at < :toUtc";
    private static final String INCIDENTS_SCOPE = " AND (s.started_by = :scopeUserId OR i.reported_by = :scopeUserId)";
    private static final String INCIDENTS_VEHICLE = " AND i.vehicle_id = :vehicleId";
    private static final String INCIDENTS_SESSION = " AND i.session_id = :sessionId";
    private static final String INCIDENTS_ORDER = " ORDER BY i.created_at DESC, i.id DESC LIMIT :limit";

    private static final String TIMELINE = """
            SELECT a.created_at, a.user_id, u.username, a.action, a.entity_type, a.entity_id, a.payload
            FROM audit_logs a
            JOIN users u ON u.id = a.user_id
            WHERE (a.entity_type = 'session' AND a.entity_id = :sessionId)
               OR (a.entity_type = 'ticket' AND a.entity_id IN (SELECT t.id FROM tickets t WHERE t.session_id = :sessionId))
            ORDER BY a.created_at, a.id
            LIMIT :limit""";

    private static final String VEHICLES = """
            SELECT v.id, v.slug, v.status, COUNT(i.id) AS incident_count, MAX(i.created_at) AS last_incident_at
            FROM vehicle_incidents i
            JOIN vehicles v ON v.id = i.vehicle_id
            LEFT JOIN sessions s ON s.id = i.session_id
            WHERE 1 = 1""";
    private static final String VEHICLES_GROUP = " GROUP BY v.id, v.slug, v.status ORDER BY incident_count DESC, last_incident_at DESC, v.id";

    private final NamedParameterJdbcTemplate jdbc;

    public JdbcReportQueries(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<SessionRow> sessions(LocalDate from, LocalDate to, Long startedBy, Long sessionId) {
        StringBuilder sql = new StringBuilder(SESSIONS);
        MapSqlParameterSource params = new MapSqlParameterSource();
        if (sessionId != null) {
            sql.append(SESSIONS_BY_ID);
            params.addValue("sessionId", sessionId);
        }
        if (from != null) {
            sql.append(SESSIONS_FROM);
            params.addValue("fromDate", from);
        }
        if (to != null) {
            sql.append(SESSIONS_TO);
            params.addValue("toDate", to);
        }
        if (startedBy != null) {
            sql.append(SESSIONS_BY_OWNER);
            params.addValue("scopeUserId", startedBy);
        }
        sql.append(SESSIONS_ORDER);
        return jdbc.query(sql.toString(), params, (rs, row) -> new SessionRow(
                rs.getLong("id"),
                rs.getObject("scheduled_date", LocalDate.class),
                rs.getString("status"),
                rs.getObject("started_at", LocalDateTime.class),
                rs.getObject("ended_at", LocalDateTime.class),
                rs.getLong("created_id"),
                rs.getString("created_name"),
                rs.getLong("started_id"),
                rs.getString("started_name"),
                rs.getObject("ended_id", Long.class),
                rs.getString("ended_name"),
                rs.getLong("ticket_count"),
                rs.getLong("canceled_count"),
                rs.getLong("cash"),
                rs.getLong("bank"),
                rs.getLong("overdue_fee"),
                rs.getLong("refund_total"),
                rs.getLong("incident_count")));
    }

    @Override
    public List<UserRow> users(Long onlyUserId) {
        StringBuilder sql = new StringBuilder(USERS);
        MapSqlParameterSource params = new MapSqlParameterSource();
        if (onlyUserId != null) {
            sql.append(USERS_BY_ID);
            params.addValue("scopeUserId", onlyUserId);
        }
        sql.append(USERS_ORDER);
        return jdbc.query(sql.toString(), params, (rs, row) -> new UserRow(
                rs.getLong("id"), rs.getString("username"), rs.getString("role"), rs.getString("status")));
    }

    @Override
    public List<ParticipantRow> participants(long sessionId) {
        return jdbc.query(PARTICIPANTS, new MapSqlParameterSource("sessionId", sessionId), (rs, row) -> {
            long cash = rs.getLong("cash");
            long bank = rs.getLong("bank");
            return new ParticipantRow(
                    rs.getLong("id"),
                    rs.getString("username"),
                    rs.getLong("tickets_created"),
                    rs.getLong("tickets_returned"),
                    rs.getLong("tickets_canceled"),
                    cash,
                    bank,
                    cash + bank);
        });
    }

    @Override
    public List<IncidentRow> incidents(LocalDateTime fromUtc, LocalDateTime toUtc, Long scopeUserId, Long vehicleId, Long sessionId, int limit) {
        StringBuilder sql = new StringBuilder(INCIDENTS);
        MapSqlParameterSource params = new MapSqlParameterSource("limit", limit);
        if (fromUtc != null) {
            sql.append(INCIDENTS_FROM);
            params.addValue("fromUtc", fromUtc);
        }
        if (toUtc != null) {
            sql.append(INCIDENTS_TO);
            params.addValue("toUtc", toUtc);
        }
        if (scopeUserId != null) {
            sql.append(INCIDENTS_SCOPE);
            params.addValue("scopeUserId", scopeUserId);
        }
        if (vehicleId != null) {
            sql.append(INCIDENTS_VEHICLE);
            params.addValue("vehicleId", vehicleId);
        }
        if (sessionId != null) {
            sql.append(INCIDENTS_SESSION);
            params.addValue("sessionId", sessionId);
        }
        sql.append(INCIDENTS_ORDER);
        return jdbc.query(sql.toString(), params, (rs, row) -> {
            Long chargeId = rs.getObject("charge_id", Long.class);
            return new IncidentRow(
                    rs.getLong("id"),
                    TimeUtil.toIsoUtc(rs.getObject("created_at", LocalDateTime.class)),
                    rs.getLong("vehicle_id"),
                    rs.getString("slug"),
                    rs.getObject("session_id", Long.class),
                    rs.getObject("ticket_id", Long.class),
                    rs.getObject("ticket_no", Integer.class),
                    rs.getString("source"),
                    new UserRef(rs.getLong("reported_by"), rs.getString("reported_name")),
                    chargeId == null ? null : new UserRef(chargeId, rs.getString("charge_name")));
        });
    }

    @Override
    public List<TimelineEvent> timeline(long sessionId, int limit) {
        MapSqlParameterSource params = new MapSqlParameterSource("sessionId", sessionId).addValue("limit", limit);
        return jdbc.query(TIMELINE, params, (rs, row) -> new TimelineEvent(
                TimeUtil.toIsoUtc(rs.getObject("created_at", LocalDateTime.class)),
                new UserRef(rs.getLong("user_id"), rs.getString("username")),
                rs.getString("action"),
                rs.getString("entity_type"),
                rs.getObject("entity_id", Long.class),
                rs.getString("payload")));
    }

    @Override
    public List<VehicleReportRow> vehicles(LocalDateTime fromUtc, LocalDateTime toUtc, Long scopeUserId) {
        StringBuilder sql = new StringBuilder(VEHICLES);
        MapSqlParameterSource params = new MapSqlParameterSource();
        if (fromUtc != null) {
            sql.append(INCIDENTS_FROM);
            params.addValue("fromUtc", fromUtc);
        }
        if (toUtc != null) {
            sql.append(INCIDENTS_TO);
            params.addValue("toUtc", toUtc);
        }
        if (scopeUserId != null) {
            sql.append(INCIDENTS_SCOPE);
            params.addValue("scopeUserId", scopeUserId);
        }
        sql.append(VEHICLES_GROUP);
        return jdbc.query(sql.toString(), params, (rs, row) -> new VehicleReportRow(
                rs.getLong("id"),
                rs.getString("slug"),
                rs.getString("status"),
                rs.getLong("incident_count"),
                TimeUtil.toIsoUtc(rs.getObject("last_incident_at", LocalDateTime.class))));
    }
}
