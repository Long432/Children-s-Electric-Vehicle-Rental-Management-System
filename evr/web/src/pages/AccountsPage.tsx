import { useMemo, useState } from 'react';
import type { ReactNode } from 'react';
import { Badge, Card, Empty, Loading, Notice } from '../components/ui';
import { duration, fullDate, money } from '../lib/format';
import { accountFlags } from '../lib/insights';
import type { AccountRow, Filters } from '../lib/types';

interface Column {
  key: string;
  label: string;
  num?: boolean;
  sortValue: (row: AccountRow) => number | string;
  cell: (row: AccountRow, flags: string[]) => ReactNode;
}

const COLUMNS: Column[] = [
  { key: 'username', label: 'Tài khoản', sortValue: (r) => r.username.toLowerCase(), cell: (r) => <strong>{r.username}</strong> },
  { key: 'role', label: 'Vai trò', sortValue: (r) => r.role, cell: (r) => <Badge value={r.role} /> },
  { key: 'sessions', label: 'Phiên phụ trách', num: true, sortValue: (r) => r.session_count, cell: (r) => String(r.session_count) },
  { key: 'minutes', label: 'Thời gian làm', num: true, sortValue: (r) => r.work_minutes, cell: (r) => duration(r.work_minutes) },
  { key: 'tickets', label: 'Vé', num: true, sortValue: (r) => r.ticket_count, cell: (r) => String(r.ticket_count) },
  { key: 'canceled', label: 'Vé hủy', num: true, sortValue: (r) => r.canceled_ticket_count, cell: (r) => String(r.canceled_ticket_count) },
  { key: 'cash', label: 'Tiền mặt', num: true, sortValue: (r) => r.cash_revenue, cell: (r) => money(r.cash_revenue) },
  { key: 'bank', label: 'Chuyển khoản', num: true, sortValue: (r) => r.bank_revenue, cell: (r) => money(r.bank_revenue) },
  { key: 'total', label: 'Tổng thu', num: true, sortValue: (r) => r.total_revenue, cell: (r) => <strong>{money(r.total_revenue)}</strong> },
  { key: 'perHour', label: 'Thu mỗi giờ', num: true, sortValue: (r) => r.revenue_per_hour, cell: (r) => money(r.revenue_per_hour) },
  { key: 'refund', label: 'Hoàn tiền', num: true, sortValue: (r) => r.refund_total, cell: (r) => money(r.refund_total) },
  { key: 'overdue', label: 'Phụ phí', num: true, sortValue: (r) => r.overdue_fee, cell: (r) => money(r.overdue_fee) },
  { key: 'incidents', label: 'Xe hỏng', num: true, sortValue: (r) => r.incident_count, cell: (r) => String(r.incident_count) },
  {
    key: 'flags',
    label: 'Nên xem lại',
    sortValue: () => 0,
    cell: (_, flags) => (flags.length === 0 ? <span className="muted">-</span> : (
      <span className="flags">{flags.map((flag) => <span key={flag} className="flag">{flag}</span>)}</span>
    )),
  },
];

export function AccountsPage({ filters, accounts, loading, error, onPick }: {
  filters: Filters;
  accounts: AccountRow[] | null;
  loading: boolean;
  error: string | null;
  onPick: (userId: number) => void;
}) {
  const [sort, setSort] = useState<{ key: string; dir: 1 | -1 }>({ key: 'total', dir: -1 });
  const rows = accounts ?? [];
  const flags = useMemo(() => accountFlags(rows), [rows]);
  const sorted = useMemo(() => {
    const column = COLUMNS.find((c) => c.key === sort.key) ?? COLUMNS[0];
    return [...rows].sort((a, b) => {
      const x = column.sortValue(a);
      const y = column.sortValue(b);
      const order = typeof x === 'number' && typeof y === 'number' ? x - y : String(x).localeCompare(String(y));
      return order * sort.dir || a.username.localeCompare(b.username);
    });
  }, [rows, sort]);

  const toggle = (key: string) => setSort((s) => (s.key === key ? { key, dir: s.dir === 1 ? -1 : 1 } : { key, dir: key === 'username' || key === 'role' ? 1 : -1 }));

  return (
    <>
      <h1 className="page-title">Tài khoản {fullDate(filters.from)} - {fullDate(filters.to)}</h1>
      <Notice kind="info">
        Mỗi phiên tính cho tài khoản bắt đầu phiên (người phụ trách). Tổng thu của các tài khoản cộng lại bằng doanh thu ở trang Tổng quan.
        Cột "Nên xem lại" chỉ là gợi ý khi chỉ số cao hơn hẳn mức trung bình, cần đối chiếu thêm trước khi kết luận.
      </Notice>
      {error && <Notice kind="error">{error}</Notice>}
      {loading && !accounts && <Loading />}
      {accounts && accounts.length === 0 && <Empty>Chưa có tài khoản nào.</Empty>}
      {accounts && accounts.length > 0 && (
        <Card>
          <div className="table-wrap">
            <table className="clickable">
              <thead>
                <tr>
                  {COLUMNS.map((column) => (
                    <th key={column.key} className={column.num ? 'num' : undefined} onClick={column.key === 'flags' ? undefined : () => toggle(column.key)}>
                      {column.label}{sort.key === column.key ? (sort.dir === 1 ? ' ▲' : ' ▼') : ''}
                    </th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {sorted.map((row) => (
                  <tr key={row.user_id} onClick={() => onPick(row.user_id)}>
                    {COLUMNS.map((column) => (
                      <td key={column.key} className={column.num ? 'num' : undefined}>{column.cell(row, flags.get(row.user_id) ?? [])}</td>
                    ))}
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <p className="muted small">Bấm vào một dòng để xem các phiên của tài khoản đó.</p>
        </Card>
      )}
    </>
  );
}
