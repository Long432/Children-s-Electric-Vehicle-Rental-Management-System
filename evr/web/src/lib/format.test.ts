import assert from 'node:assert/strict';
import { test } from 'node:test';
import { actionLabel, addDays, clock, dateTime, dayMonth, duration, fullDate, monthStart, money, payloadSummary, shortMoney, todayKey } from './format.ts';

test('formats money', () => {
  assert.equal(money(154000), '154.000đ');
  assert.equal(money(-90000), '-90.000đ');
  assert.equal(money(0), '0đ');
  assert.equal(shortMoney(1979000), '2tr');
  assert.equal(shortMoney(1250000), '1,3tr');
  assert.equal(shortMoney(999999), '1tr');
  assert.equal(shortMoney(989500), '990k');
  assert.equal(shortMoney(-90000), '-90k');
  assert.equal(shortMoney(500), '500');
});

test('formats durations', () => {
  assert.equal(duration(0), '0 phút');
  assert.equal(duration(45), '45 phút');
  assert.equal(duration(60), '1 giờ');
  assert.equal(duration(510), '8 giờ 30 phút');
});

test('formats dates in Vietnam time', () => {
  assert.equal(dayMonth('2026-11-06'), '06/11');
  assert.equal(fullDate('2026-11-06'), '06/11/2026');
  assert.equal(dateTime('2026-11-10T01:00:00Z'), '08:00 10/11/2026');
  assert.equal(dateTime('2026-11-10T17:30:00Z'), '00:30 11/11/2026');
  assert.equal(clock('2026-11-10T01:00:00Z'), '08:00');
  assert.equal(dateTime(null), '-');
  assert.equal(dateTime('khong-hop-le'), '-');
});

test('computes business days', () => {
  assert.equal(todayKey(new Date('2026-11-10T20:00:00Z')), '2026-11-11');
  assert.equal(todayKey(new Date('2026-11-10T10:00:00Z')), '2026-11-10');
  assert.equal(addDays('2026-11-11', -29), '2026-10-13');
  assert.equal(addDays('2026-12-31', 1), '2027-01-01');
  assert.equal(addDays('2024-03-01', -1), '2024-02-29');
  assert.equal(monthStart('2026-11-11'), '2026-11-01');
});

test('labels audit actions and summarises payloads', () => {
  assert.equal(actionLabel('ticket.return'), 'Trả xe');
  assert.equal(actionLabel('x.y'), 'x.y');
  assert.equal(payloadSummary('{"ticket_no":3,"vehicles":["A","B"]}'), 'ticket_no: 3 · vehicles: A, B');
  assert.equal(payloadSummary(null), '');
  assert.equal(payloadSummary('khong phai json'), '');
  assert.equal(payloadSummary('[1,2]'), '');
});
