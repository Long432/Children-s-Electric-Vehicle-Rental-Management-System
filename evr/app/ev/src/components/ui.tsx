import React, { ReactNode } from 'react';
import { ActivityIndicator, LayoutChangeEvent, Pressable, RefreshControl, ScrollView, StyleSheet, Text, TextInput, TextInputProps, TextProps, View } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { useNow } from '../clock';
import { color, font, fonts, lineHeight, radius } from '../theme';
import { formatDuration } from '../time';
import type { Session } from '../types';
import { countdownAt, countdownTint, overPlannedEndMs } from '../utils';

/** Chữ thường của app: mặc định dùng phông mô tả, style truyền vào được ghi đè. */
export const Txt = ({ style, ...rest }: TextProps) => <Text {...rest} style={[{ fontFamily: fonts.body }, style]} />;

export function Screen({ title, children, onRefresh, refreshing = false, scrollRef }: { title: string; children: ReactNode; onRefresh?: () => void; refreshing?: boolean; scrollRef?: React.RefObject<ScrollView> }) {
  const { top } = useSafeAreaInsets();
  return (
    <ScrollView ref={scrollRef} style={{ backgroundColor: color.bg }} contentContainerStyle={{ paddingTop: top + 12, padding: 16, paddingBottom: 40, gap: 12 }}
      keyboardShouldPersistTaps="handled" refreshControl={onRefresh ? <RefreshControl refreshing={refreshing} onRefresh={onRefresh} /> : undefined}>
      <Text style={s.title}>{title}</Text>
      {children}
    </ScrollView>
  );
}

export const Card = ({ children, style, onLayout }: { children: ReactNode; style?: object; onLayout?: (e: LayoutChangeEvent) => void }) => <View style={[s.card, style]} onLayout={onLayout}>{children}</View>;
export const H = ({ children }: { children: ReactNode }) => <Text style={s.h}>{children}</Text>;
export const Sub = ({ children }: { children: ReactNode }) => <Text style={s.sub}>{children}</Text>;

/** Đèn trạng thái: chấm màu + nhãn. Màu mạnh nhất trên màn hình, để liếc là biết. */
export function Light({ label, tint }: { label: string; tint: string }) {
  return (
    <View style={[s.light, { backgroundColor: tint + '1F' }]}>
      <View style={[s.dot, { backgroundColor: tint }]} />
      <Text style={{ color: tint, fontFamily: fonts.bodyBold, fontSize: font.small }}>{label}</Text>
    </View>
  );
}

export function Countdown({ dueAt }: { dueAt: string }) {
  const now = useNow();
  const countdown = countdownAt(dueAt, now);
  const tint = countdownTint[countdown.level];
  const overdue = countdown.level === 'overdue';
  const ink = overdue ? '#fff' : tint;
  return (
    <View accessibilityRole="timer" accessibilityLabel={countdown.speech} style={[s.countdown, { backgroundColor: overdue ? tint : tint + '1F', borderColor: tint }]}>
      <Text style={[s.countdownCaption, { color: ink }]}>{overdue ? 'QUÁ GIỜ' : 'CÒN LẠI'}</Text>
      <View style={{ flexDirection: 'row' }} importantForAccessibility="no-hide-descendants">
        {countdown.text.split('').map((ch, i) => (
          <Text key={i} style={[s.countdownDigits, /\d/.test(ch) && s.digitBox, { color: ink }]}>{ch}</Text>
        ))}
      </View>
    </View>
  );
}

export function Btn({ label, onPress, kind = 'primary', busy, disabled, small }: { label: string; onPress: () => void; kind?: 'primary' | 'ghost' | 'danger'; busy?: boolean; disabled?: boolean; small?: boolean }) {
  const bg = kind === 'primary' ? color.action : kind === 'danger' ? color.stop : 'transparent';
  const fg = kind === 'ghost' ? color.action : '#fff';
  return (
    <Pressable onPress={onPress} disabled={busy || disabled} style={({ pressed }) => [s.btn, { backgroundColor: bg, opacity: disabled ? 0.4 : pressed ? 0.8 : 1, minHeight: small ? 40 : 52 }, kind === 'ghost' && { borderWidth: 1.5, borderColor: color.action }]}>
      {busy ? <ActivityIndicator color={fg} /> : <Text style={{ color: fg, fontFamily: fonts.bodyBold, fontSize: small ? 14 : font.body }}>{label}</Text>}
    </Pressable>
  );
}

