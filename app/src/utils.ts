import { color } from './theme';
import type { TicketStatus, VehicleStatus } from './types';

export const money = (n: number) => String(Math.round(n)).replace(/\B(?=(\d{3})+(?!\d))/g, '.') + 'đ';

export const minutesLeft = (due: string | null) => (due ? Math.round((Date.parse(due) - Date.now()) / 60000) : null);

export const vehicleLabel: Record<VehicleStatus, [string, string]> = {
  Online: ['Sẵn sàng', color.go], Inused: ['Đang chạy', color.action], Repairing: ['Sửa chữa', color.wait],
  Offline: ['Nghỉ', color.idle], Deleted: ['Đã xóa', color.idle],
};
export const ticketLabel: Record<TicketStatus, [string, string]> = {
  Waiting: ['Chờ xe', color.wait], Inused: ['Đang chạy', color.go], Overdue: ['Quá giờ', color.stop],
  Completed: ['Hoàn thành', color.idle], Canceled: ['Đã hủy', color.idle],
};
