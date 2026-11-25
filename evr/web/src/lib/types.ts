export type Role = 'Owner' | 'Staff';

export interface User {
  id: number;
  username: string;
  status: string;
  role: Role;
}

export interface LoginResult {
  token: string;
  expires_at?: string;
  user: User;
}

export interface UserRef {
  id: number;
  username: string;
}

export interface DailyRevenue {
  date: string;
  session_count: number;
  ticket_count: number;
  cash_revenue: number;
  bank_revenue: number;
  total_revenue: number;
}

export interface Summary {
  from: string;
  to: string;
  session_count: number;
  work_minutes: number;
  ticket_count: number;
  canceled_ticket_count: number;
  cash_revenue: number;
  bank_revenue: number;
  total_revenue: number;
  overdue_fee: number;
  refund_total: number;
  incident_count: number;
  revenue_per_hour: number;
  daily: DailyRevenue[];
}

export interface AccountRow {
  user_id: number;
  username: string;
  role: Role;
  status: string;
  session_count: number;
  work_minutes: number;
  ticket_count: number;
  canceled_ticket_count: number;
  cash_revenue: number;
  bank_revenue: number;
  total_revenue: number;
  overdue_fee: number;
  refund_total: number;
  incident_count: number;
  revenue_per_hour: number;
}

export interface SessionRow {
  session_id: number;
  scheduled_date: string;
  status: string;
  started_at: string | null;
  ended_at: string | null;
  work_minutes: number;
  in_charge: UserRef;
  created_by: UserRef;
  ended_by: UserRef | null;
  ticket_count: number;
  canceled_ticket_count: number;
  cash_revenue: number;
  bank_revenue: number;
  total_revenue: number;
  overdue_fee: number;
  refund_total: number;
  incident_count: number;
}

export interface PageOf<T> {
  items: T[];
  page: number;
  limit: number;
  total: number;
}

export interface Participant {
  user_id: number;
  username: string;
  tickets_created: number;
  tickets_returned: number;
  tickets_canceled: number;
  cash_revenue: number;
  bank_revenue: number;
  total_revenue: number;
}

export interface Incident {
  id: number;
  created_at: string;
  vehicle_id: number;
  vehicle_slug: string;
  session_id: number | null;
  ticket_id: number | null;
  ticket_no: number | null;
  source: 'Ticket' | 'Manual';
  reported_by: UserRef;
  session_in_charge: UserRef | null;
}

export interface TimelineEvent {
  at: string;
  user: UserRef;
  action: string;
  entity_type: string;
  entity_id: number | null;
  payload: string | null;
}

export interface SessionDetail {
  session: SessionRow;
  participants: Participant[];
  incidents: Incident[];
  timeline: TimelineEvent[];
}

export interface VehicleRow {
  vehicle_id: number;
  slug: string;
  status: string;
  incident_count: number;
  last_incident_at: string | null;
}

export interface Filters {
  from: string;
  to: string;
  userId: number | null;
}
