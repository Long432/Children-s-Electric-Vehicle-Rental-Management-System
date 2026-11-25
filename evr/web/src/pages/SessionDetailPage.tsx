import { Badge, Card, Empty, Kpi, Loading, Notice } from '../components/ui';
import { api } from '../lib/api';
import { actionLabel, clock, dateTime, duration, fullDate, money, payloadSummary } from '../lib/format';
import { useAsync } from '../lib/hooks';

export function SessionDetailPage({ id }: { id: number }) {
  const result = useAsync(() => api.session(id), [id]);
  const data = result.data;
  const session = data?.session;

  return (
    <>
      <a className="link" href="#/sessions">← Danh sách phiên</a>
      <h1 className="page-title">Phiên #{id}</h1>
      {result.error && <Notice kind="error">{result.error}</Notice>}
      {result.loading && !data && <Loading />}
      {data && session && (
        <>
          <Card>
            <dl className="facts">
              <div><dt>Ngày</dt><dd>{fullDate(session.scheduled_date)} <Badge value={session.status} /></dd></div>
              <div><dt>Phụ trách (người bắt đầu)</dt><dd>{session.in_charge.username}</dd></div>
              <div><dt>Người tạo phiên</dt><dd>{session.created_by.username}</dd></div>
              <div><dt>Người kết thúc</dt><dd>{session.ended_by?.username ?? '-'}</dd></div>
              <div><dt>Giờ làm</dt><dd>{clock(session.started_at)} - {session.ended_at ? clock(session.ended_at) : 'đang làm'} ({duration(session.work_minutes)})</dd></div>
            </dl>
          </Card>
          <div className="kpi-grid">
            <Kpi label="Tổng thu" value={money(session.total_revenue)} tone="action" hint="Tiền thu trừ tiền hoàn" />
            <Kpi label="Tiền mặt" value={money(session.cash_revenue)} tone="go" />
            <Kpi label="Chuyển khoản" value={money(session.bank_revenue)} tone="action" />
            <Kpi label="Số vé" value={String(session.ticket_count)} hint={`${session.canceled_ticket_count} vé đã hủy`} />
            <Kpi label="Tiền hoàn" value={money(session.refund_total)} />
            <Kpi label="Phụ phí quá giờ" value={money(session.overdue_fee)} />
          </div>
          <Card title="Ai đã làm gì trong phiên">
            {data.participants.length === 0 ? <Empty>Chưa có thao tác nào.</Empty> : (
              <div className="table-wrap">
                <table>
                  <thead>
                    <tr><th>Tài khoản</th><th className="num">Vé đã tạo</th><th className="num">Xe đã trả</th><th className="num">Vé đã hủy</th><th className="num">Tiền mặt đã thu</th><th className="num">Chuyển khoản đã thu</th><th className="num">Tổng</th></tr>
                  </thead>
                  <tbody>
                    {data.participants.map((p) => (
                      <tr key={p.user_id}>
                        <td><strong>{p.username}</strong>{p.user_id === session.in_charge.id ? ' (phụ trách)' : ''}</td>
                        <td className="num">{p.tickets_created}</td>
                        <td className="num">{p.tickets_returned}</td>
                        <td className="num">{p.tickets_canceled}</td>
                        <td className="num">{money(p.cash_revenue)}</td>
                        <td className="num">{money(p.bank_revenue)}</td>
                        <td className="num"><strong>{money(p.total_revenue)}</strong></td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </Card>
          <Card title="Xe hỏng trong phiên">
            {data.incidents.length === 0 ? <Empty>Không có xe hỏng trong phiên này.</Empty> : (
              <div className="table-wrap">
                <table>
                  <thead>
                    <tr><th>Thời gian</th><th>Xe</th><th>Vé</th><th>Người báo</th><th>Cách ghi nhận</th></tr>
                  </thead>
                  <tbody>
                    {data.incidents.map((item) => (
                      <tr key={item.id}>
                        <td>{dateTime(item.created_at)}</td>
                        <td>{item.vehicle_slug}</td>
                        <td>{item.ticket_no !== null ? `#${item.ticket_no}` : '-'}</td>
                        <td>{item.reported_by.username}</td>
                        <td><Badge value={item.source} /></td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </Card>
          <Card title="Nhật ký thao tác">
            {data.timeline.length === 0 ? <Empty>Chưa có nhật ký.</Empty> : (
              <ol className="timeline">
                {data.timeline.map((event, index) => (
                  <li key={index}>
                    <span className="timeline-time">{dateTime(event.at)}</span>
                    <strong>{actionLabel(event.action)}</strong>
                    <span>{event.user.username}</span>
                    <span className="muted">{payloadSummary(event.payload)}</span>
                  </li>
                ))}
              </ol>
            )}
          </Card>
        </>
      )}
    </>
  );
}
