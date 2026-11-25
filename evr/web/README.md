# Web báo cáo thuê xe điện

Trang web cho máy tính, dùng chung backend với ứng dụng di động. Chủ cửa hàng xem tổng quan, từng tài khoản, từng phiên và xe hỏng. Nhân viên chỉ xem các phiên do mình phụ trách.

## Cần có

- Node.js 20 trở lên (chạy `npm test` cần Node 22.6 trở lên).
- Backend đã chạy ở `http://localhost:8080` và đã chạy `backend/DB/Patch_06_accountability.sql`.

## Chạy

```
cd web
npm install
npm run dev
```

Mở `http://localhost:3000` rồi đăng nhập bằng tài khoản đang dùng trên app. Backend mặc định cho phép trang chạy ở cổng 3000 (`app.cors.allowed-origins`). Đổi cổng hoặc tên miền thì sửa giá trị này trong `application.properties` hoặc `application-local.properties`.

Địa chỉ backend khác `http://localhost:8080`: sao chép `.env.example` thành `.env.local` rồi sửa `VITE_API_URL`.

## Lệnh khác

| Lệnh | Việc làm |
|---|---|
| `npm run typecheck` | Kiểm tra kiểu TypeScript |
| `npm run build` | Kiểm tra kiểu rồi build ra thư mục `dist` |
| `npm run preview` | Chạy thử bản build ở cổng 3000 |
| `npm test` | Chạy test các hàm tính toán (cột biểu đồ, định dạng, gợi ý nên xem lại) |

## Phân quyền

Cột `role` của bảng `users` là `Owner` hoặc `Staff`. `Patch_06` đặt tài khoản đầu tiên chưa bị khóa làm `Owner`, các tài khoản còn lại là `Staff`. Đổi vai trò:

```sql
UPDATE users SET role = 'Owner' WHERE username = 'ten_tai_khoan';
```

Tạo thêm tài khoản nhân viên:

```sql
CREATE EXTENSION IF NOT EXISTS pgcrypto;
INSERT INTO users (username, password_hash, status, role)
VALUES ('nhanvien1', crypt('matkhau-tu-8-ky-tu', gen_salt('bf', 10)), 'Offline', 'Staff');
```

## Cách tính

- Mỗi phiên tính cho tài khoản bắt đầu phiên (người phụ trách), nên tổng các tài khoản bằng tổng doanh thu.
- Doanh thu là tiền thu (vé, gia hạn, phụ phí quá giờ) trừ tiền hoàn, tách tiền mặt và chuyển khoản.
- Thời gian làm là từ lúc bắt đầu đến lúc kết thúc phiên. Phiên đang làm tính đến hiện tại.
- Xe hỏng gồm hai nguồn: đổi xe với trạng thái cũ "Đang sửa", và đánh dấu xe "Đang sửa" thủ công.
- Cột "Nên xem lại" chỉ gợi ý khi vé hủy, tiền hoàn hoặc số xe hỏng cao hơn hẳn mức trung bình, cần đối chiếu thêm trước khi kết luận.
