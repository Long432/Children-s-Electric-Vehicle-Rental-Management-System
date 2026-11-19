import { useState } from 'react';
import { api, setToken } from '../lib/api';
import { messageOf } from '../lib/hooks';
import type { User } from '../lib/types';

export function LoginPage({ onLoggedIn }: { onLoggedIn: (user: User) => void }) {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  const submit = async (event: { preventDefault: () => void }) => {
    event.preventDefault();
    setBusy(true);
    setError(null);
    try {
      const result = await api.login(username.trim(), password);
      setToken(result.token);
      onLoggedIn(result.user);
    } catch (e) {
      setError(messageOf(e));
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="login">
      <form className="login-card" onSubmit={submit}>
        <h1>Báo cáo thuê xe điện</h1>
        <p className="muted">Đăng nhập bằng tài khoản đang dùng trên ứng dụng.</p>
        <label>Tên đăng nhập
          <input value={username} onChange={(e) => setUsername(e.target.value)} autoComplete="username" autoFocus />
        </label>
        <label>Mật khẩu
          <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} autoComplete="current-password" />
        </label>
        {error && <div className="notice notice-error">{error}</div>}
        <button type="submit" className="btn btn-primary" disabled={busy || !username.trim() || !password}>
          {busy ? 'Đang đăng nhập...' : 'Đăng nhập'}
        </button>
      </form>
    </div>
  );
}
