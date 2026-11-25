# Gắn Firebase Analytics và Crashlytics

## Việc làm trên web (làm một lần)

1. Vào https://console.firebase.google.com, bấm Thêm dự án, đặt tên (ví dụ `ev-rental`). Giữ BẬT Google Analytics, bấm tiếp đến khi tạo xong.
2. Trong dự án, bấm biểu tượng Android để thêm ứng dụng. Ô tên gói điền đúng `org.group6.evrental`. Bấm Đăng ký ứng dụng.
3. Tải tệp `google-services.json` về và đặt vào thư mục `app` (cùng cấp với `app.json`). Các bước còn lại của Firebase trên web có thể bỏ qua, mã đã làm sẵn.
4. Menu trái, mục Build, chọn Crashlytics, bấm Enable Crashlytics.

## Việc làm trên máy

1. Chép các tệp trong gói này đè vào thư mục `app` của dự án (giữ nguyên vị trí thư mục con).
2. Chạy `npm install`.
3. Chạy `npx expo run:android` để build lại bản native. Lần này bắt buộc build lại vì Firebase có phần native.
4. Máy ảo phải là loại có Google Play (chọn ảnh hệ thống có biểu tượng Play Store).

## Xem sự kiện ngay (DebugView)

1. Máy ảo hoặc điện thoại đang chạy app, chạy lệnh:
   `adb shell setprop debug.firebase.analytics.app org.group6.evrental`
2. Dùng app: đăng nhập, tạo vé, trả xe, chuyển tab.
3. Trên web: Analytics, DebugView. Sự kiện hiện sau vài giây. Chụp ảnh ở đây.
4. Khi xong, tắt chế độ này: `adb shell setprop debug.firebase.analytics.app .none.`

Bảng Events thông thường sẽ có số liệu sau khoảng một ngày.

## Thử Crashlytics

1. Vào tab Cài đặt, bấm nút đỏ "Gửi lỗi thử cho Crashlytics" (chỉ hiện ở bản dev). App sẽ tự đóng.
2. Mở lại app. Lỗi được gửi lên ở lần mở lại.
3. Trên web: Crashlytics. Có thể mất vài phút mới hiện. Chụp ảnh ở đây.

## Danh sách sự kiện đã gắn

| Sự kiện | Khi nào ghi |
|---|---|
| login, logout | Đăng nhập, đăng xuất |
| screen_view | Chuyển giữa 5 tab |
| session_scheduled, session_started, session_ended | Hẹn, bắt đầu, kết thúc phiên |
| vehicle_added | Thêm xe |
| ticket_created (minutes, payment_method) | Tạo vé |
| ticket_assigned, ticket_extended, ticket_returned, ticket_cancelled | Gán xe, gia hạn, trả xe, hủy vé |

Sự kiện chỉ ghi khi API trả về thành công. Lỗi mạng hoặc lỗi không rõ được gửi sang Crashlytics dưới dạng lỗi không gây thoát app.

## Thêm sự kiện cho tính năng mới

Cách 1, tính năng gọi API: bọc hàm trong `src/api/index.ts` bằng `tracked`:

    xyz: tracked('ten_su_kien', (a: number) => call('POST', '/api/xyz', { a })),

Cách 2, sự kiện bất kỳ: `import { track } from './analytics'` rồi `track('ten_su_kien', { khoa: 'gia_tri' })`.

Quy ước: tên viết thường, gạch dưới, tối đa 40 ký tự. Không đưa số điện thoại hay thông tin cá nhân vào sự kiện.
