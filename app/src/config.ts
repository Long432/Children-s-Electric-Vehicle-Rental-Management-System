// true = chạy bằng dữ liệu giả trong src/api/mock.ts (không cần backend).
// false = gọi backend thật tại API_URL.
export const USE_MOCK = true;

// Khi test trên điện thoại thật, thay localhost bằng IP máy chạy backend (vd http://192.168.1.10:8080).
export const API_URL = 'http://localhost:8080';

// ---- AdMob ----
// false = tắt hoàn toàn quảng cáo.
export const ADS_ENABLED = true;

// Mã dưới đây là mã THỬ NGHIỆM của Google (an toàn để chạy và quay video demo).
// Khi nộp bản thật: tạo app + ad unit trên https://admob.google.com rồi thay mã vào đây
// và thay androidAppId / iosAppId trong app.json. Không bấm vào quảng cáo thật khi đang phát triển.
export const AD_UNITS = {
  banner: { android: 'ca-app-pub-3940256099942544/6300978111', ios: 'ca-app-pub-3940256099942544/2934735716' },
  interstitial: { android: 'ca-app-pub-3940256099942544/1033173712', ios: 'ca-app-pub-3940256099942544/4411468910' },
};
// Quảng cáo toàn màn hình hiện tối đa 1 lần / 3 phút. Trong thời gian chờ, bấm chuyển trang sẽ chuyển ngay.
export const INTERSTITIAL_COOLDOWN_MS = 3 * 60 * 1000;
