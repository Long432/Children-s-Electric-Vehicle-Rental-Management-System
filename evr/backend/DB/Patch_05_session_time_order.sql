DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'chk_sessions_planned_end_after_start') THEN
        ALTER TABLE sessions
            ADD CONSTRAINT chk_sessions_planned_end_after_start
            CHECK (planned_end_at IS NULL OR started_at IS NULL OR planned_end_at > started_at) NOT VALID;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'chk_sessions_ended_after_start') THEN
        ALTER TABLE sessions
            ADD CONSTRAINT chk_sessions_ended_after_start
            CHECK (ended_at IS NULL OR started_at IS NULL OR ended_at >= started_at) NOT VALID;
    END IF;
END $$;
