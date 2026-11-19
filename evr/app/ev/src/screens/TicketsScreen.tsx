import React, { useState } from 'react';
import { View } from 'react-native';
import { api } from '../api';
import { Btn, Card, Chips, Countdown, ErrorBox, Field, H, Light, PastEndNotice, Screen, SearchField, Sub, Txt } from '../components/ui';
import { useNow } from '../clock';
import { refreshWaitingCount, useAction, useApi, useDebounced, useTicketAlerts } from '../hooks';
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
  const [notice, setNotice] = useState('');
  const free = session.data?.vehicles.find((v) => v.status === 'Online');
  const pastEnd = isPastPlannedEnd(session.data, now);

  const create = async () => {
    const t = await act.run(() => api.createTicket(normalizePhone(phone), minutes, pay));
    if (t) { setNotice(`Đã tạo vé #${t.ticket_no}. Thu ${money(t.amount_paid)}.`); setPhone(''); }
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

  return (
    <Screen title="Vé" onRefresh={reload} refreshing={list.loading}>
      {act.error && <ErrorBox message={act.error} />}
      {list.error && <ErrorBox message={list.error.message} />}
      {!!notice && <Card style={{ borderColor: color.go }}><Txt style={{ color: color.ink, fontFamily: fonts.bodySemi }}>{notice}</Txt></Card>}

      <PastEndNotice session={session.data} />

      <Card>
        <H>Tạo vé mới</H>
        <Field label="Số điện thoại" value={phone} onChangeText={setPhone} keyboardType="phone-pad" placeholder="09xxxxxxxx" error={phoneError(phone)} />
        <Chips value={minutes} onChange={setMinutes} options={(settings.data?.packages ?? []).map((p) => ({ value: p.minutes, label: `${p.minutes} phút · ${money(p.amount)}` }))} />
        <Chips value={pay} onChange={setPay} options={[{ value: 'Cash', label: 'Tiền mặt' }, { value: 'Bank', label: 'Chuyển khoản' }]} />
        <Btn label="Tạo vé và thu tiền" onPress={create} busy={act.busy} disabled={!isPhoneValid(phone) || pastEnd} />
        {pastEnd && <Sub>Đã quá giờ kết thúc dự kiến nên không tạo được vé mới.</Sub>}
      </Card>

      <SearchField label="Tìm vé" value={q} onChangeText={setQ} onClear={() => setQ('')} onSubmitEditing={list.reload} returnKeyType="search" placeholder="Số điện thoại hoặc mã vé" />

      {!list.error && !list.loading && list.data?.length === 0 && <Sub>{keyword ? 'Không tìm thấy vé' : 'Không có vé nào phù hợp.'}</Sub>}
      {list.data?.map((t) => {
        const status = effectiveStatus(t, now);
        const [label, tint] = ticketLabel[status];
        return (
          <Card key={t.id}>
            <View style={{ flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' }}>
              <Txt style={{ fontSize: 28, lineHeight: 40, fontFamily: fonts.titleBold, color: color.ink }}>#{t.ticket_no}</Txt>
              <Light label={label} tint={tint} />
            </View>
            <Sub>{t.phone} · {t.total_minutes} phút · {money(t.amount_paid)}{t.vehicle_slug ? ` · ${t.vehicle_slug}` : ''}</Sub>
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
          </Card>
        );
      })}
    </Screen>
  );
}
