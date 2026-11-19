import { useState } from 'react';
import { Badge, Card, Empty, Loading, Notice, Pager } from '../components/ui';
import { api } from '../lib/api';
import { clock, dayMonth, duration, fullDate, money } from '../lib/format';
import { useAsync } from '../lib/hooks';
import { go } from '../lib/router';
import type { Filters } from '../lib/types';

const PAGE_SIZE = 20;

export function SessionsPage({ filters }: { filters: Filters }) {
  const [page, setPage] = useState(1);
  const result = useAsync(() => api.sessions(filters, page, PAGE_SIZE), [filters.from, filters.to, filters.userId, page]);
  const data = result.data;

  return (
    <>
      <h1 className="page-title">Các phiên {fullDate(filters.from)} - {fullDate(filters.to)}</h1>
      {result.error && <Notice kind="error">{result.error}</Notice>}
      {result.loading && !data && <Loading />}
      {data && data.items.length === 0 && <Empty>Không có phiên nào trong khoảng thời gian này.</Empty>}
      {data && data.items.length > 0 && (
        <Card>
          <div className="table-wrap">
            <table className="clickable">
              <thead>
                <tr>
                  <th>Phiên</th><th>Ngày</th><th>Phụ trách</th><th>Người tạo</th><th>Người kết thúc</th><th>Giờ làm</th>
                  <th className="num">Thời gian</th><th className="num">Vé</th><th className="num">Tiền mặt</th>
                  <th className="num">Chuyển khoản</th><th className="num">Tổng thu</th><th className="num">Xe hỏng</th>
                </tr>
              </thead>
              <tbody>
                {data.items.map((row) => (
                  <tr key={row.session_id} onClick={() => go(`/sessions/${row.session_id}`)}>
                    <td><strong>#{row.session_id}</strong> <Badge value={row.status} /></td>
                    <td>{dayMonth(row.scheduled_date)}</td>
                    <td>{row.in_charge.username}</td>
                    <td>{row.created_by.username}</td>
                    <td>{row.ended_by?.username ?? '-'}</td>
                    <td>{clock(row.started_at)} - {row.ended_at ? clock(row.ended_at) : 'đang làm'}</td>
                    <td className="num">{duration(row.work_minutes)}</td>
                    <td className="num">{row.ticket_count}{row.canceled_ticket_count > 0 ? ` (hủy ${row.canceled_ticket_count})` : ''}</td>
                    <td className="num">{money(row.cash_revenue)}</td>
                    <td className="num">{money(row.bank_revenue)}</td>
                    <td className="num"><strong>{money(row.total_revenue)}</strong></td>
                    <td className="num">{row.incident_count}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <Pager page={data.page} limit={data.limit} total={data.total} onChange={setPage} />
        </Card>
      )}
    </>
  );
}
