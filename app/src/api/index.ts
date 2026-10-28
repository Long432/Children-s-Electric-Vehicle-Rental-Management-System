import { call } from './client';
import type { PaymentMethod, ReturnResult, Session, SessionStats, Settings, Ticket, Vehicle, VehicleStatus } from '../types';

export const api = {
  login: (username: string, password: string) => call<{ token?: string }>('POST', '/api/auth/login', { username, password }),
  me: () => call<{ username: string }>('GET', '/api/auth/me'),
  logout: () => call<unknown>('POST', '/api/settings/logout'),

  settings: () => call<Settings>('GET', '/api/settings'),
  toggleRefund: (enabled: boolean) => call<Settings>('POST', '/api/settings/toggle_refund', { enabled }),
  toggleExtend: (enabled: boolean) => call<Settings>('POST', '/api/settings/toggle_extend', { enabled }),
  setOverdueFee: (amount_per_minute: number) => call<Settings>('POST', '/api/settings/set_overdue_fee', { amount_per_minute }),
  setOverdueLimit: (minutes: number) => call<Settings>('POST', '/api/settings/set_overdue_limit', { minutes }),
  setMinimumTime: (minutes: number) => call<Settings>('POST', '/api/settings/set_minimum_time', { minutes }),

  currentSession: () => call<Session>('GET', '/api/sessions/current'),
  currentStats: () => call<SessionStats>('GET', '/api/sessions/current/stats'),
  startSession: (vehicle_slugs: string[]) => call<Session>('POST', '/api/sessions/start', { vehicle_slugs }),
  endSession: () => call<unknown>('POST', '/api/sessions/current/end'),

  vehicles: () => call<Vehicle[]>('GET', '/api/vehicles'),
  addVehicle: (slug: string) => call<Vehicle>('POST', '/api/vehicles', { slug }),
  setVehicleStatus: (id: number, status: VehicleStatus) => call<Vehicle>('PATCH', `/api/vehicles/${id}/status`, { status }),

  tickets: () => call<Ticket[]>('GET', '/api/tickets'),
  searchTickets: (q: string) => call<Ticket[]>('GET', `/api/tickets/search?q=${encodeURIComponent(q)}`),
  createTicket: (phone: string, minutes: number, payment_method: PaymentMethod) =>
    call<Ticket>('POST', '/api/tickets', { phone, minutes, payment_method }),
  assignTicket: (id: number, vehicle_slug: string) => call<Ticket>('POST', `/api/tickets/${id}/assign`, { vehicle_slug }),
  cancelTicket: (id: number) => call<{ refund_amount: number }>('POST', `/api/tickets/${id}/cancel`, {}),
  extendTicket: (id: number, minutes: number, payment_method: PaymentMethod) =>
    call<Ticket>('POST', `/api/tickets/${id}/extend`, { minutes, payment_method }),
  returnTicket: (id: number, payment_method?: PaymentMethod) => call<ReturnResult>('POST', `/api/tickets/${id}/return`, { payment_method }),
};
