# Cập nhật backend: bảo mật, đăng nhập, xe, cài đặt

## Cách áp dụng

1. Giải nén file này vào thư mục gốc của repo (nơi có thư mục `backend`), chọn ghi đè.
2. Mở PowerShell trong `backend\ChildrenElectricVehicleRentalManagement`, chạy một lần:
   `.\setup-local.ps1`
   Trên macOS hoặc Linux chạy `bash setup-local.sh` trong cùng thư mục. Nhập mật khẩu PostgreSQL. Script tạo file `application-local.properties` (đã được thêm vào `.gitignore`) chứa mật khẩu và khóa JWT ngẫu nhiên.
3. Trong pgAdmin, kết nối database `childrenev` và chạy:
   - `backend\DB\Patch_02_security_and_constraints.sql`
   - `backend\DB\Patch_03_user_tokens.sql` (bảng `user_tokens`, bắt buộc để đăng nhập hoạt động)
   - `backend\DB\Patch_04_default_packages_and_dev_lock.sql` (chèn 3 gói giá mặc định nếu chưa có gói nào đang bật, đổi mật khẩu tài khoản `dev` thành chuỗi ngẫu nhiên và khóa lại). Cần bảng `price_packages` do `InitDb.sql` tạo. Giá trong file chỉ là giá tạm, sửa lại bằng `PUT /api/settings` hoặc sửa file trước khi chạy
   - Sao chép `backend\DB\Create_user.sql` thành `backend\DB\Create_user.local.sql`, thay mật khẩu trong bản sao rồi chạy bản sao. File gốc để nguyên chỗ trống mật khẩu và tự báo lỗi nếu chạy khi chưa đổi. Mọi file `*.local.sql` trong thư mục `DB` đã được `.gitignore` bỏ qua, nên không bị commit nhầm.
4. Tùy chọn: `.\don-file-cu.ps1` để xóa các file cũ không còn dùng.
5. Chạy: `.\mvnw clean spring-boot:run`. Chạy kiểm thử: `.\mvnw test`.

## Kiểm tra nhanh

```
$login = Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/auth/login -ContentType 'application/json' -Body '{"username":"banhang","password":"MAT_KHAU"}'
$token = $login.data.token
Invoke-RestMethod http://localhost:8080/api/vehicles -Headers @{Authorization="Bearer $token"}
Invoke-RestMethod http://localhost:8080/api/vehicles
```

Lệnh cuối không có token phải trả lỗi 401.

## Đã sửa

