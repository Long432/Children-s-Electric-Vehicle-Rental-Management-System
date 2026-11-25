import * as SecureStore from 'expo-secure-store';
import { BACKEND_URL } from './backend';
import { syncServerTime } from '../time';

const TOKEN_KEY = 'SESSION_TOKEN';
const LOGIN_PATH = '/api/auth/login';
const REQUEST_TIMEOUT_MS = 15000;

let token: string | null = null;
let unauthorizedHandler: (() => void) | null = null;

export class ApiError extends Error {
  constructor(public code: string, message: string, public status = 0) {
    super(message);
  }
}

export function onUnauthorized(handler: (() => void) | null) {
  unauthorizedHandler = handler;
}

export async function loadToken() {
  token = await SecureStore.getItemAsync(TOKEN_KEY);
  return token;
}

export async function setToken(value: string | null) {
  token = value;
  if (value) await SecureStore.setItemAsync(TOKEN_KEY, value);
  else await SecureStore.deleteItemAsync(TOKEN_KEY);
}

export async function call<T>(method: string, path: string, body?: unknown): Promise<T> {
  const controller = new AbortController();
  const timer = setTimeout(() => controller.abort(), REQUEST_TIMEOUT_MS);

  let response: Response;
  try {
    response = await fetch(BACKEND_URL + path, {
      method,
      signal: controller.signal,
      headers: {
        Accept: 'application/json',
        'Content-Type': 'application/json',
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
      },
      body: body === undefined ? undefined : JSON.stringify(body),
    });
  } catch {
    throw new ApiError('NETWORK', `Không kết nối được ${BACKEND_URL}. Kiểm tra backend đang chạy và địa chỉ.`);
  } finally {
    clearTimeout(timer);
  }

  syncServerTime(response.headers.get('date'));
  const json = await response.json().catch(() => null);
  if (!json || typeof json.ok !== 'boolean') {
    throw new ApiError('UNKNOWN', `Lỗi ${response.status}`, response.status);
  }
  if (!json.ok) {
    if (response.status === 401 && path !== LOGIN_PATH) {
      await setToken(null);
      unauthorizedHandler?.();
    }
    throw new ApiError(json.error?.code ?? 'UNKNOWN', json.error?.message ?? 'Có lỗi xảy ra', response.status);
  }
  return json.data as T;
}
