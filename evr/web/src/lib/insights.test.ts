import assert from 'node:assert/strict';
import { test } from 'node:test';
import { accountFlags } from './insights.ts';
import type { AccountRow } from './types.ts';

const row = (id: number, patch: Partial<AccountRow>): AccountRow => ({
  user_id: id, username: `u${id}`, role: 'Staff', status: 'Offline', session_count: 5, work_minutes: 2400, ticket_count: 100,
  canceled_ticket_count: 5, cash_revenue: 3000000, bank_revenue: 2000000, total_revenue: 5000000, overdue_fee: 0, refund_total: 200000,
  incident_count: 2, revenue_per_hour: 125000, ...patch,
});

test('flags only accounts well above the average', () => {
  const rows = [row(1, {}), row(2, {}), row(3, { canceled_ticket_count: 30, refund_total: 1500000, incident_count: 12 })];
  const flags = accountFlags(rows);
  assert.equal(flags.has(1), false);
  assert.equal(flags.has(2), false);
  assert.deepEqual(flags.get(3), ['Hủy vé nhiều', 'Hoàn tiền nhiều', 'Xe hỏng nhiều']);
});

test('ignores accounts with too little activity', () => {
  const rows = [row(1, {}), row(2, { ticket_count: 4, canceled_ticket_count: 4, session_count: 1, incident_count: 5 })];
  assert.equal(accountFlags(rows).has(2), false);
});

test('returns nothing for empty or all-zero data', () => {
  assert.equal(accountFlags([]).size, 0);
  const zero = row(1, { ticket_count: 0, canceled_ticket_count: 0, total_revenue: 0, refund_total: 0, session_count: 0, incident_count: 0 });
  assert.equal(accountFlags([zero]).size, 0);
});
