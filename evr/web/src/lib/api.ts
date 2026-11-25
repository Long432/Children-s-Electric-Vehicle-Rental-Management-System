import type { AccountRow, Incident, LoginResult, PageOf, SessionDetail, SessionRow, Summary, User, VehicleRow } from './types';

const BASE_URL = (import.meta.env.VITE_API_URL as string | undefined) ?? 'http://localhost:8080';
const TOKEN_KEY = 'EV_REPORT_TOKEN';
const LOGIN_PATH = '/api/auth/login';
const REQUEST_TIMEOUT_MS = 20000;

export class ApiError extends Error {
  constructor(public code: string, message: string, public status = 0) {
    super(message);
  }
}

function readToken(): string | null {
  try {
    return window.localStorage.getItem(TOKEN_KEY);
  } catch {
    return null;
  }
}

let token: string | null = readToken();
let unauthorizedHandler: (() => void) | null = null;

export const hasToken = () => token !== null;

export function onUnauthorized(handler: (() => void) | null) {
  unauthorizedHandler = handler;
}

export function setToken(value: string | null) {
  token = value;
  try {
    if (value) window.localStorage.setItem(TOKEN_KEY, value);
    else window.localStorage.removeItem(TOKEN_KEY);
  } catch {
    return;
  }
}

type Query = Record<string, string | number | null | undefined>;

function queryString(params: Query) {
  const search = new URLSearchParams();
  for (const [key, value] of Object.entries(params)) {
    if (value !== null && value !== undefined && value !== '') search.set(key, String(value));
  }
  const text = search.toString();
  return text ? `?${text}` : '';
}

async function call<T>(method: string, path: string, body?: unknown): Promise<T> {
  const controller = new AbortController();
  const timer = window.setTimeout(() => controller.abort(), REQUEST_TIMEOUT_MS);
  let response: Response;
  try {
    response = await fetch(BASE_URL + path, {
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
    throw new ApiError('NETWORK', `Không kết nối được ${BASE_URL}. Kiểm tra backend đang chạy và địa chỉ.`);
  } finally {
    window.clearTimeout(timer);
  }
  const json: { ok?: unknown; data?: unknown; error?: { code?: string; message?: string } } | null = await response.json().catch(() => null);
  if (!json || typeof json.ok !== 'boolean') {
    throw new ApiError('UNKNOWN', `Lỗi ${response.status}`, response.status);
  }
  if (!json.ok) {
    if (response.status === 401 && path !== LOGIN_PATH) {
      setToken(null);
      unauthorizedHandler?.();
    }
    throw new ApiError(json.error?.code ?? 'UNKNOWN', json.error?.message ?? 'Có lỗi xảy ra', response.status);
  }
  return json.data as T;
}

export interface RangeQuery {
  from: string;
  to: string;
  userId: number | null;
}

const range = (q: RangeQuery) => ({ from: q.from, to: q.to, user_id: q.userId });

export const api = {
  login: (username: string, password: string) => call<LoginResult>('POST', LOGIN_PATH, { username, password }),
  me: () => call<User>('GET', '/api/auth/me'),
  summary: (q: RangeQuery) => call<Summary>('GET', `/api/reports/summary${queryString(range(q))}`),
  accounts: (from: string, to: string) => call<AccountRow[]>('GET', `/api/reports/accounts${queryString({ from, to })}`),
  sessions: (q: RangeQuery, page: number, limit: number) =>
    call<PageOf<SessionRow>>('GET', `/api/reports/sessions${queryString({ ...range(q), page, limit })}`),
  session: (id: number) => call<SessionDetail>('GET', `/api/reports/sessions/${id}`),
  incidents: (q: RangeQuery, vehicleId: number | null, limit: number) =>
    call<Incident[]>('GET', `/api/reports/incidents${queryString({ ...range(q), vehicle_id: vehicleId, limit })}`),
  vehicles: (q: RangeQuery) => call<VehicleRow[]>('GET', `/api/reports/vehicles${queryString(range(q))}`),
};
