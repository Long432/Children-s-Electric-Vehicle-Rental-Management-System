import assert from 'node:assert/strict';
import { test } from 'node:test';
import { layoutStack, MIN_SEGMENT_PX } from './chart.ts';

test('stacks cash and bank scaled to the tallest bar', () => {
  const layout = layoutStack([{ cash: 600000, bank: 400000 }, { cash: 300000, bank: 200000 }], 180);
  assert.deepEqual(layout.bars[0], { cashUp: 108, bankUp: 72, cashDown: 0, bankDown: 0 });
  assert.deepEqual(layout.bars[1], { cashUp: 54, bankUp: 36, cashDown: 0, bankDown: 0 });
  assert.equal(layout.upHeight, 180);
  assert.equal(layout.downHeight, 0);
  assert.deepEqual(layout.ticks, [{ offset: 0, value: 1000000 }, { offset: 90, value: 500000 }, { offset: 180, value: 0 }]);
});

test('draws negative amounts below the axis', () => {
  const layout = layoutStack([{ cash: -90000, bank: 90000 }, { cash: 100000, bank: 50000 }], 150);
  assert.equal(layout.bars[0].cashUp, 0);
  assert.ok(layout.bars[0].cashDown > 0);
  assert.ok(layout.bars[0].bankUp > 0);
  assert.ok(layout.downHeight > 0);
  const last = layout.ticks[layout.ticks.length - 1];
  assert.equal(last.value, -90000);
  assert.equal(last.offset, layout.upHeight + layout.downHeight);
});

test('handles empty and all-zero input', () => {
  assert.deepEqual(layoutStack([], 100), { upHeight: 0, downHeight: 0, bars: [], ticks: [{ offset: 0, value: 0 }] });
  const zero = layoutStack([{ cash: 0, bank: 0 }], 100);
  assert.equal(zero.upHeight, 0);
  assert.deepEqual(zero.bars[0], { cashUp: 0, bankUp: 0, cashDown: 0, bankDown: 0 });
});

test('keeps tiny non-zero segments visible', () => {
  const layout = layoutStack([{ cash: 1000000, bank: 1 }], 180);
  assert.equal(layout.bars[0].bankUp, MIN_SEGMENT_PX);
});
