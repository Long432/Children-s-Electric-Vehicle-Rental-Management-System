import * as SecureStore from 'expo-secure-store';
import { API_URL, USE_MOCK } from '../config';
import { mockCall } from './mock';

export const COOKIE_NAME = 'SESSION_TOKEN';
let token: string | null = null;

export class ApiError extends Error {
  constructor(public code: string, message: string, public status = 0) { super(message); }
}

export async function loadToken() { token = await SecureStore.getItemAsync(COOKIE_NAME); return token; }
export async function setToken(t: string | null) {
  token = t;
  if (t) await SecureStore.setItemAsync(COOKIE_NAME, t); else await SecureStore.deleteItemAsync(COOKIE_NAME);
}

/** Gọi API theo quy ước { ok, data } / { ok:false, error }. Trả về data hoặc ném ApiError. */
export async function call<T>(method: string, path: string, body?: unknown): Promise<T> {
  if (USE_MOCK) return mockCall(method, path, body, token) as Promise<T>;
  let res: Response;
  try {
    res = await fetch(API_URL + path, {
      method,
      headers: { 'Content-Type': 'application/json', ...(token ? { Cookie: `${COOKIE_NAME}=${token}` } : {}) },
      body: body === undefined ? undefined : JSON.stringify(body),
    });
  } catch {
    throw new ApiError('NETWORK', 'Không kết nối được máy chủ. Kiểm tra mạng và địa chỉ API.');
  }
  const json = await res.json().catch(() => null);
  if (!json?.ok) throw new ApiError(json?.error?.code ?? 'UNKNOWN', json?.error?.message ?? 'Có lỗi xảy ra', res.status);
  return json.data as T;
}
