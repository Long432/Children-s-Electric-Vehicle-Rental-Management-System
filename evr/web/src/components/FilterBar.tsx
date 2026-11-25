import { addDays, monthStart, todayKey } from '../lib/format';
import type { AccountRow, Filters } from '../lib/types';

const PRESETS = [
  { label: '7 ngày', days: 7 },
  { label: '30 ngày', days: 30 },
  { label: '90 ngày', days: 90 },
];

export function FilterBar({ filters, onChange, accounts, isOwner }: {
  filters: Filters;
  onChange: (next: Filters) => void;
  accounts: AccountRow[] | null;
  isOwner: boolean;
}) {
  const today = todayKey();
  const setRange = (from: string, to: string) => onChange({ ...filters, from, to });
  const activeDays = filters.to === today ? PRESETS.find((p) => addDays(today, -(p.days - 1)) === filters.from)?.days : undefined;
  const monthActive = filters.to === today && filters.from === monthStart(today);

  return (
    <div className="filters">
      <div className="filter-group">
        {PRESETS.map((preset) => (
          <button key={preset.days} type="button" className={`btn${activeDays === preset.days ? ' btn-on' : ''}`}
            onClick={() => setRange(addDays(today, -(preset.days - 1)), today)}>
            {preset.label}
          </button>
        ))}
        <button type="button" className={`btn${monthActive ? ' btn-on' : ''}`} onClick={() => setRange(monthStart(today), today)}>
          Tháng này
        </button>
      </div>
      <div className="filter-group">
        <label>Từ
          <input type="date" value={filters.from} max={filters.to} onChange={(e) => e.target.value && setRange(e.target.value, filters.to)} />
        </label>
        <label>Đến
          <input type="date" value={filters.to} min={filters.from} onChange={(e) => e.target.value && setRange(filters.from, e.target.value)} />
        </label>
      </div>
      {isOwner && (
        <div className="filter-group">
          <label>Tài khoản
            <select value={filters.userId ?? ''} onChange={(e) => onChange({ ...filters, userId: e.target.value ? Number(e.target.value) : null })}>
              <option value="">Tất cả</option>
              {(accounts ?? []).map((account) => (
                <option key={account.user_id} value={account.user_id}>{account.username}</option>
              ))}
            </select>
          </label>
        </div>
      )}
    </div>
  );
}
