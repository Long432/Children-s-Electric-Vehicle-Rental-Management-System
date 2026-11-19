import React, { useState } from 'react';
import { View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useAuth } from '../auth';
import { Btn, ErrorBox, Field, Txt } from '../components/ui';
import { color, fonts, lineHeight } from '../theme';

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
        <Txt style={{ fontSize: 30, lineHeight: lineHeight.big + 4, fontFamily: fonts.titleBold, color: color.ink }}>Quản lý xe điện vui chơi</Txt>
        <Txt style={{ color: color.sub, marginBottom: 8 }}>Đăng nhập để bắt đầu phiên làm việc.</Txt>
        {!!err && <ErrorBox message={err} />}
        <Field label="Tên đăng nhập" value={u} onChangeText={setU} autoCapitalize="none" />
        <Field label="Mật khẩu" value={p} onChangeText={setP} secureTextEntry />
        <Btn label="Đăng nhập" onPress={submit} busy={busy} disabled={!u || !p} />
      </View>
    </SafeAreaView>
  );
}
