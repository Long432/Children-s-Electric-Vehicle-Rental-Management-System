import * as Notifications from 'expo-notifications';
import { Platform } from 'react-native';
import { NOTIFICATIONS_ENABLED, REMINDER_LEAD_MINUTES, TICKET_CHANNEL_ID } from './config';
import { serverNow, serverToLocal } from './time';
import type { Ticket } from './types';

type TimedTicket = Pick<Ticket, 'id' | 'ticket_no' | 'phone' | 'due_at' | 'vehicle_slug'>;

const MINUTE_MS = 60000;
const soonId = (ticketId: number) => `ticket-${ticketId}-soon`;
const overdueId = (ticketId: number) => `ticket-${ticketId}-overdue`;

const triggerAt = (serverMs: number) => ({
  type: Notifications.SchedulableTriggerInputTypes.DATE,
  date: serverToLocal(serverMs),
  channelId: TICKET_CHANNEL_ID,
}) as const;

async function ensurePermission() {
  const current = await Notifications.getPermissionsAsync();
  if (current.granted) return true;
  if (!current.canAskAgain) return false;
  return (await Notifications.requestPermissionsAsync()).granted;
}

async function cancelOne(identifier: string) {
  try {
    await Notifications.cancelScheduledNotificationAsync(identifier);
  } catch {}
}

export async function initNotifications() {
  if (!NOTIFICATIONS_ENABLED) return;
  Notifications.setNotificationHandler({
    handleNotification: async () => ({
      shouldPlaySound: true,
      shouldSetBadge: false,
      shouldShowBanner: true,
      shouldShowList: true,
    }),
  });
  if (Platform.OS === 'android') {
    await Notifications.setNotificationChannelAsync(TICKET_CHANNEL_ID, {
      name: 'Nhắc giờ trả xe',
      importance: Notifications.AndroidImportance.HIGH,
      vibrationPattern: [0, 300, 200, 300],
      sound: 'default',
    });
  }
}

export async function cancelTicketReminders(ticketId: number) {
  await Promise.all([cancelOne(soonId(ticketId)), cancelOne(overdueId(ticketId))]);
}

export async function cancelAllReminders() {
  try {
    await Notifications.cancelAllScheduledNotificationsAsync();
  } catch {}
}

export async function scheduleTicketReminders(ticket: TimedTicket) {
  if (!NOTIFICATIONS_ENABLED || !ticket.due_at) return false;
  await cancelTicketReminders(ticket.id);
  const dueAt = Date.parse(ticket.due_at);
  const now = serverNow();
  if (Number.isNaN(dueAt) || dueAt <= now) return false;
  try {
    if (!(await ensurePermission())) return false;
    const detail = ticket.vehicle_slug ? `${ticket.vehicle_slug} · ${ticket.phone}` : ticket.phone;
    const soonAt = dueAt - REMINDER_LEAD_MINUTES * MINUTE_MS;
    if (soonAt > now) {
      await Notifications.scheduleNotificationAsync({
        identifier: soonId(ticket.id),
        content: {
          title: `Vé #${ticket.ticket_no} sắp hết giờ`,
          body: `Còn khoảng ${REMINDER_LEAD_MINUTES} phút · ${detail}`,
          data: { ticket_id: ticket.id },
        },
        trigger: triggerAt(soonAt),
      });
    }
    await Notifications.scheduleNotificationAsync({
      identifier: overdueId(ticket.id),
      content: {
        title: `Vé #${ticket.ticket_no} đã quá giờ`,
        body: `Cần thu xe về · ${detail}`,
        data: { ticket_id: ticket.id },
      },
      trigger: triggerAt(dueAt),
    });
    return true;
  } catch {
    return false;
  }
}
