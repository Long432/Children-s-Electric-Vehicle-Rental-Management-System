-- Patch 01: chạy MỘT LẦN sau InitDb.sql, trước khi chạy backend giai đoạn 1.
-- Chạy được lặp lại an toàn (IF NOT EXISTS / ON CONFLICT).

-- 1. Dòng cài đặt duy nhất (id luôn = 1) và chặn tạo thêm dòng khác
INSERT INTO settings (id) VALUES (1) ON CONFLICT (id) DO NOTHING;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'chk_settings_single') THEN
        ALTER TABLE settings ADD CONSTRAINT chk_settings_single CHECK (id = 1);
    END IF;
END $$;

-- 2. Các index bảo vệ dữ liệu (đã nêu trong api_list.txt mục 7 và phân tích tranh chấp dữ liệu)
-- 2a. Tên nhận diện xe chỉ cần duy nhất giữa các xe chưa bị xóa mềm
DROP INDEX IF EXISTS "uq_vehicles_slug_active";
CREATE UNIQUE INDEX IF NOT EXISTS "uq_vehicles_slug_active" ON "vehicles" ("slug") WHERE "deleted_at" IS NULL;

-- 2b. Chỉ một phiên InProgress tại một thời điểm
CREATE UNIQUE INDEX IF NOT EXISTS "uq_sessions_one_in_progress" ON "sessions" ("status") WHERE "status" = 'InProgress';

-- 2c. Một xe không thể thuộc hai vé đang chạy/quá hạn cùng lúc
CREATE UNIQUE INDEX IF NOT EXISTS "uq_tickets_vehicle_active" ON "tickets" ("vehicle_id") WHERE "status" IN ('Inused', 'Overdue');

-- 2d. Chỉ một gói giá mặc định đang áp dụng
CREATE UNIQUE INDEX IF NOT EXISTS "uq_price_packages_one_default" ON "price_packages" ("is_default") WHERE "is_default" AND "active";
