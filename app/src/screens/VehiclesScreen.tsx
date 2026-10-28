import React, { useState } from 'react';
import { Text, View } from 'react-native';
import { api } from '../api';
import { Btn, Card, ErrorBox, Field, H, Light, Screen } from '../components/ui';
import { useAction, useApi } from '../hooks';
import { color } from '../theme';
import type { VehicleStatus } from '../types';
import { vehicleLabel } from '../utils';

const choices: [VehicleStatus, string][] = [['Online', 'Sẵn sàng'], ['Repairing', 'Sửa chữa'], ['Offline', 'Nghỉ']];

export default function VehiclesScreen() {
  const list = useApi(api.vehicles);
  const act = useAction(list.reload);
  const [slug, setSlug] = useState('');

  return (
    <Screen title="Xe điện" onRefresh={list.reload} refreshing={list.loading}>
      {act.error && <ErrorBox message={act.error} />}
      <Card>
        <H>Thêm xe</H>
        <Field label="Tên nhận diện (không dấu, nối bằng gạch ngang)" value={slug} onChangeText={setSlug} autoCapitalize="none" placeholder="xe-gau-06" />
        <Btn label="Thêm xe" busy={act.busy} disabled={!/^[a-z0-9]+(-[a-z0-9]+)*$/.test(slug)}
          onPress={async () => { if (await act.run(() => api.addVehicle(slug))) setSlug(''); }} />
      </Card>
      {list.data?.map((v) => {
        const [label, tint] = vehicleLabel[v.status];
        return (
          <Card key={v.id}>
            <View style={{ flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' }}>
              <Text style={{ fontSize: 18, fontWeight: '700', color: color.ink }}>{v.slug}</Text>
              <Light label={label} tint={tint} />
            </View>
            {v.status !== 'Inused' && (
              <View style={{ flexDirection: 'row', gap: 8 }}>
                {choices.filter(([st]) => st !== v.status).map(([st, text]) => (
                  <View key={st} style={{ flex: 1 }}><Btn small kind="ghost" label={text} onPress={() => act.run(() => api.setVehicleStatus(v.id, st))} /></View>
                ))}
              </View>
            )}
          </Card>
        );
      })}
    </Screen>
  );
}
