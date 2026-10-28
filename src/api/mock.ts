// Backend giả để dựng giao diện khi API thật chưa xong. Dữ liệu nằm trong bộ nhớ, tắt app là mất.
import { ApiError } from './client';
import type { Settings, Session, Ticket, Vehicle } from '../types';

const ago = (m: number) => new Date(Date.now() - m * 60000).toISOString();
const inMin = (m: number) => new Date(Date.now() + m * 60000).toISOString();

const db = {
  settings: {
    refund_enabled: true, extend_enabled: true, overdue_fee_per_minute: 2000, overdue_grace_minutes: 5, minimum_minutes: 15,
    packages: [{ amount: 15000, minutes: 30, is_default: true }, { amount: 30000, minutes: 60, is_default: false }],
    updated_at: new Date().toISOString(),
  } as Settings,
  session: { id: 1, status: 'InProgress', end_time: inMin(300), vehicle_count: 5 } as Session | null,
  vehicles: ['xe-gau-01', 'xe-gau-02', 'xe-gau-03', 'xe-canh-sat-04', 'xe-lua-05'].map((slug, i): Vehicle => ({
    id: i + 1, slug, status: i < 2 ? 'Inused' : i === 4 ? 'Repairing' : 'Online',
  })),
  tickets: [
    { id: 1, ticket_no: 1, phone: '0901234567', status: 'Inused', payment_method: 'Cash', total_minutes: 30, amount_paid: 15000, due_at: inMin(12), vehicle_slug: 'xe-gau-01' },
    { id: 2, ticket_no: 2, phone: '0912345678', status: 'Overdue', payment_method: 'Bank', total_minutes: 30, amount_paid: 15000, due_at: ago(9), vehicle_slug: 'xe-gau-02' },
    { id: 3, ticket_no: 3, phone: '0987654321', status: 'Waiting', payment_method: 'Cash', total_minutes: 60, amount_paid: 30000, due_at: null, vehicle_slug: null },
  ] as Ticket[],
};

const wait = (ms: number) => new Promise((r) => setTimeout(r, ms));
const fail = (code: string, msg: string, status = 409): never => { throw new ApiError(code, msg, status); };

