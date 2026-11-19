import React, { useState } from 'react';
import { Alert, View } from 'react-native';
import { api } from '../api';
import { useNavigateWithAd } from '../ads';
import { useNow } from '../clock';
import { Btn, Card, Chips, ErrorBox, Field, H, Light, PastEndNotice, Screen, Sub, Txt } from '../components/ui';
import { refreshWaitingCount, useAction, useApi } from '../hooks';
import { color, fonts } from '../theme';
import { DEFAULT_SESSION_HOURS, checkSlot, elapsedText, formatDuration, formatVn, formatVnFull, isWithinBusinessHours, parseIso, parseSessionHours, plannedEndMs, suggestSlot, vnDateAfter, vnTime } from '../time';
import type { Session } from '../types';
import { money, sessionKind, sessionLabel } from '../utils';

export default function SessionScreen() {
  const now = useNow();
  const session = useApi(api.activeSession);
  const waiting = useApi(api.waitingSessions);
  const stats = useApi(async () => { try { return await api.currentStats(); } catch { return null; } });
  const vehicles = useApi(api.vehicles);
  const reload = () => { session.reload(); waiting.reload(); stats.reload(); vehicles.reload(); refreshWaitingCount(); };
  const act = useAction(reload);
  const go = useNavigateWithAd();
  const active = session.data;
  const none = !session.loading && !session.error && active === null;
  const startable = vehicles.data?.filter((v) => v.status === 'Online' || v.status === 'Offline') ?? [];
  const slugs = startable.map((v) => v.slug);
  const [hoursText, setHoursText] = useState(DEFAULT_SESSION_HOURS);
  const hours = parseSessionHours(hoursText);
  const open = isWithinBusinessHours(vnTime(now));
  const endMs = hours === null ? null : plannedEndMs(now, hours);
  const canStart = open && endMs !== null;

  const start = (sessionId?: number) => {
    if (!canStart || endMs === null) return;
    act.run(() => api.startSession(slugs, sessionId, vnTime(endMs)));
  };

  const create = async (date: string, time: string) => (await act.run(() => api.scheduleSession(date, time))) !== undefined;
  const save = async (id: number, date: string, time: string) => (await act.run(() => api.updateSession(id, date, time))) !== undefined;
  const cancel = (target: Session) => Alert.alert(
    `Hủy phiên #${target.id}?`,
    `Phiên hẹn lúc ${formatVnFull(target.scheduled_at)} sẽ bị hủy và không bắt đầu lại được.`,
    [{ text: 'Giữ lại', style: 'cancel' }, { text: 'Hủy phiên', style: 'destructive', onPress: () => { act.run(() => api.cancelSession(target.id)); } }],
  );

  return (
    <Screen title="Phiên làm" onRefresh={reload} refreshing={session.loading}>
      {act.error && <ErrorBox message={act.error} />}
      {session.error && <ErrorBox message={session.error.message} />}

      {none && (
        <Card>
          <H>Chưa có phiên đang làm</H>
          <Sub>Phiên dùng tất cả xe đang sẵn sàng hoặc nghỉ (xe sửa chữa được bỏ qua).</Sub>
          <Field label="Thời lượng phiên (giờ)" value={hoursText} onChangeText={setHoursText} keyboardType="number-pad" />
          <Chips value={hoursText} onChange={setHoursText}
            options={[{ value: '4', label: '4 giờ' }, { value: '6', label: '6 giờ' }, { value: '8', label: '8 giờ' }, { value: '10', label: '10 giờ' }]} />
          {!open && <ErrorBox message="Ngoài giờ làm việc (06:00 đến 23:00), chưa bắt đầu được phiên." />}
          {endMs === null
            ? <ErrorBox message="Thời lượng phải là số giờ nguyên từ 1 đến 24" />
            : <Sub>Kết thúc dự kiến lúc {formatVn(new Date(endMs).toISOString(), now)}, không quá 23:00. Áp dụng cả khi bắt đầu phiên hẹn giờ bên dưới.</Sub>}
          <Btn label={`Bắt đầu phiên với ${startable.length} xe`} busy={act.busy} disabled={!startable.length || !canStart}
            onPress={() => start()} />
        </Card>
      )}

      {active && (
        <>
          <PastEndNotice session={active} />
          <Btn label="Xem danh sách vé" kind="ghost" onPress={() => go('Vé')} />
          <Card>
            <Light label={sessionLabel.InProgress[0]} tint={sessionLabel.InProgress[1]} />
            <H>Phiên #{active.id} · {sessionKind(active)}</H>
            {!active.is_instant && <Sub>Hẹn lúc {formatVnFull(active.scheduled_at)}</Sub>}
            <Sub>Bắt đầu lúc {formatVn(active.started_at, now)}</Sub>
            <Sub>Đã chạy {elapsedText(active.started_at, now)}</Sub>
            <Sub>Kết thúc dự kiến {formatVn(active.planned_end_at, now)} · {active.vehicles.length} xe</Sub>
          </Card>
          {stats.data && (
            <Card>
              <H>Thống kê hiện tại</H>
              <View style={{ flexDirection: 'row', gap: 12 }}>
                <Stat label="Số vé" value={String(stats.data.ticket_count)} />
                <Stat label="Tiền mặt" value={money(stats.data.revenue_cash)} />
                <Stat label="Chuyển khoản" value={money(stats.data.revenue_bank)} />
              </View>
            </Card>
          )}
          <Btn label="Kết thúc phiên" kind="danger" busy={act.busy} onPress={() => act.run(api.endSession)} />
        </>
      )}

      <ScheduleForm now={now} busy={act.busy} onCreate={create} />
      {waiting.error && <ErrorBox message={waiting.error.message} />}
      {waiting.data?.map((w) => (
        <WaitingCard key={w.id} session={w} now={now} busy={act.busy}
          startBlock={active ? 'Đang có phiên làm, hãy kết thúc phiên đó trước.' : !slugs.length ? 'Chưa có xe sẵn sàng hoặc nghỉ để bắt đầu.' : !open ? 'Ngoài giờ làm việc 06:00 đến 23:00, chưa bắt đầu được.' : endMs === null ? 'Thời lượng phiên chưa hợp lệ.' : null}
          onStart={() => start(w.id)} onSave={save} onCancel={() => cancel(w)} />
      ))}
    </Screen>
  );
}

