DO $$
DECLARE
    v_sessions bigint[];
BEGIN
    SELECT array_agg(g.session_id) INTO v_sessions
    FROM (
        SELECT t.session_id
        FROM tickets t
        GROUP BY t.session_id
        HAVING bool_and(t.phone LIKE '0900000%')
    ) g;

    IF v_sessions IS NOT NULL THEN
        DELETE FROM vehicle_incidents WHERE session_id = ANY (v_sessions);
        DELETE FROM payments WHERE session_id = ANY (v_sessions);
        DELETE FROM tickets WHERE session_id = ANY (v_sessions);
        DELETE FROM sessions WHERE id = ANY (v_sessions) AND status = 'Ended';
    END IF;

    DELETE FROM vehicle_incidents WHERE vehicle_id IN (SELECT v.id FROM vehicles v WHERE v.slug LIKE 'DEMO-XE-%');
    DELETE FROM vehicles WHERE slug LIKE 'DEMO-XE-%';
    DELETE FROM users u
    WHERE u.username IN ('demo_an', 'demo_binh', 'demo_chi')
      AND NOT EXISTS (SELECT 1 FROM audit_logs a WHERE a.user_id = u.id)
      AND NOT EXISTS (SELECT 1 FROM sessions s WHERE s.created_by = u.id OR s.started_by = u.id OR s.ended_by = u.id);
END $$;
