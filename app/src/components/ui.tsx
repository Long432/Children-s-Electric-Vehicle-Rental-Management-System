import React, { ReactNode } from 'react';
import { ActivityIndicator, Pressable, RefreshControl, ScrollView, StyleSheet, Text, TextInput, TextInputProps, View } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { color, font, radius } from '../theme';

export function Screen({ title, children, onRefresh, refreshing = false }: { title: string; children: ReactNode; onRefresh?: () => void; refreshing?: boolean }) {
  const { top } = useSafeAreaInsets();
  return (
    <ScrollView style={{ backgroundColor: color.bg }} contentContainerStyle={{ paddingTop: top + 12, padding: 16, paddingBottom: 40, gap: 12 }}
      keyboardShouldPersistTaps="handled" refreshControl={onRefresh ? <RefreshControl refreshing={refreshing} onRefresh={onRefresh} /> : undefined}>
      <Text style={s.title}>{title}</Text>
      {children}
    </ScrollView>
  );
}

export const Card = ({ children, style }: { children: ReactNode; style?: object }) => <View style={[s.card, style]}>{children}</View>;
export const H = ({ children }: { children: ReactNode }) => <Text style={s.h}>{children}</Text>;
export const Sub = ({ children }: { children: ReactNode }) => <Text style={s.sub}>{children}</Text>;

/** Đèn trạng thái: chấm màu + nhãn. Màu mạnh nhất trên màn hình, để liếc là biết. */
export function Light({ label, tint }: { label: string; tint: string }) {
  return (
    <View style={[s.light, { backgroundColor: tint + '1F' }]}>
      <View style={[s.dot, { backgroundColor: tint }]} />
      <Text style={{ color: tint, fontWeight: '700', fontSize: font.small }}>{label}</Text>
    </View>
  );
}

export function Btn({ label, onPress, kind = 'primary', busy, disabled, small }: { label: string; onPress: () => void; kind?: 'primary' | 'ghost' | 'danger'; busy?: boolean; disabled?: boolean; small?: boolean }) {
  const bg = kind === 'primary' ? color.action : kind === 'danger' ? color.stop : 'transparent';
  const fg = kind === 'ghost' ? color.action : '#fff';
  return (
    <Pressable onPress={onPress} disabled={busy || disabled} style={({ pressed }) => [s.btn, { backgroundColor: bg, opacity: disabled ? 0.4 : pressed ? 0.8 : 1, minHeight: small ? 40 : 52 }, kind === 'ghost' && { borderWidth: 1.5, borderColor: color.action }]}>
      {busy ? <ActivityIndicator color={fg} /> : <Text style={{ color: fg, fontWeight: '700', fontSize: small ? 14 : font.body }}>{label}</Text>}
    </Pressable>
  );
}

export const Field = (p: TextInputProps & { label: string }) => (
  <View style={{ gap: 6 }}>
    <Text style={s.sub}>{p.label}</Text>
    <TextInput placeholderTextColor={color.idle} {...p} style={s.input} />
  </View>
);

export function Chips<T extends string | number>({ options, value, onChange }: { options: { value: T; label: string }[]; value: T; onChange: (v: T) => void }) {
  return (
    <View style={{ flexDirection: 'row', flexWrap: 'wrap', gap: 8 }}>
      {options.map((o) => (
        <Pressable key={String(o.value)} onPress={() => onChange(o.value)} style={[s.chip, o.value === value && { backgroundColor: color.ink, borderColor: color.ink }]}>
          <Text style={{ color: o.value === value ? '#fff' : color.ink, fontWeight: '600' }}>{o.label}</Text>
        </Pressable>
      ))}
    </View>
  );
}

export const ErrorBox = ({ message }: { message: string }) => (
  <View style={[s.card, { backgroundColor: color.stop + '14', borderColor: color.stop }]}><Text style={{ color: color.stop, fontWeight: '600' }}>{message}</Text></View>
);

const s = StyleSheet.create({
  title: { fontSize: font.title, fontWeight: '800', color: color.ink },
  card: { backgroundColor: color.card, borderRadius: radius.card, padding: 16, gap: 10, borderWidth: 1, borderColor: color.line },
  h: { fontSize: font.h, fontWeight: '700', color: color.ink },
  sub: { fontSize: font.small, color: color.sub },
  light: { flexDirection: 'row', alignItems: 'center', gap: 6, paddingHorizontal: 10, paddingVertical: 5, borderRadius: radius.chip, alignSelf: 'flex-start' },
  dot: { width: 9, height: 9, borderRadius: 5 },
  btn: { borderRadius: radius.btn, alignItems: 'center', justifyContent: 'center', paddingHorizontal: 18 },
  input: { borderWidth: 1.5, borderColor: color.line, borderRadius: 12, paddingHorizontal: 14, height: 50, fontSize: font.body, color: color.ink, backgroundColor: '#fff' },
  chip: { paddingHorizontal: 14, paddingVertical: 10, borderRadius: radius.chip, borderWidth: 1.5, borderColor: color.line, backgroundColor: '#fff' },
});