export function PastEndNotice({ session }: { session: Session | null }) {
  const now = useNow();
  const over = overPlannedEndMs(session, now);
  if (over === null) return null;
  return (
    <View accessibilityRole="alert" style={[s.card, { backgroundColor: color.stop + '14', borderColor: color.stop }]}>
      <Text style={{ color: color.stop, fontFamily: fonts.titleBold, fontSize: font.h, lineHeight: lineHeight.h }}>Đã quá giờ kết thúc dự kiến</Text>
      <Txt style={{ color: color.ink, lineHeight: 22 }}>
        {over < 60000 ? 'Vừa quá giờ. ' : `Quá ${formatDuration(over)}. `}
        Phiên không tự kết thúc: xử lý các vé còn lại rồi kết thúc phiên. Trong lúc này không tạo được vé mới.
      </Txt>
    </View>
  );
}

export const Field = ({ error, ...p }: TextInputProps & { label: string; error?: string | null }) => (
  <View style={{ gap: 6 }}>
    <Text style={s.sub}>{p.label}</Text>
    <TextInput placeholderTextColor={color.idle} {...p} style={[s.input, !!error && { borderColor: color.stop }]} />
    {!!error && <Text style={{ color: color.stop, fontFamily: fonts.bodySemi }}>{error}</Text>}
  </View>
);

export function SearchField({ label, value, onClear, ...rest }: TextInputProps & { label: string; value: string; onClear: () => void }) {
  return (
    <View style={{ gap: 6 }}>
      <Text style={s.sub}>{label}</Text>
      <View>
        <TextInput placeholderTextColor={color.idle} value={value} {...rest} style={[s.input, { paddingRight: 48 }]} />
        {value.length > 0 && (
          <Pressable onPress={onClear} accessibilityRole="button" accessibilityLabel="Xóa ô tìm" hitSlop={8} style={s.clear}>
            <Text style={{ color: color.sub, fontSize: 20, fontFamily: fonts.bodyBold }}>✕</Text>
          </Pressable>
        )}
      </View>
    </View>
  );
}

export function Chips<T extends string | number>({ options, value, onChange }: { options: { value: T; label: string }[]; value: T; onChange: (v: T) => void }) {
  return (
    <View style={{ flexDirection: 'row', flexWrap: 'wrap', gap: 8 }}>
      {options.map((o) => (
        <Pressable key={String(o.value)} onPress={() => onChange(o.value)} style={[s.chip, o.value === value && { backgroundColor: color.ink, borderColor: color.ink }]}>
          <Text style={{ color: o.value === value ? '#fff' : color.ink, fontFamily: fonts.bodySemi }}>{o.label}</Text>
        </Pressable>
      ))}
    </View>
  );
}

export const ErrorBox = ({ message }: { message: string }) => (
  <View style={[s.card, { backgroundColor: color.stop + '14', borderColor: color.stop }]}><Text style={{ color: color.stop, fontFamily: fonts.bodySemi }}>{message}</Text></View>
);

const s = StyleSheet.create({
  title: { fontSize: font.title, lineHeight: lineHeight.title, fontFamily: fonts.titleBold, color: color.ink },
  card: { backgroundColor: color.card, borderRadius: radius.card, padding: 16, gap: 10, borderWidth: 1, borderColor: color.line },
  h: { fontSize: font.h, lineHeight: lineHeight.h, fontFamily: fonts.title, color: color.ink },
  sub: { fontSize: font.small, lineHeight: lineHeight.small, fontFamily: fonts.body, color: color.sub },
  light: { flexDirection: 'row', alignItems: 'center', gap: 6, paddingHorizontal: 10, paddingVertical: 5, borderRadius: radius.chip, alignSelf: 'flex-start' },
  dot: { width: 9, height: 9, borderRadius: 5 },
  countdown: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', borderWidth: 1.5, borderRadius: radius.btn, paddingHorizontal: 14, paddingVertical: 10 },
  countdownCaption: { fontSize: font.small, fontFamily: fonts.bodyBold, letterSpacing: 1 },
  countdownDigits: { fontSize: 30, lineHeight: lineHeight.big, fontFamily: fonts.titleBold, fontVariant: ['tabular-nums'] },
  digitBox: { width: 20, textAlign: 'center' },
  btn: { borderRadius: radius.btn, alignItems: 'center', justifyContent: 'center', paddingHorizontal: 18 },
  input: { borderWidth: 1.5, borderColor: color.line, borderRadius: 12, paddingHorizontal: 14, height: 50, fontSize: font.body, fontFamily: fonts.body, color: color.ink, backgroundColor: '#fff' },
  clear: { position: 'absolute', right: 0, top: 0, bottom: 0, width: 48, alignItems: 'center', justifyContent: 'center' },
  chip: { paddingHorizontal: 14, paddingVertical: 10, borderRadius: radius.chip, borderWidth: 1.5, borderColor: color.line, backgroundColor: '#fff' },
});
