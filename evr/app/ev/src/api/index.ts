import { ApiError, call } from './client';
import type {
  CreatedTickets, GroupCancelResult, GroupReturnResult, PaymentMethod, ReturnResult, Session, SessionRevenue, SessionStats, SessionStatus,
  Settings, Ticket, TicketStatus, Vehicle, VehicleStatus,
} from '../types';

export interface LoginResult {
  token: string;
  expires_at?: string;
  user?: { id: number; username: string };
}

interface WireTicket {
  id: number;
  ticket_no: number;
  phone: string;
  status: TicketStatus;
  payment_method: PaymentMethod;
  total_minutes: number;
  amount_paid: number;
  due_at: string | null;
  vehicle: { slug: string } | null;
  group_id?: number | null;
  group_size?: number | null;
}

interface WireCreatedTickets {
  ticket_no: number;
  amount_paid: number;
  ticket: WireTicket;
  quantity?: number;
  tickets?: WireTicket[];
}

interface WireSession {
  id: number;
  status: SessionStatus;
  is_instant: boolean;
  scheduled_date: string;
  scheduled_time: string;
  scheduled_at: string;
  planned_end_at: string | null;
  started_at: string | null;
  ended_at: string | null;
  vehicles?: Vehicle[];
}

interface WireStats {
  ticket_count: number;
  cash_revenue: number;
  bank_revenue: number;
  refund_total: number;
  overdue_fee: number;
}

interface WireRevenue {
  session_id: number;
  scheduled_date: string;
  ended_at: string | null;
  ticket_count: number;
  cash_revenue: number;
  bank_revenue: number;
  total_revenue: number;
  overdue_fee: number;
  refund_total: number;
}

interface Page<T> {
  items: T[];
}

const TICKET_PAGE_LIMIT = 100;
const SESSION_PAGE_LIMIT = 50;

const toTicket = (t: WireTicket): Ticket => ({
  id: t.id,
  ticket_no: t.ticket_no,
  phone: t.phone,
  status: t.status,
  payment_method: t.payment_method,
  total_minutes: t.total_minutes,
  amount_paid: t.amount_paid,
  due_at: t.due_at ?? null,
  vehicle_slug: t.vehicle?.slug ?? null,
  group_id: t.group_id ?? null,
  group_size: t.group_size ?? null,
});

const toSession = (s: WireSession): Session => ({
  id: s.id,
  status: s.status,
  is_instant: s.is_instant,
  scheduled_date: s.scheduled_date,
  scheduled_time: s.scheduled_time,
  scheduled_at: s.scheduled_at,
  planned_end_at: s.planned_end_at ?? null,
  started_at: s.started_at ?? null,
  ended_at: s.ended_at ?? null,
  vehicles: s.vehicles ?? [],
});

const byScheduledAt = (a: Session, b: Session) => Date.parse(a.scheduled_at) - Date.parse(b.scheduled_at);

const toStats = (s: WireStats): SessionStats => ({
  ticket_count: s.ticket_count,
  revenue_cash: s.cash_revenue,
  revenue_bank: s.bank_revenue,
  refunded: s.refund_total,
  overdue_fees: s.overdue_fee,
});

const toRevenue = (r: WireRevenue): SessionRevenue => ({
  id: r.session_id,
  scheduled_date: r.scheduled_date,
  ended_at: r.ended_at ?? null,
  ticket_count: r.ticket_count,
  revenue_cash: r.cash_revenue,
  revenue_bank: r.bank_revenue,
  revenue_total: r.total_revenue,
  refunded: r.refund_total,
  overdue_fees: r.overdue_fee,
});

const currentSession = async () => toSession(await call<WireSession>('GET', '/api/sessions/current'));

const activeSession = async (): Promise<Session | null> => {
  try {
    return await currentSession();
  } catch (e) {
    if (e instanceof ApiError && e.code === 'NO_ACTIVE_SESSION') return null;
    throw e;
  }
};

