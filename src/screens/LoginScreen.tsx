import React, { useState } from 'react';
import { Text, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useAuth } from '../auth';
import { Btn, ErrorBox, Field } from '../components/ui';
import { color } from '../theme';

export default function LoginScreen() {
  const { login } = useAuth();
  const [u, setU] = useState(''); const [p, setP] = useState('');
  const [busy, setBusy] = useState(false); const [err, setErr] = useState('');
  const submit = async () => {
    setBusy(true); setErr('');
    try { await login(u.trim(), p); } catch (e) { setErr((e as Error).message); } finally { setBusy(false); }
  };
  return (
    <SafeAreaView style={{ flex: 1, backgroundColor: color.bg, justifyContent: 'center', padding: 24 }}>
      <View style={{ gap: 14 }}>
        <Text style={{ fontSize: 30, fontWeight: '800', color: color.ink }}>Quản lý xe điện trẻ em</Text>
        <Text style={{ color: color.sub, marginBottom: 8 }}>Đăng nhập để bắt đầu phiên làm việc.</Text>
        {!!err && <ErrorBox message={err} />}
        <Field label="Tên đăng nhập" value={u} onChangeText={setU} autoCapitalize="none" />
        <Field label="Mật khẩu" value={p} onChangeText={setP} secureTextEntry />
        <Btn label="Đăng nhập" onPress={submit} busy={busy} disabled={!u || !p} />
      </View>
    </SafeAreaView>
  );
}
