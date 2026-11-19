import React, { useState } from 'react';
import { View } from 'react-native';
import { api } from '../api';
import { Btn, Card, ErrorBox, Field, H, Light, Screen, Txt } from '../components/ui';
import { useAction, useApi } from '../hooks';
import { color, fonts } from '../theme';
import type { VehicleStatus } from '../types';
import { vehicleLabel } from '../utils';

const choices: [VehicleStatus, string][] = [['Online', 'Sẵn sàng'], ['Repairing', 'Sửa chữa'], ['Offline', 'Nghỉ']];
// Quy tắc đặt tên của backend: a-z, 0-9, dấu '-'; bắt đầu bằng chữ; không có '--'; không bắt đầu/kết thúc bằng '-'.
const slugOk = (s: string) => /^[a-z][a-z0-9]*(-[a-z0-9]+)*$/.test(s);

export default function VehiclesScreen() {
  const list = useApi(api.vehicles);
  const act = useAction(list.reload);
  const [slug, setSlug] = useState('');

  return (
    <Screen title="Xe điện" onRefresh={list.reload} refreshing={list.loading}>
      {act.error && <ErrorBox message={act.error} />}
      {list.error && <ErrorBox message={list.error.message} />}
      <Card>
        <H>Thêm xe</H>
        <Field label="Tên nhận diện (a-z, 0-9, dấu gạch ngang, bắt đầu bằng chữ)" value={slug} onChangeText={setSlug} autoCapitalize="none" placeholder="xe-gau-06" />
        <Btn label="Thêm xe" busy={act.busy} disabled={!slugOk(slug)}
          onPress={async () => { if (await act.run(() => api.addVehicle(slug))) setSlug(''); }} />
      </Card>
      {list.data?.map((v) => {
        const [label, tint] = vehicleLabel[v.status];
        const canDelete = v.status === 'Offline' || v.status === 'Repairing';
        return (
          <Card key={v.id}>
            <View style={{ flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' }}>
              <Txt style={{ fontSize: 18, lineHeight: 26, fontFamily: fonts.title, color: color.ink }}>{v.slug}</Txt>
              <Light label={label} tint={tint} />
            </View>
            {v.status !== 'Inused' && (
              <View style={{ flexDirection: 'row', gap: 8 }}>
                {choices.filter(([st]) => st !== v.status).map(([st, text]) => (
                  <View key={st} style={{ flex: 1 }}><Btn small kind="ghost" label={text} onPress={() => act.run(() => api.setVehicleStatus(v.id, st))} /></View>
                ))}
                {canDelete && <View style={{ flex: 1 }}><Btn small kind="danger" label="Xóa" onPress={() => act.run(() => api.deleteVehicle(v.id))} /></View>}
              </View>
            )}
          </Card>
        );
      })}
    </Screen>
  );
}