| Nhận xét | Cách xử lý |
|---|---|
| Kiểm tra chữ ký JWT bị bỏ qua | `JwtUtil.parseUserId` kiểm tra thuật toán, chữ ký và hạn dùng |
| Mật khẩu CSDL và khóa JWT nằm trong repo | Chuyển sang `application-local.properties`, không còn khóa mặc định |
| `SecurityConfig` chỉ mở `/vehicle/**` | Chỉ `/api/auth/login` và `/error` được mở, mọi đường dẫn khác bắt buộc đăng nhập |
| Token sai trả 500 | Trả 401 theo khung `{ok, error}`, lỗi 403 và 429 cũng theo khung này |
| JWT sống 7 ngày, không thu hồi được | Lưu bản băm token trong bảng `user_tokens`, đăng xuất làm token đó mất hiệu lực, tài khoản bị khóa bị từ chối ngay |
| Bộ lọc tốc độ tin `X-Forwarded-For`, bản đồ đếm lớn dần, đăng nhập không chặt hơn | Dùng địa chỉ kết nối thật, tự dọn bản ghi hết hạn, đăng nhập tối đa 10 lần mỗi phút, 429 có thân và `Retry-After` |
| Đường dẫn, tên trường, khung phản hồi, mã lỗi của module xe | Đúng hợp đồng: `/api/vehicles`, `slug`, `status`, `{ok, data}`, 409 và 422 |
| Luật trạng thái xe, xóa mềm, trùng tên | Chỉ đổi sang Online, Repairing, Offline; xe Inused không đổi trạng thái, đổi tên hay xóa; chỉ xe Offline hoặc Repairing được xóa; tên chỉ trùng trong xe chưa xóa |
| Thiếu dữ liệu gây 500, danh sách trả Stream | Mỗi request có validator riêng, danh sách trả mảng, lọc theo `?status=` |
| Giờ máy thay vì UTC, xóa xe không cập nhật `updated_at` | Dùng `TimeUtil.nowUtc()` |
| `varchar` không có CHECK, `released_status` NOT NULL | `Patch_02` thêm CHECK và cho phép NULL |
| Index tên xe không có điều kiện, báo nhầm `SLUG_TAKEN` | `Patch_02` tự tìm và xóa mọi index hoặc ràng buộc unique không điều kiện trên `vehicles.slug`, rồi tạo `uq_vehicles_slug_active` với `WHERE deleted_at IS NULL`. Chạy lại nhiều lần vẫn an toàn. Nếu đang có hai xe chưa xóa trùng tên, patch dừng và báo rõ |
| Mọi lỗi ràng buộc đều bị đổi thành `SLUG_TAKEN` | `VehicleService` chỉ trả `SLUG_TAKEN` khi lỗi đúng là vi phạm `uq_vehicles_slug_active`, lỗi khác được ném lại thay vì mang nhãn sai |
| Hai phiên chạy cùng lúc | `Patch_02` thêm `uq_sessions_single_in_progress`, CSDL chỉ cho một phiên `InProgress` |
| Mật khẩu thật có thể bị commit qua `Create_user.sql` | File gốc chỉ chứa chỗ trống kèm bước kiểm tra, mật khẩu phải có ít nhất 8 ký tự và không quá 72 byte, bản điền mật khẩu là `*.local.sql` và bị git bỏ qua |
| CORS thiếu PATCH | Có đủ phương thức, danh sách nguồn cấu hình qua `app.cors.allowed-origins` |
| Mọi lỗi ràng buộc đều bị đổi thành `SLUG_TAKEN` (đã xác nhận lại) | `saveChecked` chỉ đổi khi vi phạm đúng `uq_vehicles_slug_active`, các lỗi khác ném lại nguyên vẹn |
| Đổi trạng thái xe không khóa dòng | `VehicleRepository.findByIdForUpdate` khóa dòng (`PESSIMISTIC_WRITE`), `rename`, `changeStatus`, `delete` đều đọc xe qua hàm này. API gán xe cho vé sau này phải dùng chính hàm này |
| Token 12 giờ, không gia hạn | Hạn mặc định 24 giờ (`jwt.expiration-ms=86400000`), đủ cho ca 08:00 đến 24:00 |
| Mỗi tài khoản một phiên đăng nhập | Bảng `user_tokens` lưu bản băm từng token. Đăng nhập ở thiết bị khác không đá thiết bị cũ, đăng xuất chỉ thu hồi token đang dùng. Tài khoản chuyển sang Offline khi không còn token nào còn hạn. Tài khoản bị khóa (`Deactivate`) bị từ chối ngay |
| Bộ bắt lỗi chung nuốt lỗi framework thành 500 | `GlobalExceptionHandler` (`@RestControllerAdvice`) xử lý riêng: thiếu tham số 400, sai phương thức 405, sai kiểu dữ liệu gửi lên 415, không chấp nhận kiểu phản hồi 406, sai đường dẫn 404. Bộ bắt lỗi cũ nằm trong `BaseController` chỉ phủ lỗi sau khi đã chọn được controller nên không bắt được 405, 415, 404 |
| Phụ thuộc thư mục chạy và PowerShell | `application-local.properties` được tìm ở thư mục chạy, `ChildrenElectricVehicleRentalManagement/` và `backend/ChildrenElectricVehicleRentalManagement/`. Có thể thay bằng biến môi trường `JWT_SECRET` và `SPRING_DATASOURCE_PASSWORD`. Thiếu hoặc còn giá trị mẫu thì ứng dụng dừng với thông báo rõ. Thêm `setup-local.sh` cho macOS, Linux |
| Chưa có kiểm thử bảo mật | `JwtUtilTest` (chữ ký sai, hoán đổi payload, hết hạn, không ký, sai định dạng), `AuthServiceTest` (đăng xuất rồi gọi lại, hai thiết bị, tài khoản bị khóa, bản ghi hết hạn), `RatelimitFilterTest` (đăng nhập 10 lần mỗi phút, 30 yêu cầu mỗi 10 giây, không bị qua mặt bằng `X-Forwarded-For`), `JwtAuthenticationFilterTest` |
| Cài đặt (từ nhánh trước) | Đưa về `/api/settings/...` dùng xác thực JWT, kèm `POST /api/settings/logout` |
| `GET /api/settings` trả danh sách gói giá rỗng khi chưa có dữ liệu | `Patch_04` chèn 15 phút 30.000, 30 phút 50.000 (mặc định), 60 phút 90.000 nếu chưa có gói nào đang bật. Chạy lại nhiều lần vẫn an toàn |
| Mật khẩu có dấu vượt 72 byte của bcrypt dù chưa đủ 72 ký tự | `LoginRequestValidator` và `Create_user.sql` đếm theo byte UTF-8, thông báo lỗi nêu rõ đơn vị. Có `LoginRequestValidatorTest` |
| `Patch_01` tạo tài khoản `dev` với `dev-token` | `Patch_01_settings.sql` không còn phần tạo tài khoản này. Database đã chạy bản cũ thì `Patch_04` đặt mật khẩu ngẫu nhiên và giữ trạng thái `Deactivate`, còn `Patch_03` đã xóa token cũ nên `dev-token` không còn dùng được |
| `pom.xml` dùng Java 26 và còn actuator | Đổi `java.version` về 21 (chạy được trên JDK 21 trở lên), bỏ `spring-boot-starter-actuator`, thêm `spring-boot-starter-test` phạm vi test để chạy được `mvnw test` |
| Chưa lọc xe theo `session_id` | `GET /api/vehicles?session_id=` trả các xe đang thuộc phiên đó (có dòng trong `session_vehicles` chưa bị gỡ), không gồm xe đã xóa, kết hợp được với `status`. `session_id` nhỏ hơn 1 trả 422, phiên không tồn tại trả danh sách rỗng. Có `VehicleServiceListTest` |
| Sau reverse proxy mọi người dùng chung một giới hạn tốc độ | Thêm profile `proxy`, xem mục bên dưới |

