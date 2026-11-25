import type { ReactNode } from 'react';

export function Card({ title, actions, children }: { title?: string; actions?: ReactNode; children: ReactNode }) {
  return (
    <section className="card">
      {(title || actions) && (
        <header className="card-head">
          {title && <h2>{title}</h2>}
          {actions}
        </header>
      )}
      {children}
    </section>
  );
}

export function Kpi({ label, value, hint, tone }: { label: string; value: string; hint?: string; tone?: 'go' | 'action' | 'stop' }) {
  return (
    <div className={`kpi${tone ? ` kpi-${tone}` : ''}`}>
      <span className="kpi-label">{label}</span>
      <strong className="kpi-value">{value}</strong>
      {hint && <span className="kpi-hint">{hint}</span>}
    </div>
  );
}

export function Notice({ kind, children }: { kind: 'error' | 'info'; children: ReactNode }) {
  return <div className={`notice notice-${kind}`}>{children}</div>;
}

export function Loading() {
  return <div className="loading">Đang tải số liệu...</div>;
}

export function Empty({ children }: { children: ReactNode }) {
  return <div className="empty">{children}</div>;
}

const STATUS_LABEL: Record<string, string> = {
  InProgress: 'Đang làm',
  Ended: 'Đã kết thúc',
  Waiting: 'Chờ',
  Canceled: 'Đã hủy',
  Online: 'Sẵn sàng',
  Offline: 'Nghỉ',
  Repairing: 'Đang sửa',
  Inused: 'Đang dùng',
  Owner: 'Chủ',
  Staff: 'Nhân viên',
  Ticket: 'Khi đổi xe',
  Manual: 'Đánh dấu thủ công',
};

const STATUS_TONE: Record<string, string> = {
  InProgress: 'go',
  Ended: 'idle',
  Repairing: 'stop',
  Online: 'go',
  Owner: 'action',
};

export function Badge({ value }: { value: string }) {
  return <span className={`badge badge-${STATUS_TONE[value] ?? 'idle'}`}>{STATUS_LABEL[value] ?? value}</span>;
}

export function Pager({ page, limit, total, onChange }: { page: number; limit: number; total: number; onChange: (page: number) => void }) {
  const pages = Math.max(1, Math.ceil(total / limit));
  return (
    <div className="pager">
      <button type="button" className="btn" disabled={page <= 1} onClick={() => onChange(page - 1)}>Trước</button>
      <span>Trang {page} / {pages} · {total} phiên</span>
      <button type="button" className="btn" disabled={page >= pages} onClick={() => onChange(page + 1)}>Sau</button>
    </div>
  );
}
