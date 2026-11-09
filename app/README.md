# Ứng dụng quản lý thuê xe điện vui chơi (React Native + Expo)

## Chạy thử
    npm install
    npx expo install --fix     # căn lại phiên bản thư viện theo SDK Expo bạn đang dùng
    npx expo run:android       # build bản dev (cần Android Studio + máy ảo hoặc điện thoại cắm cáp)

AdMob dùng module native nên KHÔNG chạy đầy đủ trong Expo Go. Trong Expo Go app vẫn chạy bình thường,
chỉ hiện khung giữ chỗ thay cho quảng cáo. Nếu không có Android Studio, build bằng EAS:
    npm i -g eas-cli && eas login
    eas build --profile development --platform android
Cài file .apk tạo ra lên điện thoại, rồi chạy `npx expo start --dev-client`.

## AdMob
- `src/ads.tsx`: khởi tạo, `<AdBanner />` (nằm dưới cùng, hiện ở mọi màn hình), `showInterstitialThen(next)`:
  hiện quảng cáo toàn màn hình, người dùng tắt xong mới chạy `next` (chuyển trang). Tối đa 1 lần / 3 phút.
- Chuyển trang bằng nút: dùng `const go = useNavigateWithAd(); go('Vé')`. Chuyển bằng thanh tab: đã chặn trong `App.tsx`.
- `src/config.ts`: `ADS_ENABLED`, mã ad unit, `INTERSTITIAL_COOLDOWN_MS`. Đang dùng mã THỬ NGHIỆM của Google.
- `app.json`: `androidAppId` / `iosAppId` của plugin. Đang là App ID thử nghiệm.
- Chuyển sang quảng cáo thật: tạo app và ad unit tại https://admob.google.com, thay mã vào 2 file trên, build lại.
  Không tự bấm vào quảng cáo thật của mình (dễ bị khóa tài khoản).

## Nối backend thật
1. Chạy backend (PostgreSQL, các file trong `backend/DB`, `setup-local.ps1`, `mvnw spring-boot:run`) và tạo tài khoản đăng nhập bằng `Create_user.local.sql`.
2. Sửa `BACKEND_URL` trong `src/api/backend.ts`: máy ảo Android dùng `http://10.0.2.2:8080`, điện thoại thật dùng `http://<IP Wi-Fi của máy tính>:8080`.
3. Đăng nhập bằng tài khoản thật. App lưu JWT trong SecureStore và gửi qua header `Authorization: Bearer`.

## Cấu trúc
- `src/api/` gọi API (`index.ts`, có đổi dữ liệu backend sang kiểu của app), xử lý `{ok,data,error}` (`client.ts`)
- `src/types.ts` kiểu dữ liệu mà các màn hình dùng
- `src/screens/` 5 màn hình: Đăng nhập, Phiên, Vé, Xe, Cài đặt
- `src/components/ui.tsx` thành phần dùng chung; `src/theme.ts` màu và cỡ chữ