## Chạy sau reverse proxy

Bộ lọc tốc độ dùng địa chỉ kết nối thật nên khi có proxy (Nginx, Caddy, dịch vụ đám mây) mọi người dùng sẽ cùng một địa chỉ và dùng chung giới hạn. Bật profile `proxy` để Tomcat lấy địa chỉ khách từ `X-Forwarded-For`, nhưng chỉ khi yêu cầu đến từ proxy tin cậy:

```
SPRING_PROFILES_ACTIVE=proxy
TRUSTED_PROXY_REGEX=10\.0\.0\.5
```

`TRUSTED_PROXY_REGEX` là biểu thức chính quy khớp địa chỉ của proxy, ví dụ `10\.0\.0\.5` hoặc `10\.0\.0\.\d{1,3}`. Thiếu biến này ứng dụng dừng với thông báo rõ. Yêu cầu không đến từ proxy này vẫn bị bỏ qua `X-Forwarded-For`, nên không giả mạo được địa chỉ. Proxy phải ghi đè hoặc nối thêm `X-Forwarded-For` chứ không chuyển nguyên giá trị khách gửi. Chạy trực tiếp không qua proxy thì không bật profile này.

## Lưu ý về quyền

Hiện chỉ có một vai trò, mọi tài khoản đăng nhập đều sửa được cài đặt. Khi thêm vai trò thứ hai cần thêm kiểm tra quyền cho `/api/settings`.