function ScheduleForm({ now, busy, onCreate }: { now: number; busy: boolean; onCreate: (date: string, time: string) => Promise<boolean> }) {
  const [open, setOpen] = useState(false);
  const [slot, setSlot] = useState(() => suggestSlot(now));
  const [problem, setProblem] = useState<string | null>(null);
  const today = vnDateAfter(now, 0);
  const tomorrow = vnDateAfter(now, 1);

  const submit = async () => {
    const issue = checkSlot(slot.date, slot.time, now);
    setProblem(issue);
    if (issue) return;
    if (await onCreate(slot.date.trim(), slot.time.trim())) {
      setSlot(suggestSlot(now));
      setOpen(false);
    }
  };

  if (!open) return <Btn label="Hẹn phiên mới" kind="ghost" onPress={() => { setSlot(suggestSlot(now)); setOpen(true); }} />;
  return (
    <Card>
      <H>Hẹn phiên mới</H>
      <Sub>Nhập theo giờ Việt Nam, trong khung 06:00 đến 23:00. Phiên hẹn giờ ở trạng thái chờ, bấm bắt đầu khi tới giờ.</Sub>
      <Chips value={slot.date} onChange={(date) => setSlot({ ...slot, date })}
        options={[{ value: today, label: 'Hôm nay' }, { value: tomorrow, label: 'Ngày mai' }]} />
      <Field label="Ngày (yyyy-MM-dd)" value={slot.date} onChangeText={(date) => setSlot({ ...slot, date })} keyboardType="numbers-and-punctuation" />
      <Field label="Giờ (HH:mm)" value={slot.time} onChangeText={(time) => setSlot({ ...slot, time })} keyboardType="numbers-and-punctuation" />
      {problem && <ErrorBox message={problem} />}
      <View style={{ flexDirection: 'row', gap: 8 }}>
        <View style={{ flex: 1 }}><Btn label="Hẹn phiên" busy={busy} onPress={submit} /></View>
        <Btn kind="ghost" label="Đóng" onPress={() => { setProblem(null); setOpen(false); }} />
      </View>
    </Card>
  );
}

