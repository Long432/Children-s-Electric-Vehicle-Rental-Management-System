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

Mặc định `USE_MOCK = true` trong `src/config.ts`: app chạy bằng dữ liệu giả, đăng nhập bằng tên và mật khẩu bất kỳ.

## Nối backend thật
1. Đặt `USE_MOCK = false`, sửa `API_URL` thành IP máy chạy backend (không dùng localhost trên điện thoại thật).
2. Backend cần làm API đăng nhập và trả `data.token` (giá trị cookie `SESSION_TOKEN`). App lưu token và gửi lại qua header `Cookie`.
3. Hiện chỉ nhóm API Cài đặt có sẵn; dùng token `dev-token` (từ `Patch_01_settings.sql`) để thử.

## Cấu trúc
- `src/api/` gọi API (`index.ts`), xử lý `{ok,data,error}` (`client.ts`), dữ liệu giả (`mock.ts`)
- `src/types.ts` kiểu dữ liệu. Các response ngoài Settings là đề xuất, cần thống nhất với backend
- `src/screens/` 5 màn hình: Đăng nhập, Phiên, Vé, Xe, Cài đặt
- `src/components/ui.tsx` thành phần dùng chung; `src/theme.ts` màu và cỡ chữ
