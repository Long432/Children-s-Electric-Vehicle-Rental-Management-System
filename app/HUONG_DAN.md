# Hướng dẫn cấu hình và chạy ứng dụng (Windows)

Nhóm: Đỗ Hoàng Long (nhóm trưởng), Trần Đức Mạnh, Nguyễn Ngọc Sơn, Nguyễn Đức Duy, Nguyễn Phan Anh.

Ứng dụng viết bằng React Native (Expo). Vì có quảng cáo AdMob (module native) nên **không chạy bằng Expo Go**, phải build bản development lên máy ảo hoặc điện thoại Android.

## 1. Cài sẵn trên máy

| Phần mềm | Ghi chú |
|---|---|
| Node.js bản LTS | Kiểm tra: `node -v` |
| Git | Kiểm tra: `git --version` |
| Android Studio | Cài kèm Android SDK, tạo máy ảo Pixel trong Device Manager |
| JDK 17 (Eclipse Temurin) | `winget install EclipseAdoptium.Temurin.17.JDK`. Bắt buộc dùng 17, Java 25/26 sẽ lỗi khi build |
| PostgreSQL | Chỉ cần khi chạy backend |

## 2. Lấy mã nguồn và đặt ở thư mục có đường dẫn NGẮN

Đường dẫn quá dài (hơn 250 ký tự) làm bước biên dịch C++ bị lỗi. Hãy đặt app ở thư mục ngắn, ví dụ `D:\ev`:

```
git clone <link-github-cua-nhom> D:\evr
xcopy D:\evr\app D:\ev /E /I
cd D:\ev
```

Hoặc giải nén file `ev-rental-app.zip` thẳng vào `D:\ev`. Không để trong thư mục có tên dài như `Children-s-Electric-Vehicle-Rental-Management-System-main`.

## 3. Cài thư viện

```
cd D:\ev
npm install
npx expo install --fix
```

## 4. Cấu hình môi trường (mỗi người chỉnh theo máy mình)

**a) Đường dẫn JDK 17.** Tìm thư mục cài bằng lệnh:

```
Get-ChildItem "C:\Program Files\Eclipse Adoptium" -Directory | Select-Object FullName
```

**b) Đường dẫn Android SDK.** Mở Android Studio, vào Settings > Languages & Frameworks > Android SDK, sao chép ô "Android SDK Location".

**c) Đặt biến môi trường** (thay đường dẫn bằng của bạn). Cách tạm, chỉ có hiệu lực trong cửa sổ PowerShell đang mở:

```
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-17.0.xx-hotspot"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
$env:ANDROID_HOME = "D:\duong\dan\toi\Android\Sdk"
java -version
```

Lệnh `java -version` phải in ra `17.x`. Nếu chưa ra 17 thì đừng build.

Cách cố định, không phải gõ lại: tạo hoặc mở file `C:\Users\<tên bạn>\.gradle\gradle.properties` và thêm một dòng (dùng dấu `/`):

```
org.gradle.java.home=C:/Program Files/Eclipse Adoptium/jdk-17.0.xx-hotspot
```

## 5. Build và chạy app lần đầu

1. Mở máy ảo Pixel trong Android Studio (hoặc cắm điện thoại Android, bật USB debugging).
2. Trong PowerShell (đã đặt biến ở mục 4):

```
cd D:\ev
npx expo run:android
```

Lần đầu mất khoảng 5 đến 15 phút vì phải tải và biên dịch. Khi xong, app "Thuê xe điện trẻ em" tự mở. Đăng nhập bằng tài khoản đã tạo trong cơ sở dữ liệu (cần chạy backend ở mục 7 trước).

## 6. Các lần chạy sau (không cần build lại)

```
cd D:\ev
npx expo start --dev-client
```

Rồi bấm phím `a` để mở trên máy ảo. Chỉ cần chạy lại `npx expo run:android` khi thêm hoặc xóa thư viện native, hoặc sửa `app.json`.

## 7. Chạy backend (Spring Boot)

1. Tạo cơ sở dữ liệu `childrenev` trong PostgreSQL, chạy `backend/DB/InitDb.sql`, rồi các file `Patch_01` đến `Patch_04` theo thứ tự.
2. Trong thư mục `backend/ChildrenElectricVehicleRentalManagement` chạy một lần `.\setup-local.ps1` (nhập mật khẩu PostgreSQL). Script tạo `application-local.properties`, không đẩy file này lên GitHub.
3. Sao chép `backend/DB/Create_user.sql` thành `Create_user.local.sql`, điền mật khẩu rồi chạy để tạo tài khoản đăng nhập app.
4. Chạy bằng `.\mvnw spring-boot:run`. Backend mặc định ở cổng 8080.
5. Thử nhanh: gọi `POST http://localhost:8080/api/auth/login` với `{"username":"...","password":"..."}`, phải nhận `ok: true` kèm `token`.

