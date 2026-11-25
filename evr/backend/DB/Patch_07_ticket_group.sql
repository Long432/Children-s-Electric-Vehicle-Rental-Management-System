DO $$
BEGIN
    IF to_regclass('tickets') IS NULL THEN
        RAISE EXCEPTION 'Khong tim thay bang tickets, hay chay InitDb.sql truoc';
    END IF;
END $$;

ALTER TABLE tickets ADD COLUMN IF NOT EXISTS group_id bigint REFERENCES tickets (id);
ALTER TABLE tickets ADD COLUMN IF NOT EXISTS group_size int;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'chk_tickets_group') THEN
        ALTER TABLE tickets
            ADD CONSTRAINT chk_tickets_group
            CHECK ((group_id IS NULL AND group_size IS NULL) OR (group_id IS NOT NULL AND group_size >= 2));
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS ix_tickets_group_id ON tickets (group_id) WHERE group_id IS NOT NULL;
