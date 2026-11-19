import type { SessionRevenue } from './types';

export const MIN_SEGMENT_PX = 2;

export interface BarPixels { id: number; cashUp: number; bankUp: number; cashDown: number; bankDown: number }
export interface AxisTick { offset: number; value: number }
export interface ChartLayout { upHeight: number; downHeight: number; bars: BarPixels[]; ticks: AxisTick[] }

const positive = (n: number) => (n > 0 ? n : 0);
const negative = (n: number) => (n < 0 ? -n : 0);

export function layoutBars(rows: SessionRevenue[], height: number): ChartLayout {
  let maxUp = 0;
  let maxDown = 0;
  for (const row of rows) {
    maxUp = Math.max(maxUp, positive(row.revenue_cash) + positive(row.revenue_bank));
    maxDown = Math.max(maxDown, negative(row.revenue_cash) + negative(row.revenue_bank));
  }
  const span = maxUp + maxDown;
  const unit = span > 0 ? height / span : 0;
  const px = (value: number) => (value > 0 ? Math.max(MIN_SEGMENT_PX, Math.round(value * unit)) : 0);

  const bars = rows.map((row): BarPixels => ({
    id: row.id,
    cashUp: px(positive(row.revenue_cash)),
    bankUp: px(positive(row.revenue_bank)),
    cashDown: px(negative(row.revenue_cash)),
    bankDown: px(negative(row.revenue_bank)),
  }));
  const upHeight = Math.max(0, ...bars.map((b) => b.cashUp + b.bankUp));
  const downHeight = Math.max(0, ...bars.map((b) => b.cashDown + b.bankDown));

  const ticks: AxisTick[] = maxUp > 0
    ? [{ offset: 0, value: maxUp }, { offset: Math.round(upHeight / 2), value: maxUp / 2 }, { offset: upHeight, value: 0 }]
    : [{ offset: upHeight, value: 0 }];
  if (maxDown > 0) ticks.push({ offset: upHeight + downHeight, value: -maxDown });
  return { upHeight, downHeight, bars, ticks };
}
