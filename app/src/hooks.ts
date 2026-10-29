import { useCallback, useState } from 'react';
import { useFocusEffect } from '@react-navigation/native';
import { ApiError } from './api/client';

/** Tải dữ liệu mỗi khi màn hình được mở lại; trả về data, lỗi, trạng thái tải và hàm tải lại. */
export function useApi<T>(fn: () => Promise<T>) {
  const [data, setData] = useState<T | null>(null);
  const [error, setError] = useState<ApiError | null>(null);
  const [loading, setLoading] = useState(true);
  const reload = useCallback(async () => {
    setLoading(true);
    try { setData(await fn()); setError(null); }
    catch (e) { setData(null); setError(e as ApiError); }
    finally { setLoading(false); }
  }, []); // eslint-disable-line react-hooks/exhaustive-deps
  useFocusEffect(useCallback(() => { reload(); }, [reload]));
  return { data, error, loading, reload };
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