function WaitingCard({ session, now, busy, startBlock, onStart, onSave, onCancel }: {
  session: Session; now: number; busy: boolean; startBlock: string | null;
  onStart: () => void; onSave: (id: number, date: string, time: string) => Promise<boolean>; onCancel: () => void;
}) {
  const [editing, setEditing] = useState(false);
  const [slot, setSlot] = useState({ date: session.scheduled_date, time: session.scheduled_time });
  const [problem, setProblem] = useState<string | null>(null);
  const scheduledMs = parseIso(session.scheduled_at);
  const late = scheduledMs !== null && scheduledMs < now;

  const submit = async () => {
    const issue = checkSlot(slot.date, slot.time, now);
    setProblem(issue);
    if (issue) return;
    if (await onSave(session.id, slot.date.trim(), slot.time.trim())) setEditing(false);
  };

  return (
    <Card>
      <Light label={sessionLabel.Waiting[0]} tint={sessionLabel.Waiting[1]} />
      <H>Phiên #{session.id} · {sessionKind(session)}</H>
      <Sub>Hẹn lúc {formatVnFull(session.scheduled_at)}</Sub>
      {late && scheduledMs !== null && <Txt style={{ color: color.wait, fontFamily: fonts.bodyBold }}>Đã qua giờ hẹn {formatDuration(now - scheduledMs)}, chưa bắt đầu</Txt>}
      {editing ? (
        <>
          <Field label="Ngày (yyyy-MM-dd)" value={slot.date} onChangeText={(date) => setSlot({ ...slot, date })} keyboardType="numbers-and-punctuation" />
          <Field label="Giờ (HH:mm)" value={slot.time} onChangeText={(time) => setSlot({ ...slot, time })} keyboardType="numbers-and-punctuation" />
          {problem && <ErrorBox message={problem} />}
          <View style={{ flexDirection: 'row', gap: 8 }}>
            <View style={{ flex: 1 }}><Btn small label="Lưu" busy={busy} onPress={submit} /></View>
            <Btn small kind="ghost" label="Đóng" onPress={() => { setProblem(null); setEditing(false); }} />
          </View>
        </>
      ) : (
        <>
          <Btn small label="Bắt đầu phiên này" busy={busy} disabled={startBlock !== null} onPress={onStart} />
          {startBlock !== null && <Sub>{startBlock}</Sub>}
          <View style={{ flexDirection: 'row', gap: 8 }}>
            <View style={{ flex: 1 }}><Btn small kind="ghost" label="Sửa giờ hẹn" onPress={() => { setSlot({ date: session.scheduled_date, time: session.scheduled_time }); setEditing(true); }} /></View>
            <View style={{ flex: 1 }}><Btn small kind="danger" label="Hủy phiên" disabled={busy} onPress={onCancel} /></View>
          </View>
        </>
      )}
    </Card>
  );
}

const Stat = ({ label, value }: { label: string; value: string }) => (
  <View style={{ flex: 1 }}><Txt style={{ fontSize: 20, lineHeight: 28, fontFamily: fonts.titleBold, color: color.ink }}>{value}</Txt><Sub>{label}</Sub></View>
);
