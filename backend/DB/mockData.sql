-- Dữ liệu mẫu (mock data) để chạy thử API.
-- Chạy SAU InitDb.sql. Có thể chạy nhiều lần (WHERE NOT EXISTS).
-- Dữ liệu này do admin seeding, không phải dữ liệu sinh ra từ người dùng khi sử dụng chương trình.

INSERT INTO users (username, password_hash, status)
SELECT 'admin', 'placeholder-chua-co-login', 'Offline'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'admin');

INSERT INTO price_packages (amount, minutes, is_default, active)
SELECT 15000, 30, true, true WHERE NOT EXISTS (SELECT 1 FROM price_packages);

INSERT INTO price_packages (amount, minutes, is_default, active)
SELECT 30000, 60, false, true WHERE (SELECT count(*) FROM price_packages) = 1;

INSERT INTO vehicles (slug, status)
SELECT s, 'Online' FROM (VALUES ('xe-gau-01'), ('xe-gau-02'), ('xe-gau-03')) AS v(s)
WHERE NOT EXISTS (SELECT 1 FROM vehicles WHERE slug = v.s);

INSERT INTO sessions (status, scheduled_date, scheduled_time, planned_end_at, started_at, is_instant, created_by)
SELECT 'InProgress', (now() at time zone 'utc')::date, (now() at time zone 'utc')::time,
       date_trunc('day', now() at time zone 'utc') + interval '1 day', now() at time zone 'utc', true,
       (SELECT id FROM users WHERE username = 'admin')
WHERE NOT EXISTS (SELECT 1 FROM sessions WHERE status = 'InProgress');

INSERT INTO session_vehicles (session_id, vehicle_id, added_at)
SELECT (SELECT id FROM sessions WHERE status = 'InProgress' ORDER BY id DESC LIMIT 1), v.id, now() at time zone 'utc'
FROM vehicles v
WHERE NOT EXISTS (
  SELECT 1 FROM session_vehicles sv
  WHERE sv.vehicle_id = v.id
    AND sv.session_id = (SELECT id FROM sessions WHERE status = 'InProgress' ORDER BY id DESC LIMIT 1));
