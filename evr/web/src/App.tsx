import { useEffect, useState } from 'react';
import type { ReactNode } from 'react';
import { FilterBar } from './components/FilterBar';
import { Badge, Notice } from './components/ui';
import { api, hasToken, onUnauthorized, setToken } from './lib/api';
import { addDays, todayKey } from './lib/format';
import { useAsync } from './lib/hooks';
import { go, useRoute } from './lib/router';
import type { Filters, User } from './lib/types';
import { AccountsPage } from './pages/AccountsPage';
import { LoginPage } from './pages/LoginPage';
import { OverviewPage } from './pages/OverviewPage';
import { SessionDetailPage } from './pages/SessionDetailPage';
import { SessionsPage } from './pages/SessionsPage';
import { VehiclesPage } from './pages/VehiclesPage';

const NAV = [
  { path: '/overview', label: 'Tổng quan' },
  { path: '/accounts', label: 'Tài khoản' },
  { path: '/sessions', label: 'Các phiên' },
  { path: '/vehicles', label: 'Xe hỏng' },
];

function defaultFilters(): Filters {
  const to = todayKey();
  return { from: addDays(to, -29), to, userId: null };
}

export default function App() {
  const [user, setUser] = useState<User | null>(null);
  const [booting, setBooting] = useState(hasToken());

  useEffect(() => {
    onUnauthorized(() => setUser(null));
    if (hasToken()) {
      api.me().then(setUser, () => setToken(null)).finally(() => setBooting(false));
    }
    return () => onUnauthorized(null);
  }, []);

  if (booting) return <div className="loading">Đang kiểm tra đăng nhập...</div>;
  if (!user) return <LoginPage onLoggedIn={setUser} />;
  return <Dashboard key={user.id} user={user} onLogout={() => { setToken(null); setUser(null); }} />;
}

function Dashboard({ user, onLogout }: { user: User; onLogout: () => void }) {
  const [filters, setFilters] = useState<Filters>(defaultFilters);
  const route = useRoute();
  const isOwner = user.role === 'Owner';
  const accounts = useAsync(() => api.accounts(filters.from, filters.to), [filters.from, filters.to]);

  const detail = /^\/sessions\/(\d+)$/.exec(route.path);
  const section = detail ? '/sessions' : route.path;
  const pickAccount = (userId: number) => {
    if (isOwner) setFilters((current) => ({ ...current, userId }));
    go('/sessions');
  };

  let page: ReactNode;
  if (detail) {
    page = <SessionDetailPage id={Number(detail[1])} />;
  } else if (route.path === '/accounts') {
    page = <AccountsPage filters={filters} accounts={accounts.data} loading={accounts.loading} error={accounts.error} onPick={pickAccount} />;
  } else if (route.path === '/sessions') {
    page = <SessionsPage key={`${filters.from}|${filters.to}|${filters.userId}`} filters={filters} />;
  } else if (route.path === '/vehicles') {
    page = <VehiclesPage key={`${filters.userId}`} filters={filters} />;
  } else if (route.path === '/overview') {
    page = <OverviewPage filters={filters} />;
  } else {
    page = <Notice kind="error">Không tìm thấy trang. <a className="link" href="#/overview">Về tổng quan</a></Notice>;
  }

  return (
    <div className="shell">
      <aside className="sidebar">
        <div className="brand">Thuê xe điện</div>
        <nav>
          {NAV.map((item) => (
            <a key={item.path} href={`#${item.path}`} className={section === item.path ? 'active' : undefined}>{item.label}</a>
          ))}
        </nav>
        <div className="sidebar-foot">
          <div><strong>{user.username}</strong> <Badge value={user.role} /></div>
          <button type="button" className="btn" onClick={onLogout}>Đăng xuất</button>
        </div>
      </aside>
      <main className="content">
        {!detail && <FilterBar filters={filters} onChange={setFilters} accounts={accounts.data} isOwner={isOwner} />}
        {!isOwner && !detail && <Notice kind="info">Bạn đang xem số liệu của các phiên do chính bạn phụ trách.</Notice>}
        {page}
      </main>
    </div>
  );
}
