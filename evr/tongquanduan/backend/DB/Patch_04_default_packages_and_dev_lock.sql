CREATE EXTENSION IF NOT EXISTS pgcrypto;

DO $$
BEGIN
    IF to_regclass('price_packages') IS NULL THEN
        RAISE EXCEPTION 'Khong tim thay bang price_packages, hay chay InitDb.sql truoc';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM price_packages WHERE active) THEN
        INSERT INTO price_packages (amount, minutes, is_default, active, created_at, updated_at)
        VALUES
            (30000, 15, false, true, now() AT TIME ZONE 'UTC', now() AT TIME ZONE 'UTC'),
            (50000, 30, true, true, now() AT TIME ZONE 'UTC', now() AT TIME ZONE 'UTC'),
            (90000, 60, false, true, now() AT TIME ZONE 'UTC', now() AT TIME ZONE 'UTC');
    END IF;
END $$;

UPDATE users
SET status = 'Deactivate',
    password_hash = crypt(encode(gen_random_bytes(24), 'hex'), gen_salt('bf', 10)),
    cookie_hash = NULL,
    cookie_expires_at = NULL,
    updated_at = now()
WHERE username = 'dev';
