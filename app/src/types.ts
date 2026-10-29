// Tên field theo api_list.txt / SettingsResponse. Các response khác là ĐỀ XUẤT, cần thống nhất với backend.
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
export interface Session { id: number; status: SessionStatus; end_time: string; vehicle_count: number }
export interface SessionStats { ticket_count: number; revenue_cash: number; revenue_bank: number; refunded: number; overdue_fees: number }
export interface Ticket {
  id: number; ticket_no: number; phone: string; status: TicketStatus; payment_method: PaymentMethod;
  total_minutes: number; amount_paid: number; due_at: string | null; vehicle_slug: string | null;
}
export interface ReturnResult {
  ticket_no: number; status: TicketStatus; overdue_minutes: number; overdue_fee: number; refund_amount: number;
  next_ticket: { ticket_no: number; phone: string } | null;
}
