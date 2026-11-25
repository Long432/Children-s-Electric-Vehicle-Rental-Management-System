import React, { useRef, useState } from 'react';
import { Pressable, ScrollView, StyleSheet, View } from 'react-native';
import { api } from '../api';
import { layoutBars } from '../chart';
import { Card, Chips, ErrorBox, H, Screen, Sub, Txt } from '../components/ui';
import { useApi } from '../hooks';
import { color, font, fonts } from '../theme';
import { dateKeyDayMonth, dateKeyFull, formatVnFull } from '../time';
import type { SessionRevenue } from '../types';
import { money, shortMoney } from '../utils';

const LIMIT_OPTIONS = [{ value: 7, label: '7 phiên gần nhất' }, { value: 14, label: '14 phiên gần nhất' }];
const CHART_HEIGHT = 180;
const AXIS_WIDTH = 46;
const MIN_COLUMN = 36;
const MAX_BAR_WIDTH = 28;
const BAR_GAP = 10;
const TOP_PAD = 10;
const LABEL_HEIGHT = 22;
const TICK_HEIGHT = 14;

export default function StatsScreen() {
  const [limit, setLimit] = useState(7);
  const [picked, setPicked] = useState<number | null>(null);
  const revenue = useApi(() => api.sessionRevenue(limit), [limit]);
  const rows = revenue.data ?? [];
  const current = rows.find((r) => r.id === picked) ?? rows[rows.length - 1] ?? null;
  const firstLoad = revenue.loading && revenue.data === null && !revenue.error;
  const empty = revenue.data !== null && rows.length === 0;

  return (
    <Screen title="Thống kê" onRefresh={revenue.reload} refreshing={revenue.loading}>
      <Chips options={LIMIT_OPTIONS} value={limit} onChange={setLimit} />
      {revenue.error && <ErrorBox message={revenue.error.message} />}
      {firstLoad && <Card><Sub>Đang tải số liệu...</Sub></Card>}
      {empty && (
        <Card>
          <H>Chưa có phiên nào kết thúc</H>
          <Sub>Biểu đồ doanh thu sẽ hiện sau khi bạn kết thúc phiên đầu tiên.</Sub>
        </Card>
      )}
      {current && (
        <>
          <RevenueChart rows={rows} selectedId={current.id} onSelect={setPicked} />
          <Detail row={current} />
        </>
      )}
    </Screen>
  );
}

function RevenueChart({ rows, selectedId, onSelect }: { rows: SessionRevenue[]; selectedId: number; onSelect: (id: number) => void }) {
  const [width, setWidth] = useState(0);
  const scroller = useRef<ScrollView>(null);
  const layout = layoutBars(rows, CHART_HEIGHT);
  const column = Math.max(MIN_COLUMN, Math.floor((width - AXIS_WIDTH) / rows.length));
  const barWidth = Math.min(MAX_BAR_WIDTH, column - BAR_GAP);
  const plotHeight = TOP_PAD + layout.upHeight + layout.downHeight + LABEL_HEIGHT;
  const total = rows.reduce((sum, r) => sum + r.revenue_total, 0);

  return (
    <Card>
      <H>Doanh thu {rows.length} phiên gần nhất</H>
      <Txt style={s.total}>{money(total)}</Txt>
      <View style={s.legendRow}>
        <Legend tint={color.go} label="Tiền mặt" />
        <Legend tint={color.action} label="Chuyển khoản" />
      </View>
      <View style={s.chartRow} onLayout={(e) => setWidth(e.nativeEvent.layout.width)}>
        <View style={{ width: AXIS_WIDTH, height: plotHeight }}>
          {layout.ticks.map((tick, i) => (
            <Txt key={i} style={[s.tick, { top: TOP_PAD + tick.offset - TICK_HEIGHT / 2 }]}>{shortMoney(tick.value)}</Txt>
          ))}
        </View>
        <ScrollView ref={scroller} horizontal showsHorizontalScrollIndicator={false} style={s.scroller}
          onContentSizeChange={() => scroller.current?.scrollToEnd({ animated: false })}>
          <View style={{ width: column * rows.length, height: plotHeight }}>
            <View style={s.columns}>
              {rows.map((row, i) => {
                const bar = layout.bars[i];
                const selected = row.id === selectedId;
                return (
                  <Pressable key={row.id} onPress={() => onSelect(row.id)} accessibilityRole="button" accessibilityState={{ selected }}
                    accessibilityLabel={`Phiên ${row.id}, ngày ${dateKeyDayMonth(row.scheduled_date)}, tổng ${money(row.revenue_total)}, tiền mặt ${money(row.revenue_cash)}, chuyển khoản ${money(row.revenue_bank)}`}
                    style={[s.column, { width: column, height: plotHeight }, selected && s.columnSelected]}>
                    <View style={{ width: barWidth, height: layout.upHeight, justifyContent: 'flex-end' }}>
                      <View style={{ height: bar.bankUp, backgroundColor: color.action }} />
                      <View style={{ height: bar.cashUp, backgroundColor: color.go }} />
                    </View>
                    <View style={{ width: barWidth, height: layout.downHeight }}>
                      <View style={{ height: bar.cashDown, backgroundColor: color.go }} />
                      <View style={{ height: bar.bankDown, backgroundColor: color.action }} />
                    </View>
                    <Txt style={[s.label, selected && s.labelSelected]}>{dateKeyDayMonth(row.scheduled_date)}</Txt>
                  </Pressable>
                );
              })}
            </View>
            {layout.ticks.map((tick, i) => (
              <View key={i} pointerEvents="none" style={[s.gridline, { top: TOP_PAD + tick.offset }, tick.value === 0 && s.baseline]} />
            ))}
          </View>
        </ScrollView>
      </View>
      <Sub>
        Doanh thu = tiền thu (vé, gia hạn, phụ phí quá giờ) trừ tiền hoàn. Mỗi cột là một phiên đã kết thúc, mỗi màu là một cách thanh toán. Chạm vào cột để xem chi tiết.
        {layout.downHeight > 0 ? ' Phần dưới trục là khi tiền hoàn lớn hơn tiền thu của cách thanh toán đó.' : ''}
      </Sub>
    </Card>
  );
}