## 8. Các file cấu hình trong app

| File | Thiết lập | Ý nghĩa |
|---|---|---|
| `src/api/backend.ts` | `BACKEND_URL` | Máy ảo Android: `http://10.0.2.2:8080`. Điện thoại thật: `http://<IP Wi-Fi của máy tính>:8080` (máy tính và điện thoại cùng mạng) |
| `src/config.ts` | `ADS_ENABLED` | Bật hoặc tắt quảng cáo |
| `src/config.ts` | `INTERSTITIAL_COOLDOWN_MS` | Thời gian chờ giữa hai lần hiện quảng cáo toàn màn hình (3 phút) |
| `src/config.ts` | `COUNTDOWN_WARNING_SECONDS` | Còn dưới số giây này thì đồng hồ vé đổi sang vàng và rung một lần (mặc định 300 giây) |
| `src/config.ts` | `COUNTDOWN_CRITICAL_SECONDS` | Còn dưới số giây này thì đồng hồ vé đổi sang đỏ và rung mạnh hơn (mặc định 60 giây) |
| `src/config.ts` | `VIBRATION_ENABLED`, `VIBRATION_PATTERNS` | Bật hoặc tắt rung, và kiểu rung cho từng mức (cảnh báo, sắp hết, quá giờ) |
| `src/config.ts` | `AD_UNITS` | Mã đơn vị quảng cáo (đang dùng mã thử nghiệm của Google) |
| `app.json` | `androidAppId`, `iosAppId` | App ID của AdMob (đang là mã thử nghiệm) |

## 8b. Thông báo cục bộ

App nhắc trước khi vé hết giờ (mặc định 5 phút, chỉnh `REMINDER_LEAD_MINUTES` trong `src/config.ts`) và đúng lúc vé quá giờ. Thông báo được lên lịch trên máy ngay khi gán xe hoặc gia hạn, không cần server, và bị hủy khi trả xe hoặc đăng xuất. Lần đầu dùng app hỏi quyền thông báo. Thư viện `expo-notifications` là module native nên sau khi cập nhật phải chạy `npm install` rồi `npx expo run:android` để build lại. `NOTIFICATIONS_ENABLED = false` để tắt.

## 9. Lỗi thường gặp

| Thông báo lỗi | Cách xử lý |
|---|---|
| `Unsupported class file major version 70` (hoặc 69) | Đang dùng Java 25/26. Trỏ `JAVA_HOME` về JDK 17 (mục 4), chạy `cd android; .\gradlew --stop; cd ..` rồi build lại |
| `JAVA_HOME is set to an invalid directory` | Sai đường dẫn JDK. Kiểm tra lại bằng `Test-Path "<đường dẫn>"` |
| `SDK location not found` | Chưa đặt `ANDROID_HOME` (mục 4b) |
| `Cannot find module 'babel-preset-expo'` | Chạy `npx expo install babel-preset-expo` |
| `No development build ... is installed` | Chưa build app lên máy. Chạy `npx expo run:android` (mục 5) |
| `ninja: error: mkdir(...)` hoặc cảnh báo `has ... characters` | Đường dẫn dự án quá dài. Chuyển sang thư mục ngắn (mục 2), xóa thư mục `android` rồi build lại |
| App kẹt ở màn hình chờ, terminal không có dòng `Bundling` | Chạy `npx expo start --dev-client --localhost`, hoặc chạy `adb reverse tcp:8081 tcp:8081`, rồi tải lại app |
| Báo `Không kết nối được ...` | Backend chưa chạy, hoặc `BACKEND_URL` sai (máy ảo phải dùng `10.0.2.2`, không dùng `localhost`) |
| Báo `Lỗi server` | Kiểm tra log của backend; thường do chưa kết nối được PostgreSQL |
| Quảng cáo không hiện | Máy ảo cần có mạng. Đợi khoảng 10 giây sau khi mở app để quảng cáo tải xong. Quảng cáo toàn màn hình chỉ hiện sau khi chuyển trang và cách lần trước ít nhất 3 phút |

## 10. Khi đẩy lên GitHub

File `.gitignore` đã loại `node_modules`, `android`, `ios`, `.expo`. Không đẩy các thư mục này và không đẩy mật khẩu cơ sở dữ liệu.
