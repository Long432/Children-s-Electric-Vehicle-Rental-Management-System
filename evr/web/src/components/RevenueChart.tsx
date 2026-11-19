import { useState } from 'react';
import { layoutStack } from '../lib/chart';
import { dayMonth, fullDate, money, shortMoney } from '../lib/format';
import type { DailyRevenue } from '../lib/types';

const PLOT_HEIGHT = 200;
const AXIS_WIDTH = 56;
const TOP_PAD = 12;
const LABEL_HEIGHT = 26;
const CASH_COLOR = '#12915F';
const BANK_COLOR = '#2457D6';

function barSizes(count: number) {
  if (count <= 31) return { bar: 22, gap: 10 };
  if (count <= 92) return { bar: 9, gap: 4 };
  return { bar: 3, gap: 1 };
}

export function RevenueChart({ days }: { days: DailyRevenue[] }) {
  const [hover, setHover] = useState<number | null>(null);
  const layout = layoutStack(days.map((d) => ({ cash: d.cash_revenue, bank: d.bank_revenue })), PLOT_HEIGHT);
  const { bar, gap } = barSizes(days.length);
  const step = bar + gap;
  const plotWidth = Math.max(days.length * step, 1);
  const baseY = TOP_PAD + layout.upHeight;
  const height = TOP_PAD + layout.upHeight + layout.downHeight + LABEL_HEIGHT;
  const labelEvery = Math.max(1, Math.ceil(52 / step));
  const shown = hover === null ? null : days[hover];

  return (
    <div className="chart">
      <div className="chart-legend">
        <span><i style={{ background: CASH_COLOR }} />Tiền mặt</span>
        <span><i style={{ background: BANK_COLOR }} />Chuyển khoản</span>
      </div>
      <div className="chart-scroll">
        <svg width={AXIS_WIDTH + plotWidth} height={height} role="img" aria-label="Biểu đồ doanh thu theo ngày">
          {layout.ticks.map((tick, index) => (
            <g key={index}>
              <line x1={AXIS_WIDTH} x2={AXIS_WIDTH + plotWidth} y1={TOP_PAD + tick.offset} y2={TOP_PAD + tick.offset} stroke={tick.value === 0 ? '#8A96A3' : '#D5DCE3'} strokeWidth={1} />
              <text x={AXIS_WIDTH - 8} y={TOP_PAD + tick.offset + 4} textAnchor="end" className="chart-tick">{shortMoney(tick.value)}</text>
            </g>
          ))}
          {days.map((day, index) => {
            const parts = layout.bars[index];
            const x = AXIS_WIDTH + index * step + gap / 2;
            return (
              <g key={day.date} onMouseEnter={() => setHover(index)} onMouseLeave={() => setHover(null)}>
                <rect x={AXIS_WIDTH + index * step} y={0} width={step} height={height - LABEL_HEIGHT} fill={hover === index ? '#EDF1F5' : 'transparent'} />
                <rect x={x} y={baseY - parts.cashUp} width={bar} height={parts.cashUp} fill={CASH_COLOR} />
                <rect x={x} y={baseY - parts.cashUp - parts.bankUp} width={bar} height={parts.bankUp} fill={BANK_COLOR} />
                <rect x={x} y={baseY} width={bar} height={parts.cashDown} fill={CASH_COLOR} />
                <rect x={x} y={baseY + parts.cashDown} width={bar} height={parts.bankDown} fill={BANK_COLOR} />
                {index % labelEvery === 0 && (
                  <text x={x + bar / 2} y={height - 8} textAnchor="middle" className="chart-tick">{dayMonth(day.date)}</text>
                )}
                <title>{`${fullDate(day.date)}: ${money(day.total_revenue)}`}</title>
              </g>
            );
          })}
        </svg>
      </div>
      <div className="chart-detail">
        {shown ? (
          <>
            <strong>{fullDate(shown.date)}</strong>
            <span>Tổng {money(shown.total_revenue)}</span>
            <span>Tiền mặt {money(shown.cash_revenue)}</span>
            <span>Chuyển khoản {money(shown.bank_revenue)}</span>
            <span>{shown.session_count} phiên · {shown.ticket_count} vé</span>
          </>
        ) : (
          <span className="muted">Rê chuột vào một cột để xem chi tiết ngày đó.</span>
        )}
      </div>
    </div>
  );
}