function Detail({ row }: { row: SessionRevenue }) {
  return (
    <Card>
      <H>Phiên #{row.id} · {dateKeyFull(row.scheduled_date)}</H>
      <Sub>Kết thúc lúc {formatVnFull(row.ended_at)}</Sub>
      <Line label="Tổng doanh thu" value={money(row.revenue_total)} strong />
      <Line label="Tiền mặt" value={money(row.revenue_cash)} tint={color.go} />
      <Line label="Chuyển khoản" value={money(row.revenue_bank)} tint={color.action} />
      <Line label="Số vé" value={String(row.ticket_count)} />
      <Line label="Phụ phí quá giờ (đã gồm trong tổng)" value={money(row.overdue_fees)} />
      <Line label="Tiền hoàn (đã trừ khỏi tổng)" value={money(row.refunded)} />
    </Card>
  );
}

const Line = ({ label, value, tint, strong }: { label: string; value: string; tint?: string; strong?: boolean }) => (
  <View style={s.line}>
    <View style={s.lineLabel}>
      {!!tint && <View style={[s.dot, { backgroundColor: tint }]} />}
      <Txt style={s.lineText}>{label}</Txt>
    </View>
    <Txt style={{ color: color.ink, fontFamily: strong ? fonts.titleBold : fonts.bodyBold, fontSize: strong ? 20 : font.body }}>{value}</Txt>
  </View>
);

const Legend = ({ tint, label }: { tint: string; label: string }) => (
  <View style={s.legend}>
    <View style={[s.dot, { backgroundColor: tint }]} />
    <Txt style={s.legendText}>{label}</Txt>
  </View>
);

const s = StyleSheet.create({
  total: { fontSize: 24, lineHeight: 32, fontFamily: fonts.titleBold, color: color.ink },
  legendRow: { flexDirection: 'row', gap: 16 },
  legend: { flexDirection: 'row', alignItems: 'center', gap: 6 },
  legendText: { fontSize: font.small, color: color.ink },
  dot: { width: 10, height: 10, borderRadius: 5 },
  chartRow: { flexDirection: 'row' },
  scroller: { flex: 1 },
  columns: { flexDirection: 'row' },
  column: { alignItems: 'center', paddingTop: TOP_PAD, borderRadius: 8 },
  columnSelected: { backgroundColor: color.bg },
  label: { fontSize: 11, lineHeight: LABEL_HEIGHT, height: LABEL_HEIGHT, color: color.sub, fontFamily: fonts.bodySemi },
  labelSelected: { color: color.ink, fontFamily: fonts.bodyBold },
  tick: { position: 'absolute', right: 6, height: TICK_HEIGHT, lineHeight: TICK_HEIGHT, fontSize: 11, color: color.sub, textAlign: 'right' },
  gridline: { position: 'absolute', left: 0, right: 0, height: 1, backgroundColor: color.line },
  baseline: { backgroundColor: color.idle },
  line: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', gap: 12 },
  lineLabel: { flexDirection: 'row', alignItems: 'center', gap: 8, flexShrink: 1 },
  lineText: { color: color.ink, flexShrink: 1 },
});
