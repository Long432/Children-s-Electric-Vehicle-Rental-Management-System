import React, { createContext, useContext, useEffect, useState } from 'react';
import { api } from './api';
import { loadToken, setToken } from './api/client';

type State = 'loading' | 'out' | 'in';
const Ctx = createContext<{ state: State; login: (u: string, p: string) => Promise<void>; logout: () => Promise<void> }>(null as never);
export const useAuth = () => useContext(Ctx);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [state, setState] = useState<State>('loading');
  useEffect(() => {
    (async () => {
      if (!(await loadToken())) return setState('out');
      try { await api.me(); setState('in'); } catch { setState('out'); }
    })();
  }, []);
  const login = async (u: string, p: string) => {
    const r = await api.login(u, p);
    // Backend thật chưa có API login: cần trả token trong body (data.token) thì app mới lưu và gửi lại qua header Cookie.
    if (r.token) await setToken(r.token);
    setState('in');
  };
  const logout = async () => { try { await api.logout(); } catch {} await setToken(null); setState('out'); };
  return <Ctx.Provider value={{ state, login, logout }}>{children}</Ctx.Provider>;
}
