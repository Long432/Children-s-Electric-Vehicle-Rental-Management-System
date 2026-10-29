import React, { useState } from 'react';
import { Text, View } from 'react-native';
import { api } from '../api';
import { Btn, Card, Chips, ErrorBox, Field, H, Light, Screen, Sub } from '../components/ui';
import { useAction, useApi } from '../hooks';
import { color } from '../theme';
import type { PaymentMethod, Ticket } from '../types';
import { minutesLeft, money, ticketLabel } from '../utils';

export default function TicketsScreen() {
  const [q, setQ] = useState('');
  const list = useApi(() => (q.trim() ? api.searchTickets(q.trim()) : api.tickets()));
  const settings = useApi(api.settings);
  const vehicles = useApi(api.vehicles);
  const reload = () => { list.reload(); vehicles.reload(); };
  const act = useAction(reload);
  const [phone, setPhone] = useState(''); const [minutes, setMinutes] = useState(30); const [pay, setPay] = useState<PaymentMethod>('Cash');
  const [notice, setNotice] = useState('');
  const free = vehicles.data?.find((v) => v.status === 'Online');

  const create = async () => {
    const t = await act.run(() => api.createTicket(phone, minutes, pay));
    if (t) { setNotice(`Đã tạo vé #${t.ticket_no}. Thu ${money(t.amount_paid)}.`); setPhone(''); }
  };
  const giveBack = async (t: Ticket) => {
    const r = await act.run(() => api.returnTicket(t.id, 'Cash'));
    if (r) setNotice([`Đã trả vé #${r.ticket_no}.`, r.overdue_fee ? `Thu phụ phí ${money(r.overdue_fee)}.` : '', r.refund_amount ? `Hoàn ${money(r.refund_amount)}.` : '', r.next_ticket ? `Gán xe cho vé #${r.next_ticket.ticket_no} (${r.next_ticket.phone}).` : ''].filter(Boolean).join(' '));
  };

  return (
    <Screen title="Vé" onRefresh={reload} refreshing={list.loading}>
      {act.error && <ErrorBox message={act.error} />}
      {!!notice && <Card style={{ borderColor: color.go }}><Text style={{ color: color.ink, fontWeight: '600' }}>{notice}</Text></Card>}

      <Card>
        <H>Tạo vé mới</H>
        <Field label="Số điện thoại" value={phone} onChangeText={setPhone} keyboardType="phone-pad" placeholder="09xxxxxxxx" />
        <Chips value={minutes} onChange={setMinutes} options={(settings.data?.packages ?? []).map((p) => ({ value: p.minutes, label: `${p.minutes} phút · ${money(p.amount)}` }))} />
        <Chips value={pay} onChange={setPay} options={[{ value: 'Cash', label: 'Tiền mặt' }, { value: 'Bank', label: 'Chuyển khoản' }]} />
        <Btn label="Tạo vé và thu tiền" onPress={create} busy={act.busy} disabled={phone.length < 9} />
      </Card>

      <Field label="Tìm vé" value={q} onChangeText={setQ} onSubmitEditing={list.reload} returnKeyType="search" placeholder="Số điện thoại hoặc mã vé" />

      {list.data?.length === 0 && <Sub>Không có vé nào phù hợp.</Sub>}
      {list.data?.map((t) => {
        const [label, tint] = ticketLabel[t.status]; const left = minutesLeft(t.due_at);
        return (
          <Card key={t.id}>
            <View style={{ flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' }}>
              <Text style={{ fontSize: 28, fontWeight: '800', color: color.ink }}>#{t.ticket_no}</Text>
              <Light label={label} tint={tint} />
            </View>
            <Sub>{t.phone} · {t.total_minutes} phút · {money(t.amount_paid)}{t.vehicle_slug ? ` · ${t.vehicle_slug}` : ''}</Sub>
            {left !== null && t.status !== 'Completed' && <Text style={{ color: left < 0 ? color.stop : color.ink, fontWeight: '700' }}>{left < 0 ? `Quá ${-left} phút` : `Còn ${left} phút`}</Text>}
            <View style={{ flexDirection: 'row', gap: 8 }}>
              {t.status === 'Waiting' && <>
                <View style={{ flex: 1 }}><Btn small label={free ? `Gán ${free.slug}` : 'Chưa có xe trống'} disabled={!free} onPress={() => act.run(() => api.assignTicket(t.id, free!.slug))} /></View>
                <Btn small kind="ghost" label="Hủy vé" onPress={() => act.run(() => api.cancelTicket(t.id))} />
              </>}
              {(t.status === 'Inused' || t.status === 'Overdue') && <>
                <View style={{ flex: 1 }}><Btn small label="Trả xe" onPress={() => giveBack(t)} /></View>
                {settings.data?.extend_enabled && <Btn small kind="ghost" label="Gia hạn 15 phút" onPress={() => act.run(() => api.extendTicket(t.id, 15, 'Cash'))} />}
              </>}
            </View>
          </Card>
        );
      })}
    </Screen>
  );
}
