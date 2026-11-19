export const BUSINESS_ZONE = 'Asia/Ho_Chi_Minh';

export const money = (n: number) => String(Math.round(n)).replace(/\B(?=(\d{3})+(?!\d))/g, '.') + 'đ';

export function shortMoney(n: number) {
  const abs = Math.abs(Math.round(n));
  const sign = n < 0 ? '-' : '';
  if (abs >= 999500) return `${sign}${String(Math.round(abs / 100000) / 10).replace('.', ',')}tr`;
  if (abs >= 1000) return `${sign}${Math.round(abs / 1000)}k`;
  return `${sign}${abs}`;
}

export function duration(minutes: number) {
  const total = Math.max(0, Math.round(minutes));
  const hours = Math.floor(total / 60);
  const rest = total % 60;
  if (hours === 0) return `${rest} phút`;
  return rest === 0 ? `${hours} giờ` : `${hours} giờ ${rest} phút`;
}

export const dayMonth = (key: string) => `${key.slice(8, 10)}/${key.slice(5, 7)}`;

export const fullDate = (key: string) => `${key.slice(8, 10)}/${key.slice(5, 7)}/${key.slice(0, 4)}`;

export function dateTime(iso: string | null) {
  if (!iso) return '-';
  const date = new Date(iso);
  if (Number.isNaN(date.getTime())) return '-';
  const parts = new Intl.DateTimeFormat('en-GB', {
    timeZone: BUSINESS_ZONE,
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
    hour12: false,
  }).formatToParts(date);
  const get = (type: string) => parts.find((p) => p.type === type)?.value ?? '';
  return `${get('hour')}:${get('minute')} ${get('day')}/${get('month')}/${get('year')}`;
}

export function clock(iso: string | null) {
  const text = dateTime(iso);
  return text === '-' ? text : text.slice(0, 5);
}

export function todayKey(now = new Date()) {
  return new Intl.DateTimeFormat('en-CA', { timeZone: BUSINESS_ZONE }).format(now);
}

export function addDays(key: string, days: number) {
  const [y, m, d] = key.split('-').map(Number);
  const moved = new Date(Date.UTC(y, m - 1, d + days));
  return moved.toISOString().slice(0, 10);
}

export function monthStart(key: string) {
  return `${key.slice(0, 8)}01`;
}

const ACTIONS: Record<string, string> = {
  'session.create': 'Tạo phiên',
  'session.start': 'Bắt đầu phiên',
  'session.end': 'Kết thúc phiên',
  'session.update': 'Sửa phiên',
  'session.cancel': 'Hủy phiên',
  'ticket.create': 'Tạo vé',
  'ticket.assign': 'Gán xe cho vé',
  'ticket.extend': 'Gia hạn vé',
  'ticket.cancel': 'Hủy vé',
  'ticket.return': 'Trả xe',
  'ticket.change_vehicle': 'Đổi xe',
};

export const actionLabel = (action: string) => ACTIONS[action] ?? action;

export function payloadSummary(payload: string | null) {
  if (!payload) return '';
  try {
    const value: unknown = JSON.parse(payload);
    if (value === null || typeof value !== 'object' || Array.isArray(value)) return '';
    return Object.entries(value as Record<string, unknown>)
      .map(([key, item]) => `${key}: ${Array.isArray(item) ? item.join(', ') : String(item)}`)
      .join(' · ');
  } catch {
    return '';
  }
}
