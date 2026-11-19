import type { AccountRow } from './types';

export const MIN_TICKETS_FOR_FLAGS = 10;
export const MIN_SESSIONS_FOR_FLAGS = 3;
const FLOOR_RATE = 0.1;
const FACTOR = 1.5;

export function accountFlags(rows: AccountRow[]): Map<number, string[]> {
  const sum = (pick: (row: AccountRow) => number) => rows.reduce((total, row) => total + pick(row), 0);
  const tickets = sum((r) => r.ticket_count);
  const sessions = sum((r) => r.session_count);
  const gross = sum((r) => r.total_revenue + r.refund_total);
  const cancelAvg = tickets > 0 ? sum((r) => r.canceled_ticket_count) / tickets : 0;
  const refundAvg = gross > 0 ? sum((r) => r.refund_total) / gross : 0;
  const incidentAvg = sessions > 0 ? sum((r) => r.incident_count) / sessions : 0;

  const result = new Map<number, string[]>();
  for (const row of rows) {
    const flags: string[] = [];
    if (row.ticket_count >= MIN_TICKETS_FOR_FLAGS) {
      const cancelRate = row.canceled_ticket_count / row.ticket_count;
      if (cancelRate > FLOOR_RATE && cancelRate > cancelAvg * FACTOR) flags.push('Hủy vé nhiều');
    }
    const rowGross = row.total_revenue + row.refund_total;
    if (rowGross > 0 && row.ticket_count >= MIN_TICKETS_FOR_FLAGS) {
      const refundRate = row.refund_total / rowGross;
      if (refundRate > FLOOR_RATE && refundRate > refundAvg * FACTOR) flags.push('Hoàn tiền nhiều');
    }
    if (row.session_count >= MIN_SESSIONS_FOR_FLAGS) {
      const perSession = row.incident_count / row.session_count;
      if (perSession > 1 && perSession > incidentAvg * FACTOR) flags.push('Xe hỏng nhiều');
    }
    if (flags.length > 0) result.set(row.user_id, flags);
  }
  return result;
}
