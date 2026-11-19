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

export const NOTIFICATIONS_ENABLED = true;
export const REMINDER_LEAD_MINUTES = 5;
export const TICKET_CHANNEL_ID = 'ticket-timer';

export const COUNTDOWN_WARNING_SECONDS = 5 * 60;
export const COUNTDOWN_CRITICAL_SECONDS = 60;

export const VIBRATION_ENABLED = true;
export const VIBRATION_PATTERNS = {
  warning: [0, 300],
  critical: [0, 400, 150, 400],
  overdue: [0, 600, 200, 600, 200, 600],
};