export async function mockCall(method: string, path: string, body: any, token: string | null): Promise<unknown> {
  await wait(150);
  const [p, qs] = path.split('?');
  const s = db.settings;
  const touch = () => { s.updated_at = new Date().toISOString(); return { ...s }; };

  if (p === '/api/auth/login') return { token: 'dev-token' };
  if (!token) fail('UNAUTHORIZED', 'Bạn cần đăng nhập lại', 401);
  if (p === '/api/auth/me') return { username: 'dev' };
  if (p === '/api/settings/logout') return {};

  if (p === '/api/settings' || p === '/api/settings/') return { ...s };
  if (p.endsWith('/toggle_refund')) { s.refund_enabled = body.enabled; return touch(); }
  if (p.endsWith('/toggle_extend')) { s.extend_enabled = body.enabled; return touch(); }
  if (p.endsWith('/set_overdue_fee')) { s.overdue_fee_per_minute = body.amount_per_minute; return touch(); }
  if (p.endsWith('/set_overdue_limit')) { s.overdue_grace_minutes = body.minutes; return touch(); }
  if (p.endsWith('/set_minimum_time')) { s.minimum_minutes = body.minutes; return touch(); }

  if (p === '/api/sessions/current') return db.session ?? fail('NO_ACTIVE_SESSION', 'Chưa có phiên đang làm');
  if (p === '/api/sessions/current/stats') {
    const sum = (m: string) => db.tickets.filter((t) => t.payment_method === m && t.status !== 'Canceled').reduce((a, t) => a + t.amount_paid, 0);
    return { ticket_count: db.tickets.length, revenue_cash: sum('Cash'), revenue_bank: sum('Bank'), refunded: 0, overdue_fees: 0 };
  }
  if (p === '/api/sessions/start') {
    if (db.session) fail('SESSION_ALREADY_IN_PROGRESS', 'Đã có phiên đang làm');
    db.session = { id: 2, status: 'InProgress', end_time: inMin(600), vehicle_count: body.vehicle_slugs.length };
    return db.session;
  }
  if (p === '/api/sessions/current/end') {
    if (db.tickets.some((t) => ['Waiting', 'Inused', 'Overdue'].includes(t.status))) fail('SESSION_HAS_OPEN_TICKETS', 'Còn vé chưa xử lý xong');
    db.session = null; return {};
  }

  if (p === '/api/vehicles' && method === 'GET') return db.vehicles.filter((v) => v.status !== 'Deleted');
  if (p === '/api/vehicles' && method === 'POST') {
    if (db.vehicles.some((v) => v.slug === body.slug)) fail('SLUG_TAKEN', 'Tên nhận diện đã tồn tại', 422);
    const v: Vehicle = { id: db.vehicles.length + 1, slug: body.slug, status: 'Offline' }; db.vehicles.push(v); return v;
  }
  let m = p.match(/^\/api\/vehicles\/(\d+)\/status$/);
  if (m) {
    const v = db.vehicles.find((x) => x.id === +m![1])!;
    if (v.status === 'Inused') fail('VEHICLE_IN_USE', 'Xe đang được dùng, không đổi trạng thái được');
    v.status = body.status; return v;
  }

  if (p === '/api/tickets/search') {
    const q = new URLSearchParams(qs).get('q') ?? '';
    return db.tickets.filter((t) => t.phone.includes(q) || String(t.ticket_no) === q);
  }
  if (p === '/api/tickets' && method === 'GET') return [...db.tickets].reverse();
  if (p === '/api/tickets' && method === 'POST') {
    if (!db.session) fail('NO_ACTIVE_SESSION', 'Chưa có phiên đang làm');
    if (body.minutes < s.minimum_minutes) fail('BELOW_MINIMUM_TIME', `Tối thiểu ${s.minimum_minutes} phút`, 422);
    const d = s.packages.find((x) => x.is_default) ?? s.packages[0];
    const t: Ticket = { id: db.tickets.length + 1, ticket_no: db.tickets.length + 1, phone: body.phone, status: 'Waiting',
      payment_method: body.payment_method, total_minutes: body.minutes, amount_paid: Math.round((d.amount / d.minutes) * body.minutes), due_at: null, vehicle_slug: null };
    db.tickets.push(t); return t;
  }
  m = p.match(/^\/api\/tickets\/(\d+)\/(assign|return|cancel|extend)$/);
  if (m) {
    const t = db.tickets.find((x) => x.id === +m![1])!;
    if (m[2] === 'assign') {
      const v = db.vehicles.find((x) => x.slug === body.vehicle_slug);
      if (!v || v.status !== 'Online') fail('VEHICLE_NOT_AVAILABLE', 'Xe này chưa sẵn sàng');
      v!.status = 'Inused'; t.status = 'Inused'; t.vehicle_slug = v!.slug; t.due_at = inMin(t.total_minutes); return t;
    }
    if (m[2] === 'cancel') {
      if (t.status !== 'Waiting') fail('TICKET_NOT_CANCELABLE', 'Chỉ hủy được vé đang chờ xe');
      t.status = 'Canceled'; return { refund_amount: t.amount_paid };
    }
    if (m[2] === 'extend') {
      if (!s.extend_enabled) fail('EXTEND_DISABLED', 'Gia hạn đang tắt trong cài đặt');
      t.total_minutes += body.minutes; t.due_at = new Date(Date.parse(t.due_at!) + body.minutes * 60000).toISOString(); return t;
    }
    const left = Math.round((Date.parse(t.due_at!) - Date.now()) / 60000);
    const over = Math.max(0, -left), chargeable = Math.max(0, over - s.overdue_grace_minutes);
    const veh = db.vehicles.find((x) => x.slug === t.vehicle_slug); if (veh) veh.status = 'Online';
    t.status = 'Completed';
    const next = db.tickets.find((x) => x.status === 'Waiting');
    return { ticket_no: t.ticket_no, status: 'Completed', overdue_minutes: over, overdue_fee: chargeable * s.overdue_fee_per_minute,
      refund_amount: s.refund_enabled && left > 0 ? Math.round((t.amount_paid / t.total_minutes) * left) : 0,
      next_ticket: next ? { ticket_no: next.ticket_no, phone: next.phone } : null };
  }
  return fail('NOT_MOCKED', `Chưa có mock cho ${method} ${p}`, 404);
}