## Chưa làm

- Thanh toán (controller và service hiện vẫn rỗng). Cần chuyển 20 API ở nhánh `sus_backend_updated` từ cookie `EVR_SESSION` sang JWT (`@AuthenticationPrincipal`) và khung phản hồi `{ok, data}`.
- Index chặn tranh chấp "một xe một vé đang chạy" trên `ticket_vehicle_history`: cần cột thực tế trong schema (nằm ở `Patch_01`) mới viết đúng được.
- Đổi mật khẩu PostgreSQL đã lộ trên GitHub. Đây là việc phải làm thủ công: lịch sử git vẫn giữ bản cũ, nên chỉ đổi mật khẩu mới thật sự vô hiệu hóa nó. Sau khi đổi, chạy lại `setup-local.ps1` (xóa `application-local.properties` cũ trước) để cập nhật mật khẩu mới.

## Phiên và vé

API: `POST /api/sessions`, `POST /api/sessions/start`, `POST /api/sessions/current/end`, `GET /api/sessions/current`, `GET /api/sessions/current/stats`, `GET /api/sessions/{id}/stats`, `GET /api/sessions`, `GET /api/sessions/{id}`, `PATCH /api/sessions/{id}`, `POST /api/sessions/{id}/cancel`, `POST /api/tickets`, `GET /api/tickets`, `GET /api/tickets/search`, `GET /api/tickets/queue`, `GET /api/tickets/{id}`.

Quy ước đã chọn:
- Ngày `yyyy-MM-dd`, giờ `HH:mm` theo múi giờ nghiệp vụ `app.business-zone` (mặc định `Asia/Ho_Chi_Minh`). Thời điểm lưu và trả về vẫn là UTC. Ngày phiên không được ở quá khứ.
- `end_time` của `start` là giờ `HH:mm`, lấy lần tới gần nhất. Không gửi thì mặc định 00:00 hôm sau theo giờ nghiệp vụ.
- Bắt đầu phiên: chỉ nhận xe Online hoặc Offline (xe Repairing, Inused bị 409 `VEHICLE_NOT_AVAILABLE`), xe được chọn chuyển Online. Kết thúc phiên: xe Online về Offline, xe Repairing giữ nguyên.
- Giá vé: đúng số phút của một gói thì lấy giá gói đó, còn lại tính theo giá gói mặc định, làm tròn lên đồng. `price_amount` và `price_minutes` lưu gói mặc định lúc tạo để dùng cho gia hạn.
- Tìm vé: 1 đến 4 chữ số là mã vé, 5 đến 11 chữ số là số điện thoại (khớp một phần). Mặc định tìm trong phiên hiện tại, có thể truyền `session_id`.
- Danh sách, tìm kiếm, hàng chờ khi chưa có phiên đang làm trả kết quả rỗng. `GET /api/sessions/current` và tạo vé trả 409 `NO_ACTIVE_SESSION`.
- Xe đang thuộc phiên InProgress không xóa được (409 `VEHICLE_IN_SESSION`).

Đổi xe: `POST /api/tickets/{id}/change-vehicle` với `vehicle_slug` và `old_vehicle_status` (Online hoặc Repairing). Chỉ vé Inused hoặc Overdue trong phiên đang làm. Xe mới phải thuộc phiên và đang Online (409 `VEHICLE_NOT_AVAILABLE`, `VEHICLE_NOT_IN_SESSION`), không được trùng xe hiện tại (409 `VEHICLE_UNCHANGED`). Giờ bắt đầu và `due_at` giữ nguyên, nên thời gian còn lại không đổi. Xe cũ chuyển sang `old_vehicle_status`, xe mới thành Inused, lịch sử xe của vé đóng dòng cũ và mở dòng mới.

