CREATE EXTENSION IF NOT EXISTS pgcrypto;

DO $$
DECLARE
    v_today date := (now() AT TIME ZONE 'Asia/Ho_Chi_Minh')::date;
    v_counts int[] := ARRAY[16, 21, 14, 26, 19, 31, 23, 28];
    v_staff bigint[];
    v_vehicles bigint[];
    v_charge bigint;
    v_other bigint;
    v_actor bigint;
    v_handover boolean;
    v_day date;
    v_start timestamp;
    v_session bigint;
    v_s int;
    v_i int;
    v_kind int;
    v_minutes int;
    v_amount int;
    v_method varchar;
    v_status varchar;
    v_extra_minutes int;
    v_extra_amount int;
    v_overdue_minutes int;
    v_overdue_fee int;
    v_refund int;
    v_queued timestamp;
    v_ticket_start timestamp;
    v_due timestamp;
    v_returned timestamp;
    v_ticket bigint;
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns c WHERE c.table_name = 'sessions' AND c.column_name = 'started_by')
       OR to_regclass('vehicle_incidents') IS NULL THEN
        RAISE EXCEPTION 'Hay chay Patch_06_accountability.sql truoc khi chay file nay';
    END IF;
    IF EXISTS (SELECT 1 FROM tickets t WHERE t.phone LIKE '0900000%') THEN
        RAISE EXCEPTION 'Du lieu mau da ton tai, hay chay Seed_revenue_demo_remove.sql truoc khi chay lai';
    END IF;

    INSERT INTO users (username, password_hash, status, role)
    SELECT n.username, crypt('demo12345', gen_salt('bf', 10)), 'Offline', 'Staff'
    FROM (VALUES ('demo_an'), ('demo_binh'), ('demo_chi')) AS n(username)
    ON CONFLICT (username) DO NOTHING;

    SELECT array_agg(u.id ORDER BY u.username) INTO v_staff
    FROM users u WHERE u.username IN ('demo_an', 'demo_binh', 'demo_chi');

    INSERT INTO vehicles (slug, status)
    SELECT 'DEMO-XE-' || g.n, 'Offline'
    FROM generate_series(1, 3) AS g(n)
    WHERE NOT EXISTS (SELECT 1 FROM vehicles v WHERE v.slug = 'DEMO-XE-' || g.n AND v.deleted_at IS NULL);

    SELECT array_agg(v.id ORDER BY v.slug) INTO v_vehicles
    FROM vehicles v WHERE v.slug LIKE 'DEMO-XE-%' AND v.deleted_at IS NULL;

    FOR v_s IN 1..array_length(v_counts, 1) LOOP
        v_day := v_today - (array_length(v_counts, 1) + 1 - v_s);
        v_start := (v_day + time '08:00') - interval '7 hours';
        v_charge := v_staff[((v_s - 1) % 3) + 1];
        v_other := v_staff[(v_s % 3) + 1];
        v_handover := v_s % 4 = 0;

        INSERT INTO sessions (status, scheduled_date, scheduled_time, planned_end_at, started_at, ended_at, is_instant, next_ticket_no,
                              created_by, started_by, ended_by, created_at, updated_at)
        VALUES ('Ended', v_day, time '08:00', v_start + interval '15 hours', v_start, v_start + interval '14 hours 30 minutes', false, v_counts[v_s] + 1,
                CASE WHEN v_s % 2 = 0 THEN v_other ELSE v_charge END, v_charge, CASE WHEN v_handover THEN v_other ELSE v_charge END,
                v_start - interval '1 day', v_start + interval '14 hours 30 minutes')
        RETURNING id INTO v_session;

        FOR v_i IN 1..v_counts[v_s] LOOP
            v_kind := (v_i + v_s * 2) % 9;
            v_actor := CASE WHEN v_handover AND v_i > v_counts[v_s] / 2 THEN v_other ELSE v_charge END;
            v_minutes := (ARRAY[30, 60, 30, 15, 60, 30])[((v_i + v_s) % 6) + 1];
            v_amount := CASE v_minutes WHEN 15 THEN 30000 WHEN 30 THEN 50000 ELSE 90000 END;
            v_method := CASE WHEN (v_i * 3 + v_s) % 5 < 3 THEN 'Cash' ELSE 'Bank' END;
            v_status := CASE WHEN v_kind = 0 THEN 'Canceled' ELSE 'Completed' END;
            v_extra_minutes := CASE WHEN v_kind = 3 THEN 30 ELSE 0 END;
            v_extra_amount := CASE WHEN v_kind = 3 THEN 50000 ELSE 0 END;
            v_overdue_minutes := CASE WHEN v_kind IN (2, 5) THEN 8 + (v_i % 4) * 3 ELSE 0 END;
            v_overdue_fee := CASE WHEN v_overdue_minutes > 0 THEN (v_overdue_minutes - 5) * 2000 ELSE 0 END;
            v_refund := CASE WHEN v_kind = 0 THEN v_amount WHEN v_kind = 1 THEN v_amount / 10 ELSE 0 END;
            v_queued := v_start + (v_i * 20) * interval '1 minute';
            v_ticket_start := CASE WHEN v_kind = 0 THEN NULL ELSE v_queued + interval '2 minutes' END;
            v_due := CASE WHEN v_kind = 0 THEN NULL ELSE v_ticket_start + (v_minutes + v_extra_minutes) * interval '1 minute' END;
            v_returned := CASE
                WHEN v_kind = 0 THEN NULL
                WHEN v_kind = 1 THEN v_due - interval '5 minutes'
                ELSE v_due + v_overdue_minutes * interval '1 minute'
            END;

            INSERT INTO tickets (session_id, ticket_no, phone, status, vehicle_id, payment_method, total_minutes, amount_paid, price_amount, price_minutes,
                                 overdue_fee_per_minute, overdue_grace_minutes, refund_enabled, queued_at, started_at, due_at, returned_at,
                                 overdue_minutes, overdue_fee, refund_amount, canceled_at, created_by, returned_by, canceled_by, created_at, updated_at)
            VALUES (v_session, v_i, '0900000' || lpad((v_s * 100 + v_i)::text, 3, '0'), v_status, NULL, v_method, v_minutes + v_extra_minutes, v_amount + v_extra_amount, v_amount, v_minutes,
                    2000, 5, true, v_queued, v_ticket_start, v_due, v_returned,
                    v_overdue_minutes, v_overdue_fee, v_refund, CASE WHEN v_kind = 0 THEN v_queued + interval '5 minutes' ELSE NULL END,
                    v_actor, CASE WHEN v_kind = 0 THEN NULL ELSE v_actor END, CASE WHEN v_kind = 0 THEN v_actor ELSE NULL END, v_queued,
                    COALESCE(v_returned, v_queued + interval '5 minutes'))
            RETURNING id INTO v_ticket;

            INSERT INTO payments (session_id, ticket_id, type, method, amount, minutes, created_by, created_at)
            VALUES (v_session, v_ticket, 'Ticket', v_method, v_amount, v_minutes, v_actor, v_queued);

            IF v_kind = 3 THEN
                INSERT INTO payments (session_id, ticket_id, type, method, amount, minutes, created_by, created_at)
                VALUES (v_session, v_ticket, 'Extension', v_method, v_extra_amount, v_extra_minutes, v_actor, v_ticket_start + interval '10 minutes');
            END IF;

            IF v_overdue_fee > 0 THEN
                INSERT INTO payments (session_id, ticket_id, type, method, amount, minutes, created_by, created_at)
                VALUES (v_session, v_ticket, 'OverdueFee', v_method, v_overdue_fee, v_overdue_minutes - 5, v_actor, v_returned);
            END IF;

            IF v_refund > 0 THEN
                INSERT INTO payments (session_id, ticket_id, type, method, amount, minutes, created_by, created_at)
                VALUES (v_session, v_ticket, 'Refund', v_method, v_refund, CASE WHEN v_kind = 0 THEN v_minutes ELSE 5 END, v_actor, COALESCE(v_returned, v_queued + interval '5 minutes'));
            END IF;

            IF v_kind = 4 AND v_i % 2 = 0 THEN
                INSERT INTO vehicle_incidents (vehicle_id, session_id, ticket_id, source, reported_by, created_at)
                VALUES (v_vehicles[(v_i % 3) + 1], v_session, v_ticket, 'Ticket', v_actor, v_queued + interval '15 minutes');
            END IF;
        END LOOP;

        IF v_s % 3 = 0 THEN
            INSERT INTO vehicle_incidents (vehicle_id, session_id, ticket_id, source, reported_by, created_at)
            VALUES (v_vehicles[(v_s % 3) + 1], v_session, NULL, 'Manual', v_charge, v_start + interval '3 hours');
        END IF;
    END LOOP;

    INSERT INTO vehicle_incidents (vehicle_id, session_id, ticket_id, source, reported_by, created_at)
    VALUES (v_vehicles[2], NULL, NULL, 'Manual', v_staff[1], ((v_today - 3) + time '22:00') - interval '7 hours');
END $$;