export const api = {
  login: (username: string, password: string) => call<LoginResult>('POST', '/api/auth/login', { username, password }),
  me: () => call<{ username: string }>('GET', '/api/auth/me'),
  logout: () => call<unknown>('POST', '/api/settings/logout'),

  settings: () => call<Settings>('GET', '/api/settings'),
  toggleRefund: (enabled: boolean) => call<Settings>('POST', '/api/settings/toggle_refund', { enabled }),
  toggleExtend: (enabled: boolean) => call<Settings>('POST', '/api/settings/toggle_extend', { enabled }),
  setOverdueFee: (amount_per_minute: number) => call<Settings>('POST', '/api/settings/set_overdue_fee', { amount_per_minute }),
  setOverdueLimit: (minutes: number) => call<Settings>('POST', '/api/settings/set_overdue_limit', { minutes }),
  setMinimumTime: (minutes: number) => call<Settings>('POST', '/api/settings/set_minimum_time', { minutes }),

  sessionRevenue: async (limit: number) =>
    (await call<WireRevenue[]>('GET', `/api/sessions/revenue?limit=${limit}`)).map(toRevenue).reverse(),
  currentSession,
  currentStats: async () => toStats(await call<WireStats>('GET', '/api/sessions/current/stats')),
  activeSession,
  startSession: async (vehicle_slugs: string[], session_id?: number, end_time?: string) =>
    toSession(await call<WireSession>('POST', '/api/sessions/start', { vehicle_slugs, session_id, end_time })),
  endSession: () => call<unknown>('POST', '/api/sessions/current/end'),
  waitingSessions: async () =>
    (await call<Page<WireSession>>('GET', `/api/sessions?status=Waiting&limit=${SESSION_PAGE_LIMIT}`)).items.map(toSession).sort(byScheduledAt),
  scheduleSession: async (date: string, time: string) => toSession(await call<WireSession>('POST', '/api/sessions', { date, time })),
  updateSession: async (id: number, date: string, time: string) => toSession(await call<WireSession>('PATCH', `/api/sessions/${id}`, { date, time })),
  cancelSession: async (id: number) => toSession(await call<WireSession>('POST', `/api/sessions/${id}/cancel`, {})),

  vehicles: () => call<Vehicle[]>('GET', '/api/vehicles'),
  addVehicle: (slug: string) => call<Vehicle>('POST', '/api/vehicles', { slug }),
  renameVehicle: (id: number, slug: string) => call<Vehicle>('PATCH', `/api/vehicles/${id}`, { slug }),
  setVehicleStatus: (id: number, status: VehicleStatus) => call<Vehicle>('PATCH', `/api/vehicles/${id}/status`, { status }),
  deleteVehicle: (id: number) => call<Vehicle>('DELETE', `/api/vehicles/${id}`),

  tickets: async () => (await call<Page<WireTicket>>('GET', `/api/tickets?limit=${TICKET_PAGE_LIMIT}`)).items.map(toTicket),
  searchTickets: async (q: string) => (await call<WireTicket[]>('GET', `/api/tickets/search?q=${encodeURIComponent(q)}`)).map(toTicket),
  createTicket: async (phone: string, minutes: number, payment_method: PaymentMethod, quantity = 1): Promise<CreatedTickets> => {
    const created = await call<WireCreatedTickets>('POST', '/api/tickets', { phone, minutes, payment_method, quantity });
    return {
      ticket_no: created.ticket_no,
      quantity: created.quantity ?? 1,
      amount_paid: created.amount_paid,
      tickets: (created.tickets ?? [created.ticket]).map(toTicket),
    };
  },
  assignTicket: async (id: number, vehicle_slug: string) =>
    toTicket(await call<WireTicket>('POST', `/api/tickets/${id}/assign`, { vehicle_slug })),
  cancelTicket: (id: number) => call<{ refund_amount: number }>('POST', `/api/tickets/${id}/cancel`, {}),
  extendTicket: async (id: number, minutes: number, payment_method: PaymentMethod) =>
    toTicket((await call<{ ticket: WireTicket }>('POST', `/api/tickets/${id}/extend`, { minutes, payment_method })).ticket),
  returnTicket: (id: number, payment_method?: PaymentMethod) => call<ReturnResult>('POST', `/api/tickets/${id}/return`, { payment_method }),
  assignGroup: async (group_id: number, vehicle_slugs: string[]) =>
    (await call<{ tickets: WireTicket[] }>('POST', `/api/tickets/groups/${group_id}/assign`, { vehicle_slugs })).tickets.map(toTicket),
  cancelGroup: (group_id: number) => call<GroupCancelResult>('POST', `/api/tickets/groups/${group_id}/cancel`, {}),
  returnGroup: (group_id: number, payment_method?: PaymentMethod) =>
    call<GroupReturnResult>('POST', `/api/tickets/groups/${group_id}/return`, { payment_method }),
};
