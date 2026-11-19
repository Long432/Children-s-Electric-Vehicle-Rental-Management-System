export const MIN_SEGMENT_PX = 2;

export interface StackInput { cash: number; bank: number }
export interface StackBar { cashUp: number; bankUp: number; cashDown: number; bankDown: number }
export interface AxisTick { offset: number; value: number }
export interface StackLayout { upHeight: number; downHeight: number; bars: StackBar[]; ticks: AxisTick[] }

const positive = (n: number) => (n > 0 ? n : 0);
const negative = (n: number) => (n < 0 ? -n : 0);

export function layoutStack(items: StackInput[], height: number): StackLayout {
  let maxUp = 0;
  let maxDown = 0;
  for (const item of items) {
    maxUp = Math.max(maxUp, positive(item.cash) + positive(item.bank));
    maxDown = Math.max(maxDown, negative(item.cash) + negative(item.bank));
  }
  const span = maxUp + maxDown;
  const unit = span > 0 ? height / span : 0;
  const px = (value: number) => (value > 0 ? Math.max(MIN_SEGMENT_PX, Math.round(value * unit)) : 0);

  const bars = items.map((item): StackBar => ({
    cashUp: px(positive(item.cash)),
    bankUp: px(positive(item.bank)),
    cashDown: px(negative(item.cash)),
    bankDown: px(negative(item.bank)),
  }));
  const upHeight = Math.max(0, ...bars.map((b) => b.cashUp + b.bankUp));
  const downHeight = Math.max(0, ...bars.map((b) => b.cashDown + b.bankDown));

  const ticks: AxisTick[] = maxUp > 0
    ? [{ offset: 0, value: maxUp }, { offset: Math.round(upHeight / 2), value: maxUp / 2 }, { offset: upHeight, value: 0 }]
    : [{ offset: upHeight, value: 0 }];
  if (maxDown > 0) ticks.push({ offset: upHeight + downHeight, value: -maxDown });
  return { upHeight, downHeight, bars, ticks };
}
