const HEADER_PRECISION_MS = 500;
const RESYNC_THRESHOLD_MS = 2000;

let offset = 0;
let synced = false;

export const serverNow = () => Date.now() + offset;

export const serverToLocal = (serverMs: number) => serverMs - offset;

export function syncServerTime(dateHeader: string | null) {
  if (!dateHeader) return;
  const server = Date.parse(dateHeader);
  if (Number.isNaN(server)) return;
  const next = server + HEADER_PRECISION_MS - Date.now();
  if (!synced || Math.abs(next - offset) > RESYNC_THRESHOLD_MS) {
    offset = next;
    synced = true;
  }
}

const MINUTE_MS = 60 * 1000;
const HOUR_MS = 60 * MINUTE_MS;
const DAY_MS = 24 * HOUR_MS;
const VN_OFFSET_MS = 7 * HOUR_MS;
const DATE_PATTERN = /^\d{4}-\d{2}-\d{2}$/;
const TIME_PATTERN = /^([01]\d|2[0-3]):[0-5]\d$/;

const two = (n: number) => String(n).padStart(2, '0');
const vnShift = (ms: number) => new Date(ms + VN_OFFSET_MS);

export function parseIso(iso: string | null | undefined) {
  if (!iso) return null;
  const ms = Date.parse(iso);
  return Number.isNaN(ms) ? null : ms;
}

export function vnTime(ms: number) {
  const d = vnShift(ms);
  return `${two(d.getUTCHours())}:${two(d.getUTCMinutes())}`;
}

export function vnDateKey(ms: number) {
  const d = vnShift(ms);
  return `${d.getUTCFullYear()}-${two(d.getUTCMonth() + 1)}-${two(d.getUTCDate())}`;
}

const vnDayMonth = (ms: number) => {
  const d = vnShift(ms);
  return `${two(d.getUTCDate())}/${two(d.getUTCMonth() + 1)}`;
};

export function formatVn(iso: string | null | undefined, nowMs: number) {
  const ms = parseIso(iso);
  if (ms === null) return '--:--';
  return vnDateKey(ms) === vnDateKey(nowMs) ? vnTime(ms) : `${vnTime(ms)} ${vnDayMonth(ms)}`;
}

export function formatVnFull(iso: string | null | undefined) {
  const ms = parseIso(iso);
  if (ms === null) return '--:--';
  return `${vnTime(ms)} ${vnDayMonth(ms)}/${vnShift(ms).getUTCFullYear()}`;
}

export function formatDuration(ms: number) {
  const totalMinutes = Math.max(0, Math.floor(ms / MINUTE_MS));
  const hours = Math.floor(totalMinutes / 60);
  const minutes = totalMinutes % 60;
  if (hours === 0) return `${minutes} phút`;
  return minutes === 0 ? `${hours} giờ` : `${hours} giờ ${minutes} phút`;
}

export function elapsedText(iso: string | null | undefined, nowMs: number) {
  const start = parseIso(iso);
  if (start === null) return '--';
  const elapsed = nowMs - start;
  return elapsed < MINUTE_MS ? 'dưới 1 phút' : formatDuration(elapsed);
}

export const vnMomentToMs = (date: string, time: string) => Date.parse(`${date}T${time}:00+07:00`);

export const vnDateAfter = (nowMs: number, days: number) => vnDateKey(nowMs + days * DAY_MS);

export function suggestSlot(nowMs: number) {
  const next = (Math.floor(nowMs / HOUR_MS) + 1) * HOUR_MS;
  const clock = vnTime(next);
  if (isWithinBusinessHours(clock)) return { date: vnDateKey(next), time: clock };
  return { date: vnDateAfter(next, clock < BUSINESS_OPEN ? 0 : 1), time: BUSINESS_OPEN };
}

export function checkSlot(date: string, time: string, nowMs: number): string | null {
  const day = date.trim();
  const clock = time.trim();
  if (!DATE_PATTERN.test(day)) return 'Ngày phải có dạng yyyy-MM-dd';
  if (!TIME_PATTERN.test(clock)) return 'Giờ phải có dạng HH:mm, ví dụ 08:30';
  const ms = vnMomentToMs(day, clock);
  if (Number.isNaN(ms) || vnDateKey(ms) !== day) return 'Ngày không hợp lệ';
  if (!isWithinBusinessHours(clock)) return 'Giờ hẹn phải nằm trong khung từ 06:00 đến 23:00';
  if (ms < Math.floor(nowMs / MINUTE_MS) * MINUTE_MS) return 'Giờ hẹn không được ở quá khứ';
  return null;
}

export const DEFAULT_END_TIME = '00:00';

export const DEFAULT_SESSION_HOURS = '8';

export const parseSessionHours = (text: string) => {
  const value = text.trim();
  return /^\d{1,2}$/.test(value) && Number(value) >= 1 && Number(value) <= 24 ? Number(value) : null;
};

export const BUSINESS_OPEN = '06:00';
export const BUSINESS_CLOSE = '23:00';

export const isWithinBusinessHours = (clock: string) => clock >= BUSINESS_OPEN && clock < BUSINESS_CLOSE;

export const plannedEndMs = (nowMs: number, hours: number) =>
  Math.min(nowMs + hours * HOUR_MS, vnMomentToMs(vnDateKey(nowMs), BUSINESS_CLOSE));

export function nextEndMs(time: string, nowMs: number) {
  const today = vnMomentToMs(vnDateKey(nowMs), time.trim());
  return today > nowMs ? today : vnMomentToMs(vnDateAfter(nowMs, 1), time.trim());
}

export const dateKeyDayMonth = (key: string) => `${key.slice(8, 10)}/${key.slice(5, 7)}`;

export const dateKeyFull = (key: string) => `${key.slice(8, 10)}/${key.slice(5, 7)}/${key.slice(0, 4)}`;
