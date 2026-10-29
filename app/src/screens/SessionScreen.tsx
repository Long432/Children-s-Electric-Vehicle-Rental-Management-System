import React from 'react';
import { Text, View } from 'react-native';
import { api } from '../api';
import { useNavigateWithAd } from '../ads';
import { Btn, Card, ErrorBox, H, Light, Screen, Sub } from '../components/ui';
import { useAction, useApi } from '../hooks';
import { color } from '../theme';
import { money } from '../utils';

export default function SessionScreen() {
  const session = useApi(api.currentSession);
  const stats = useApi(async () => { try { return await api.currentStats(); } catch { return null; } });
  const vehicles = useApi(api.vehicles);
  const reload = () => { session.reload(); stats.reload(); vehicles.reload(); };
  const act = useAction(reload);
  const go = useNavigateWithAd();
  const none = session.error?.code === 'NO_ACTIVE_SESSION';
  const ready = vehicles.data?.filter((v) => v.status === 'Online') ?? [];

  return (
    <Screen title="Phiên làm" onRefresh={reload} refreshing={session.loading}>
      {act.error && <ErrorBox message={act.error} />}
      {session.error && !none && <ErrorBox message={session.error.message} />}

      {none && (
        <Card>
          <H>Chưa có phiên đang làm</H>
          <Sub>Chọn xe sẽ chạy hôm nay rồi bắt đầu. Phiên mặc định kết thúc lúc 0 giờ.</Sub>
          <Btn label={`Bắt đầu phiên với ${ready.length} xe sẵn sàng`} busy={act.busy} disabled={!ready.length}
            onPress={() => act.run(() => api.startSession(ready.map((v) => v.slug)))} />
        </Card>
      )}

      {session.data && (
        <>
          <Btn label="Xem danh sách vé" kind="ghost" onPress={() => go('Vé')} />
          <Card>
            <Light label="Đang làm" tint={color.go} />
            <H>Phiên #{session.data.id}</H>
            <Sub>Kết thúc dự kiến {new Date(session.data.end_time).toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' })} · {session.data.vehicle_count} xe</Sub>
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
    </Screen>
  );
}

const Stat = ({ label, value }: { label: string; value: string }) => (
  <View style={{ flex: 1 }}><Text style={{ fontSize: 20, fontWeight: '800', color: color.ink }}>{value}</Text><Sub>{label}</Sub></View>
);
