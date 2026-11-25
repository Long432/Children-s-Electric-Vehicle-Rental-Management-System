import React, { useRef, useState } from 'react';
import { LayoutChangeEvent, ScrollView, View } from 'react-native';
import { api } from '../api';
import { Btn, Card, Chips, Countdown, ErrorBox, Field, H, Light, PastEndNotice, Screen, SearchField, Sub, Txt } from '../components/ui';
import { useNow } from '../clock';
import { refreshWaitingCount, useAction, useApi, useDebounced, useTicketAlerts } from '../hooks';
import { MAX_GROUP_SIZE } from '../config';
import { cancelTicketReminders, scheduleTicketReminders } from '../notifications';
import { color, fonts } from '../theme';
import type { PaymentMethod, Ticket } from '../types';
import { effectiveStatus, isPastPlannedEnd, isPhoneValid, isRunning, money, normalizePhone, phoneError, ticketLabel } from '../utils';

const SEARCH_DELAY_MS = 300;

export default function TicketsScreen() {
  const [q, setQ] = useState('');
  const keyword = useDebounced(normalizePhone(q), SEARCH_DELAY_MS);
  const list = useApi(() => (keyword ? api.searchTickets(keyword) : api.tickets()), [keyword]);
  useTicketAlerts(list.data);
  const settings = useApi(api.settings);
  const session = useApi(api.activeSession);
  const now = useNow();
  const reload = () => { list.reload(); session.reload(); refreshWaitingCount(); };
  const act = useAction(reload);
  const [phone, setPhone] = useState(''); const [minutes, setMinutes] = useState(30); const [pay, setPay] = useState<PaymentMethod>('Cash');
  const [quantity, setQuantity] = useState(1);
  const [notice, setNotice] = useState('');
  const scrollRef = useRef<ScrollView>(null);
  const pendingIds = useRef<number[]>([]);
  const positions = useRef<Record<number, number>>({});
  const free = session.data?.vehicles.find((v) => v.status === 'Online');
  const freeSlugs = (session.data?.vehicles ?? []).filter((v) => v.status === 'Online').map((v) => v.slug);
  const selectedPackage = settings.data?.packages.find((p) => p.minutes === minutes);
  const pastEnd = isPastPlannedEnd(session.data, now);

  const onTicketLayout = (id: number) => (e: LayoutChangeEvent) => {
    if (!pendingIds.current.includes(id)) return;
    positions.current[id] = e.nativeEvent.layout.y;
    if (!pendingIds.current.every((p) => positions.current[p] !== undefined)) return;
    const top = Math.min(...pendingIds.current.map((p) => positions.current[p]));
    pendingIds.current = [];
    positions.current = {};
    scrollRef.current?.scrollTo({ y: Math.max(top - 12, 0), animated: true });
  };

  const create = async () => {
    const created = await act.run(() => api.createTicket(normalizePhone(phone), minutes, pay, quantity));
    if (created) {
      const last = created.tickets[created.tickets.length - 1];
      const numbers = created.quantity > 1 ? `${created.quantity} vé #${created.ticket_no} đến #${last.ticket_no}` : `vé #${created.ticket_no}`;
      setNotice(`Đã tạo ${numbers}. Thu ${money(created.amount_paid)}.`);
      setPhone('');
      setQuantity(1);
      setQ('');
      positions.current = {};
      pendingIds.current = created.tickets.map((t) => t.id);
    }
  };
  const assign = async (t: Ticket, slug: string) => {
    const updated = await act.run(() => api.assignTicket(t.id, slug));
    if (updated) await scheduleTicketReminders(updated);
  };
  const extend = async (t: Ticket) => {
    const updated = await act.run(() => api.extendTicket(t.id, 15, 'Cash'));
    if (updated) await scheduleTicketReminders(updated);
  };
  const giveBack = async (t: Ticket) => {
    const r = await act.run(() => api.returnTicket(t.id, 'Cash'));
    if (r) await cancelTicketReminders(t.id);
    if (r) setNotice([`Đã trả vé #${r.ticket_no}.`, r.overdue_fee ? `Thu phụ phí ${money(r.overdue_fee)}.` : '', r.refund_amount ? `Hoàn ${money(r.refund_amount)}.` : '', r.next_ticket ? `Gán xe cho vé #${r.next_ticket.ticket_no} (${r.next_ticket.phone}).` : ''].filter(Boolean).join(' '));
  };

  const assignGroup = async (groupId: number, slugs: string[]) => {
    const updated = await act.run(() => api.assignGroup(groupId, slugs));
    if (updated) {
      for (const t of updated) await scheduleTicketReminders(t);
    }
  };
  const cancelGroup = async (groupId: number) => {
    const r = await act.run(() => api.cancelGroup(groupId));
    if (r) setNotice(`Đã hủy ${r.canceled_count} vé của nhóm. Hoàn lại ${money(r.refund_amount)}.`);
  };
  const giveBackGroup = async (groupId: number, members: Ticket[]) => {
    const r = await act.run(() => api.returnGroup(groupId, 'Cash'));
    if (r) {
      for (const t of members) await cancelTicketReminders(t.id);
      setNotice([`Đã trả ${r.returned_count} vé của nhóm.`, r.overdue_fee ? `Thu phụ phí ${money(r.overdue_fee)}.` : '', r.refund_amount ? `Hoàn ${money(r.refund_amount)}.` : '', r.next_ticket ? `Gán xe cho vé #${r.next_ticket.ticket_no} (${r.next_ticket.phone}).` : ''].filter(Boolean).join(' '));
    }
  };

  return (
    <Screen title="Vé" onRefresh={reload} refreshing={list.loading} scrollRef={scrollRef}>
      {act.error && <ErrorBox message={act.error} />}
      {list.error && <ErrorBox message={list.error.message} />}
      {!!notice && <Card style={{ borderColor: color.go }}><Txt style={{ color: color.ink, fontFamily: fonts.bodySemi }}>{notice}</Txt></Card>}

      <PastEndNotice session={session.data} />

      <Card>
        <H>Tạo vé mới</H>
        <Field label="Số điện thoại" value={phone} onChangeText={setPhone} keyboardType="phone-pad" placeholder="09xxxxxxxx" error={phoneError(phone)} />
        <Chips value={minutes} onChange={setMinutes} options={(settings.data?.packages ?? []).map((p) => ({ value: p.minutes, label: `${p.minutes} phút · ${money(p.amount)}` }))} />
        <Sub>Số xe cho khách này</Sub>
        <Chips value={quantity} onChange={setQuantity} options={Array.from({ length: MAX_GROUP_SIZE }, (_, i) => ({ value: i + 1, label: `${i + 1} xe` }))} />
        {quantity > 1 && !!selectedPackage && <Sub>Thu tổng {money(selectedPackage.amount * quantity)} cho {quantity} xe.</Sub>}
        <Chips value={pay} onChange={setPay} options={[{ value: 'Cash', label: 'Tiền mặt' }, { value: 'Bank', label: 'Chuyển khoản' }]} />
        <Btn label={quantity > 1 ? `Tạo ${quantity} vé và thu tiền` : 'Tạo vé và thu tiền'} onPress={create} busy={act.busy} disabled={!isPhoneValid(phone) || pastEnd} />
        {pastEnd && <Sub>Đã quá giờ kết thúc dự kiến nên không tạo được vé mới.</Sub>}
      </Card>

      <SearchField label="Tìm vé" value={q} onChangeText={setQ} onClear={() => setQ('')} onSubmitEditing={list.reload} returnKeyType="search" placeholder="Số điện thoại hoặc mã vé" />

      {!list.error && !list.loading && list.data?.length === 0 && <Sub>{keyword ? 'Không tìm thấy vé' : 'Không có vé nào phù hợp.'}</Sub>}
      {list.data?.map((t) => {
        const status = effectiveStatus(t, now);
        const [label, tint] = ticketLabel[status];
        const mates = !keyword && t.group_id ? (list.data ?? []).filter((x) => x.group_id === t.group_id) : [];
        const waitingMates = mates.filter((x) => x.status === 'Waiting');
        const runningMates = mates.filter((x) => isRunning(effectiveStatus(x, now)));
        const showAssignGroup = t.status === 'Waiting' && waitingMates.length > 1 && waitingMates[0].id === t.id;
        const showReturnGroup = isRunning(status) && runningMates.length > 1 && runningMates[0].id === t.id;
        return (
          <Card key={t.id} onLayout={onTicketLayout(t.id)}>
            <View style={{ flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' }}>
              <Txt style={{ fontSize: 28, lineHeight: 40, fontFamily: fonts.titleBold, color: color.ink }}>#{t.ticket_no}</Txt>
              <Light label={label} tint={tint} />
            </View>
            <Sub>{t.phone} · {t.total_minutes} phút · {money(t.amount_paid)}{t.vehicle_slug ? ` · ${t.vehicle_slug}` : ''}</Sub>
            {!!t.group_id && <Sub>Nhóm {t.group_size} xe cùng khách</Sub>}
            {t.due_at && isRunning(status) && <Countdown dueAt={t.due_at} />}
            <View style={{ flexDirection: 'row', gap: 8 }}>
              {t.status === 'Waiting' && <>
                <View style={{ flex: 1 }}><Btn small label={free ? `Gán ${free.slug}` : 'Chưa có xe trống'} disabled={!free} onPress={() => assign(t, free!.slug)} /></View>
                <Btn small kind="ghost" label="Hủy vé" onPress={() => act.run(() => api.cancelTicket(t.id))} />
              </>}
              {isRunning(status) && <>
                <View style={{ flex: 1 }}><Btn small label="Trả xe" onPress={() => giveBack(t)} /></View>
                {settings.data?.extend_enabled && <Btn small kind="ghost" label="Gia hạn 15 phút" onPress={() => extend(t)} />}
              </>}
            </View>
            {showAssignGroup && (
              <View style={{ flexDirection: 'row', gap: 8 }}>
                <View style={{ flex: 1 }}>
                  <Btn small
                    label={freeSlugs.length >= waitingMates.length ? `Gán cả nhóm (${waitingMates.length} xe)` : `Cần ${waitingMates.length} xe trống, đang có ${freeSlugs.length}`}
                    disabled={freeSlugs.length < waitingMates.length}
                    onPress={() => assignGroup(t.group_id!, freeSlugs.slice(0, waitingMates.length))} />
                </View>
                <Btn small kind="ghost" label="Hủy cả nhóm" onPress={() => cancelGroup(t.group_id!)} />
              </View>
            )}
            {showReturnGroup && <Btn small label={`Trả cả nhóm (${runningMates.length} xe)`} onPress={() => giveBackGroup(t.group_id!, runningMates)} />}
          </Card>
        );
      })}
    </Screen>
  );
}
