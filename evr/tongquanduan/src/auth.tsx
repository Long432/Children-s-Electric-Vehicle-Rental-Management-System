import React, { createContext, useContext, useEffect, useState } from 'react';
import { api } from './api';
import { loadToken, onUnauthorized, setToken } from './api/client';
import { cancelAllReminders } from './notifications';

type State = 'loading' | 'out' | 'in';

const AuthContext = createContext<{
  state: State;
  login: (username: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
}>(null as never);

export const useAuth = () => useContext(AuthContext);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [state, setState] = useState<State>('loading');

  useEffect(() => {
    onUnauthorized(() => setState('out'));
    (async () => {
      if (!(await loadToken())) {
        setState('out');
        return;
      }
      try {
        await api.me();
        setState('in');
      } catch {
        setState('out');
      }
    })();
    return () => onUnauthorized(null);
  }, []);

  const login = async (username: string, password: string) => {
    const result = await api.login(username, password);
    await setToken(result.token);
    setState('in');
  };

  const logout = async () => {
    try {
      await api.logout();
    } catch {}
    await cancelAllReminders();
    await setToken(null);
    setState('out');
  };

  return <AuthContext.Provider value={{ state, login, logout }}>{children}</AuthContext.Provider>;
}
