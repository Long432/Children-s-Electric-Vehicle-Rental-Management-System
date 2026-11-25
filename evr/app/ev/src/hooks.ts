import { useCallback, useEffect, useRef, useState } from 'react';
import { AppState, Vibration } from 'react-native';
import { useFocusEffect } from '@react-navigation/native';
import { api } from './api';
import { ApiError } from './api/client';
import { useNow } from './clock';
import { VIBRATION_ENABLED, VIBRATION_PATTERNS } from './config';
import type { Ticket } from './types';
import { CountdownLevel, scanAlerts } from './utils';

/** Tải dữ liệu mỗi khi màn hình được mở lại; trả về data, lỗi, trạng thái tải và hàm tải lại. */
export function useApi<T>(fn: () => Promise<T>, deps: readonly unknown[] = []) {
  const [data, setData] = useState<T | null>(null);
  const [error, setError] = useState<ApiError | null>(null);
  const [loading, setLoading] = useState(true);
  const latest = useRef(fn);
  latest.current = fn;
  const requestId = useRef(0);
  const key = JSON.stringify(deps);
  const reload = useCallback(async () => {
    const mine = ++requestId.current;
    setLoading(true);
    try {
      const result = await latest.current();
      if (mine === requestId.current) { setData(result); setError(null); }
    } catch (e) {
      if (mine === requestId.current) { setData(null); setError(e as ApiError); }
    } finally {
      if (mine === requestId.current) setLoading(false);
    }
  }, []);
  useFocusEffect(useCallback(() => { reload(); }, [reload, key]));
  return { data, error, loading, reload };
}

export function useDebounced<T>(value: T, delayMs: number): T {
  const [debounced, setDebounced] = useState(value);
  useEffect(() => {
    const timer = setTimeout(() => setDebounced(value), delayMs);
    return () => clearTimeout(timer);
  }, [value, delayMs]);
  return debounced;
}

/** Chạy một thao tác (gọi API), tự bắt lỗi để hiển thị, rồi tải lại dữ liệu. */
export function useAction(onDone: () => void) {
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const run = async <R,>(fn: () => Promise<R>): Promise<R | undefined> => {
    setBusy(true); setError(null);
    try { const r = await fn(); onDone(); return r; }
    catch (e) { setError((e as Error).message); }
    finally { setBusy(false); }
  };
  return { run, busy, error };
}

export function useTicketAlerts(tickets: Ticket[] | null) {
  const now = useNow();
  const levels = useRef<Map<number, CountdownLevel>>(new Map());

  useEffect(() => {
    if (!tickets) return;
    const scan = scanAlerts(levels.current, tickets, now);
    levels.current = scan.levels;
    if (scan.strongest && VIBRATION_ENABLED) Vibration.vibrate(VIBRATION_PATTERNS[scan.strongest]);
  }, [tickets, now]);

  useEffect(() => () => Vibration.cancel(), []);
}

const WAITING_POLL_MS = 20000;
const waitingListeners = new Set<() => void>();

/** Báo cho huy hiệu trên tab Vé tải lại ngay, gọi sau khi tạo, gán, hủy hoặc trả vé. */
export const refreshWaitingCount = () => waitingListeners.forEach((load) => load());

/** Số vé đang chờ xe. Tự tải lại định kỳ, khi app mở lại và khi có thao tác về vé. Lỗi mạng thì giữ số cũ. */
export function useWaitingCount() {
  const [count, setCount] = useState(0);
  useEffect(() => {
    let alive = true;
    const load = async () => {
      try {
        const list = await api.tickets();
        if (alive) setCount(list.filter((t) => t.status === 'Waiting').length);
      } catch {
        // giữ nguyên số cũ
      }
    };
    load();
    const timer = setInterval(load, WAITING_POLL_MS);
    waitingListeners.add(load);
    const subscription = AppState.addEventListener('change', (state) => { if (state === 'active') load(); });
    return () => {
      alive = false;
      clearInterval(timer);
      waitingListeners.delete(load);
      subscription.remove();
    };
  }, []);
  return count;
}
