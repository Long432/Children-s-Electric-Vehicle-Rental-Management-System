import { RevenueChart } from '../components/RevenueChart';
import { Badge, Card, Empty, Kpi, Loading, Notice } from '../components/ui';
import { api } from '../lib/api';
import { dateTime, duration, fullDate, money } from '../lib/format';
import { useAsync } from '../lib/hooks';
import type { Filters } from '../lib/types';

export function OverviewPage({ filters }: { filters: Filters }) {
  const deps = [filters.from, filters.to, filters.userId] as const;
  const summary = useAsync(() => api.summary(filters), deps);
  const incidents = useAsync(() => api.incidents(filters, null, 5), deps);
  const data = summary.data;

  return (
    <>
      <h1 className="page-title">Tổng quan {fullDate(filters.from)} - {fullDate(filters.to)}</h1>
      {summary.error && <Notice kind="error">{summary.error}</Notice>}
      {summary.loading && !data && <Loading />}
      {data && (
        <>
          <div className="kpi-grid">
            <Kpi label="Tổng doanh thu" value={money(data.total_revenue)} tone="action" hint="Tiền thu trừ tiền hoàn" />
            <Kpi label="Tiền mặt" value={money(data.cash_revenue)} tone="go" />
            <Kpi label="Chuyển khoản" value={money(data.bank_revenue)} tone="action" />
            <Kpi label="Số phiên" value={String(data.session_count)} hint={`Làm ${duration(data.work_minutes)}`} />
            <Kpi label="Số vé" value={String(data.ticket_count)} hint={`${data.canceled_ticket_count} vé đã hủy`} />
            <Kpi label="Doanh thu mỗi giờ làm" value={money(data.revenue_per_hour)} />
            <Kpi label="Tiền hoàn" value={money(data.refund_total)} tone={data.refund_total > 0 ? 'stop' : undefined} />
            <Kpi label="Phụ phí quá giờ" value={money(data.overdue_fee)} />
            <Kpi label="Xe hỏng trong phiên" value={String(data.incident_count)} tone={data.incident_count > 0 ? 'stop' : undefined} />
          </div>
          <Card title="Doanh thu theo ngày">
            {data.session_count === 0 ? <Empty>Không có phiên nào trong khoảng thời gian này.</Empty> : <RevenueChart days={data.daily} />}
          </Card>
        </>
      )}
      <Card title="Xe hỏng gần đây" actions={<a className="link" href="#/vehicles">Xem tất cả</a>}>
        {incidents.error && <Notice kind="error">{incidents.error}</Notice>}
        {incidents.data && incidents.data.length === 0 && <Empty>Không có xe hỏng trong khoảng thời gian này.</Empty>}
        {incidents.data && incidents.data.length > 0 && (
          <div className="table-wrap">
            <table>
              <thead>
                <tr><th>Thời gian</th><th>Xe</th><th>Phiên</th><th>Người báo</th><th>Phụ trách phiên</th></tr>
              </thead>
              <tbody>
                {incidents.data.map((item) => (
                  <tr key={item.id}>
                    <td>{dateTime(item.created_at)}</td>
                    <td>{item.vehicle_slug}</td>
                    <td>{item.session_id ? <a className="link" href={`#/sessions/${item.session_id}`}>#{item.session_id}</a> : <span className="muted">Ngoài phiên</span>}</td>
                    <td>{item.reported_by.username} <Badge value={item.source} /></td>
                    <td>{item.session_in_charge?.username ?? '-'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </Card>
    </>
  );
}
