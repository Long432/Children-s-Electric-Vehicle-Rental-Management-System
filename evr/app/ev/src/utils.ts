import { COUNTDOWN_CRITICAL_SECONDS, COUNTDOWN_WARNING_SECONDS } from './config';
import { color } from './theme';
import { parseIso } from './time';
import type { Session, SessionStatus, Ticket, TicketStatus, VehicleStatus } from './types';

export const money = (n: number) => String(Math.round(n)).replace(/\B(?=(\d{3})+(?!\d))/g, '.') + 'đ';

export function shortMoney(n: number) {
  const abs = Math.abs(Math.round(n));
  const sign = n < 0 ? '-' : '';
  if (abs >= 999500) return `${sign}${String(Math.round(abs / 100000) / 10).replace('.', ',')}tr`;
  if (abs >= 1000) return `${sign}${Math.round(abs / 1000)}k`;
  return `${sign}${abs}`;
}

export const vehicleLabel: Record<VehicleStatus, [string, string]> = {
  Online: ['Sẵn sàng', color.go], Inused: ['Đang chạy', color.action], Repairing: ['Sửa chữa', color.wait],
  Offline: ['Nghỉ', color.idle], Deleted: ['Đã xóa', color.idle],
};
export const ticketLabel: Record<TicketStatus, [string, string]> = {
  Waiting: ['Chờ xe', color.wait], Inused: ['Đang chạy', color.go], Overdue: ['Quá giờ', color.stop],
  Completed: ['Hoàn thành', color.idle], Canceled: ['Đã hủy', color.idle],
};

export const sessionLabel: Record<SessionStatus, [string, string]> = {
  Waiting: ['Chờ tới giờ', color.wait], InProgress: ['Đang làm', color.go], Ended: ['Đã kết thúc', color.idle], Canceled: ['Đã hủy', color.idle],
};

export const sessionKind = (session: Session) => (session.is_instant ? 'Bắt đầu ngay' : 'Phiên hẹn giờ');

export function overPlannedEndMs(session: Session | null, nowMs: number) {
  if (!session || session.status !== 'InProgress') return null;
  const end = parseIso(session.planned_end_at);
  return end === null || nowMs < end ? null : nowMs - end;
}

export const isPastPlannedEnd = (session: Session | null, nowMs: number) => overPlannedEndMs(session, nowMs) !== null;

export type CountdownLevel = 'normal' | 'warning' | 'critical' | 'overdue';
export type AlertLevel = Exclude<CountdownLevel, 'normal'>;

export interface Countdown { level: CountdownLevel; seconds: number; text: string; speech: string }
export interface AlertScan { levels: Map<number, CountdownLevel>; strongest: AlertLevel | null }

export const countdownRank: Record<CountdownLevel, number> = { normal: 0, warning: 1, critical: 2, overdue: 3 };
export const countdownTint: Record<CountdownLevel, string> = {
  normal: color.go, warning: color.wait, critical: color.stop, overdue: color.stop,
};

const PHONE_LENGTH = 10;
const PHONE_FORMAT = /^0[35789]\d{8}$/;

export function normalizePhone(raw: string) {
  const value = raw.replace(/[\s.-]/g, '');
  if (value.startsWith('+84')) return `0${value.slice(3)}`;
  if (value.startsWith('84') && value.length === PHONE_LENGTH + 1) return `0${value.slice(2)}`;
  return value;
}

export function phoneError(raw: string): string | null {
  const value = normalizePhone(raw);
  if (!value) return null;
  if (!/^\d+$/.test(value)) return 'Số điện thoại chỉ được chứa chữ số';
  if (value.length !== PHONE_LENGTH) return `Số điện thoại phải gồm đúng ${PHONE_LENGTH} chữ số`;
  if (!PHONE_FORMAT.test(value)) return 'Số điện thoại phải bắt đầu bằng 03, 05, 07, 08 hoặc 09';
  return null;
}

export const isPhoneValid = (raw: string) => PHONE_FORMAT.test(normalizePhone(raw));

export const isRunning = (status: TicketStatus) => status === 'Inused' || status === 'Overdue';

export const effectiveStatus = (ticket: Ticket, nowMs: number): TicketStatus =>
  ticket.status === 'Inused' && ticket.due_at && Date.parse(ticket.due_at) <= nowMs ? 'Overdue' : ticket.status;

const pad = (n: number) => String(n).padStart(2, '0');

export function formatClock(totalSeconds: number) {
  const hours = Math.floor(totalSeconds / 3600);
  const minutes = Math.floor((totalSeconds % 3600) / 60);
  const seconds = totalSeconds % 60;
  return hours > 0 ? `${hours}:${pad(minutes)}:${pad(seconds)}` : `${pad(minutes)}:${pad(seconds)}`;
}

function speak(totalSeconds: number) {
  const hours = Math.floor(totalSeconds / 3600);
  const minutes = Math.floor((totalSeconds % 3600) / 60);
  const seconds = totalSeconds % 60;
  return `${hours > 0 ? `${hours} giờ ` : ''}${minutes} phút ${seconds} giây`;
}

export function countdownAt(due: string, nowMs: number): Countdown {
  const diff = Date.parse(due) - nowMs;
  if (Number.isNaN(diff)) return { level: 'normal', seconds: 0, text: '--:--', speech: 'Chưa có giờ hẹn trả' };
  if (diff <= 0) {
    const seconds = Math.floor(-diff / 1000);
    return { level: 'overdue', seconds, text: `+${formatClock(seconds)}`, speech: `Quá giờ ${speak(seconds)}` };
  }
  const seconds = Math.ceil(diff / 1000);
  const level: CountdownLevel = seconds <= COUNTDOWN_CRITICAL_SECONDS ? 'critical' : seconds <= COUNTDOWN_WARNING_SECONDS ? 'warning' : 'normal';
  return { level, seconds, text: formatClock(seconds), speech: `Còn ${speak(seconds)}` };
}

export function scanAlerts(previous: ReadonlyMap<number, CountdownLevel>, tickets: Ticket[], nowMs: number): AlertScan {
  const levels = new Map(previous);
  let strongest: AlertLevel | null = null;
  for (const ticket of tickets) {
    if (!ticket.due_at || !isRunning(ticket.status)) continue;
    const { level } = countdownAt(ticket.due_at, nowMs);
    const before = previous.get(ticket.id);
    levels.set(ticket.id, level);
    if (level === 'normal' || before === undefined || countdownRank[level] <= countdownRank[before]) continue;
    if (strongest === null || countdownRank[level] > countdownRank[strongest]) strongest = level;
  }
  return { levels, strongest };
}
