import React, { useEffect, useState } from 'react';
import { Switch, Text, View } from 'react-native';
import { api } from '../api';
import { useAuth } from '../auth';
import { Btn, Card, ErrorBox, Field, H, Screen, Sub } from '../components/ui';
import { useAction, useApi } from '../hooks';
import { color } from '../theme';
import { money } from '../utils';

export default function SettingsScreen() {
  const { logout } = useAuth();
  const st = useApi(api.settings);
  const act = useAction(st.reload);
  const [fee, setFee] = useState(''); const [grace, setGrace] = useState(''); const [min, setMin] = useState('');
  useEffect(() => {
    if (st.data) { setFee(String(st.data.overdue_fee_per_minute)); setGrace(String(st.data.overdue_grace_minutes)); setMin(String(st.data.minimum_minutes)); }
  }, [st.data]);
  const d = st.data;

  return (
    <Screen title="Cài đặt" onRefresh={st.reload} refreshing={st.loading}>
      {(act.error || st.error) && <ErrorBox message={act.error ?? st.error!.message} />}
      {d && <>
        <Card>
          <Row label="Hoàn tiền khi trả xe sớm" value={d.refund_enabled} onChange={(v) => act.run(() => api.toggleRefund(v))} />
          <Row label="Cho phép gia hạn thêm giờ" value={d.extend_enabled} onChange={(v) => act.run(() => api.toggleExtend(v))} />
          <Sub>Thay đổi chỉ áp dụng cho vé tạo sau đó.</Sub>
        </Card>
        <Card>
          <H>Bảng giá</H>
          {d.packages.map((p) => <Text key={p.minutes} style={{ fontSize: 16, color: color.ink }}>{money(p.amount)} / {p.minutes} phút{p.is_default ? '  (mặc định)' : ''}</Text>)}
        </Card>
        <Card>
          <H>Quá giờ và thời gian tối thiểu</H>
          <Field label="Phụ phí quá giờ (đồng/phút)" value={fee} onChangeText={setFee} keyboardType="number-pad" onEndEditing={() => act.run(() => api.setOverdueFee(+fee))} />
          <Field label="Số phút trễ được bỏ qua" value={grace} onChangeText={setGrace} keyboardType="number-pad" onEndEditing={() => act.run(() => api.setOverdueLimit(+grace))} />
          <Field label="Thời gian thuê tối thiểu (phút)" value={min} onChangeText={setMin} keyboardType="number-pad" onEndEditing={() => act.run(() => api.setMinimumTime(+min))} />
        </Card>
      </>}
      <Btn label="Đăng xuất" kind="ghost" onPress={logout} />
    </Screen>
  );
}

const Row = ({ label, value, onChange }: { label: string; value: boolean; onChange: (v: boolean) => void }) => (
  <View style={{ flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' }}>
    <Text style={{ fontSize: 16, color: color.ink, flex: 1 }}>{label}</Text>
    <Switch value={value} onValueChange={onChange} trackColor={{ true: color.go }} />
  </View>
);
