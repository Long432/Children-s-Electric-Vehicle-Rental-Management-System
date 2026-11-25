UPDATE users
SET status = 'Deactivate', cookie_hash = NULL, cookie_expires_at = NULL, updated_at = now()
WHERE username = 'dev';

ALTER TABLE ticket_vehicle_history ALTER COLUMN released_status DROP NOT NULL;

DO $$
DECLARE
    item record;
BEGIN
    FOR item IN
        SELECT * FROM (VALUES
            ('users', 'status', 'chk_users_status', ARRAY['Online', 'Offline', 'Deactivate']),
            ('vehicles', 'status', 'chk_vehicles_status', ARRAY['Online', 'Repairing', 'Offline', 'Inused', 'Deleted']),
            ('sessions', 'status', 'chk_sessions_status', ARRAY['Waiting', 'InProgress', 'Ended', 'Canceled']),
            ('tickets', 'status', 'chk_tickets_status', ARRAY['Waiting', 'Inused', 'Overdue', 'Canceled', 'Completed']),
            ('tickets', 'payment_method', 'chk_tickets_payment_method', ARRAY['Cash', 'Bank']),
            ('payments', 'type', 'chk_payments_type', ARRAY['Ticket', 'Extension', 'OverdueFee', 'Refund']),
            ('payments', 'method', 'chk_payments_method', ARRAY['Cash', 'Bank']),
            ('ticket_vehicle_history', 'released_status', 'chk_history_released_status', ARRAY['Online', 'Repairing'])
        ) AS t(table_name, column_name, constraint_name, allowed)
    LOOP
        IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = item.constraint_name) THEN
            EXECUTE format(
                'ALTER TABLE %I ADD CONSTRAINT %I CHECK (%I = ANY (%L::text[]))',
                item.table_name, item.constraint_name, item.column_name, item.allowed
            );
        END IF;
    END LOOP;
END $$;

DO $$
DECLARE
    slug_attnum smallint;
    item record;
    has_valid boolean := false;
BEGIN
    SELECT attnum INTO slug_attnum
    FROM pg_attribute
    WHERE attrelid = 'vehicles'::regclass AND attname = 'slug' AND NOT attisdropped;

    IF EXISTS (
        SELECT 1 FROM vehicles WHERE deleted_at IS NULL GROUP BY slug HAVING count(*) > 1
    ) THEN
        RAISE EXCEPTION 'Bang vehicles dang co slug trung nhau giua cac xe chua xoa, hay xu ly truoc khi chay patch nay';
    END IF;

    FOR item IN
        SELECT i.indexrelid::regclass::text AS index_ref,
               ic.relname AS index_name,
               c.conname AS constraint_name,
               pg_get_expr(i.indpred, i.indrelid) AS predicate
        FROM pg_index i
        JOIN pg_class ic ON ic.oid = i.indexrelid
        LEFT JOIN pg_constraint c ON c.conindid = i.indexrelid AND c.contype = 'u'
        WHERE i.indrelid = 'vehicles'::regclass
          AND i.indisunique
          AND NOT i.indisprimary
          AND slug_attnum = ANY (string_to_array(i.indkey::text, ' ')::smallint[])
    LOOP
        IF item.index_name = 'uq_vehicles_slug_active'
           AND item.predicate IN ('(deleted_at IS NULL)', 'deleted_at IS NULL') THEN
            has_valid := true;
        ELSIF item.constraint_name IS NOT NULL THEN
            EXECUTE format('ALTER TABLE vehicles DROP CONSTRAINT %I', item.constraint_name);
        ELSE
            EXECUTE format('DROP INDEX %s', item.index_ref);
        END IF;
    END LOOP;

    IF NOT has_valid THEN
        CREATE UNIQUE INDEX uq_vehicles_slug_active ON vehicles (slug) WHERE deleted_at IS NULL;
    END IF;
END $$;

DO $$
BEGIN
    IF (SELECT count(*) FROM sessions WHERE status = 'InProgress') > 1 THEN
        RAISE EXCEPTION 'Bang sessions dang co nhieu hon mot phien InProgress, hay xu ly truoc khi chay patch nay';
    END IF;
END $$;

CREATE UNIQUE INDEX IF NOT EXISTS uq_sessions_single_in_progress
    ON sessions ((1))
    WHERE status = 'InProgress';