Thống kê phiên: `GET /api/sessions/current/stats` (409 `NO_ACTIVE_SESSION` khi chưa có phiên) và `GET /api/sessions/{id}/stats` (404 `SESSION_NOT_FOUND`). Số liệu lấy từ bảng `payments` theo `session_id`: `cash_revenue` và `bank_revenue` là tổng thu trừ hoàn tiền theo từng phương thức, `total_revenue` là tổng hai khoản đó, `ticket_revenue` là tiền vé và gia hạn, `overdue_fee` là phụ phí, `refund_total` là tiền đã hoàn, `ticket_count` và `ticket_counts` đếm vé theo trạng thái. `total_revenue = ticket_revenue + overdue_fee - refund_total`. Tạo vé ghi dòng `Ticket`, hủy vé ghi dòng `Refund`; `overdue_fee` và `Extension` chỉ có số liệu sau khi trả xe và gia hạn ghi dòng `OverdueFee`, `Extension`.

Gia hạn: `POST /api/tickets/{id}/extend` với `minutes` (1 đến 1440) và `payment_method` (Cash hoặc Bank). Chỉ vé Inused hoặc Overdue trong phiên đang làm, và phải bật gia hạn trong cài đặt (409 `EXTEND_DISABLED`). Tiền tính theo giá gói mặc định đã lưu trong vé, làm tròn lên đồng. Không áp dụng mức thuê tối thiểu cho gia hạn. `total_minutes`, `amount_paid` tăng thêm, `due_at` cộng thêm từ giờ kết thúc cũ (kể cả vé đã quá hạn), ghi dòng `Extension` vào `payments`. Vé sau khi cộng mà `due_at` vẫn ở quá khứ thì là Overdue, ngược lại là Inused. Trả về `ticket_no`, `extended_minutes`, `amount_paid` (số tiền của lần gia hạn này), `payment_method`, `ticket`.

Trả xe: `POST /api/tickets/{id}/return`, thân yêu cầu có thể bỏ trống, hoặc gồm `payment_method` (thu phụ phí) và `refund_method` (hoàn tiền), mặc định theo phương thức đã thu lúc tạo vé. Chỉ vé Inused hoặc Overdue (409 `TICKET_NOT_IN_USE` với trạng thái khác).
- Trễ so với `due_at`: số phút trễ tính tròn lên (trễ 6 phút 20 giây là 7 phút), phút tính phí là phần vượt ân hạn của vé, `overdue_fee` bằng phút tính phí nhân phụ phí mỗi phút của vé. Ghi dòng `OverdueFee`.
- Trả sớm và vé có `refund_enabled`: hoàn theo số phút còn lại làm tròn xuống, bằng `amount_paid` chia tỷ lệ theo `total_minutes` (nên vé mua gói giảm giá hoặc đã gia hạn vẫn không bao giờ hoàn quá số tiền đã trả). Ghi dòng `Refund`. Tắt hoàn tiền thì `refund_amount` là 0.
- Sau khi trả: vé Completed, lưu `returned_at`, `overdue_minutes`, `overdue_fee`, `refund_amount`, xe về Online, dòng lịch sử xe của vé được đóng (`released_status` Online).
- Phản hồi có `ticket_no`, `status`, `overdue_minutes`, `chargeable_overdue_minutes`, `overdue_fee`, `overdue_fee_method`, `refund_amount`, `refund_method`, `vehicle`, `next_ticket` và `ticket`. `next_ticket` (gồm `id`, `ticket_no`, `phone`) là vé Waiting đầu hàng chờ, `null` nếu hàng chờ trống. Chỉ là gợi ý, muốn gán xe thì gọi `POST /api/tickets/{id}/assign` với `vehicle.slug` vừa trả về.

Lưu ý: hệ thống chưa có tác vụ nền tự chuyển vé Inused sang Overdue khi quá giờ. Gia hạn và trả xe đều tính theo `due_at` thật nên kết quả vẫn đúng, nhưng danh sách vé theo `status=Overdue` và API `GET /api/tickets/overdue` trong `api_list.txt` thì chưa có.

Chưa làm: `GET /api/tickets/overdue`, tự chuyển vé sang Overdue, thêm hoặc bớt xe giữa phiên.
