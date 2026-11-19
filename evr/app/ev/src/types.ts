export type VehicleStatus = 'Online' | 'Repairing' | 'Offline' | 'Inused' | 'Deleted';
export type SessionStatus = 'Waiting' | 'InProgress' | 'Ended' | 'Canceled';
export type TicketStatus = 'Waiting' | 'Inused' | 'Overdue' | 'Canceled' | 'Completed';
export type PaymentMethod = 'Cash' | 'Bank';

export interface Vehicle { id: number; slug: string; status: VehicleStatus }
export interface PricePackage { amount: number; minutes: number; is_default: boolean }
export interface Settings {
  refund_enabled: boolean; extend_enabled: boolean;
  overdue_fee_per_minute: number; overdue_grace_minutes: number; minimum_minutes: number;
  packages: PricePackage[]; updated_at: string;
}
export interface Session {
  id: number; status: SessionStatus; is_instant: boolean;
  scheduled_date: string; scheduled_time: string; scheduled_at: string;
  planned_end_at: string | null; started_at: string | null; ended_at: string | null;
  vehicles: Vehicle[];
}
export interface SessionStats { ticket_count: number; revenue_cash: number; revenue_bank: number; refunded: number; overdue_fees: number }
export interface SessionRevenue {
  id: number; scheduled_date: string; ended_at: string | null; ticket_count: number;
  revenue_cash: number; revenue_bank: number; revenue_total: number; refunded: number; overdue_fees: number;
}
export interface Ticket {
  id: number; ticket_no: number; phone: string; status: TicketStatus; payment_method: PaymentMethod;
  total_minutes: number; amount_paid: number; due_at: string | null; vehicle_slug: string | null;
}
export interface ReturnResult {
  ticket_no: number; status: TicketStatus; overdue_minutes: number; overdue_fee: number; refund_amount: number;
  next_ticket: { ticket_no: number; phone: string } | null;
}
