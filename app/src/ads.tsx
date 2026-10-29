import React from 'react';
import { Platform, StyleSheet, Text, View } from 'react-native';
import { useNavigation } from '@react-navigation/native';
import { ADS_ENABLED, AD_UNITS, INTERSTITIAL_COOLDOWN_MS } from './config';
import { color } from './theme';

// Nạp thư viện an toàn: trong Expo Go không có module native, app vẫn chạy và chỉ hiện khung giữ chỗ.
let Ads: any = null;
try { Ads = require('react-native-google-mobile-ads'); } catch { Ads = null; }

const pick = (u: { android: string; ios: string }) => (Platform.OS === 'ios' ? u.ios : u.android);
const available = ADS_ENABLED && !!Ads;

let interstitial: any = null;
let loaded = false;
let lastShown = 0;
let pendingNext: (() => void) | null = null;

function finish() { const n = pendingNext; pendingNext = null; n?.(); }

function loadInterstitial() {
  const { InterstitialAd, AdEventType } = Ads;
  loaded = false;
  interstitial = InterstitialAd.createForAdRequest(pick(AD_UNITS.interstitial), { requestNonPersonalizedAdsOnly: true });
  interstitial.addAdEventListener(AdEventType.LOADED, () => { loaded = true; });
  interstitial.addAdEventListener(AdEventType.CLOSED, () => { loadInterstitial(); finish(); }); // người dùng tắt quảng cáo -> nạp cái kế tiếp rồi mới chuyển trang
  interstitial.addAdEventListener(AdEventType.ERROR, () => { loaded = false; finish(); });
  interstitial.load();
}

/** Gọi một lần khi app khởi động. */
export async function initAds() {
  if (!available) return;
  try {
    const mobileAds = Ads.default();
    await mobileAds.setRequestConfiguration({ maxAdContentRating: Ads.MaxAdContentRating.G });
    await mobileAds.initialize();
    loadInterstitial();
  } catch (e) { console.warn('AdMob init lỗi', e); }
}

/** Có đang được phép hiện quảng cáo toàn màn hình không (đã nạp xong và đã qua 3 phút kể từ lần trước). */
export function canShowInterstitial() {
  return available && loaded && !!interstitial && Date.now() - lastShown >= INTERSTITIAL_COOLDOWN_MS;
}

/**
 * Hiện quảng cáo toàn màn hình (nếu được phép), và chỉ gọi `next` (chuyển trang) SAU KHI người dùng tắt quảng cáo.
 * Nếu chưa được phép hiện thì gọi `next` ngay.
 */
export function showInterstitialThen(next: () => void) {
  if (!canShowInterstitial()) return next();
  try {
    lastShown = Date.now();
    pendingNext = next;
    interstitial.show();
  } catch { finish(); }
}

/** Hook: trả về hàm chuyển màn hình có kèm quảng cáo, dùng cho các nút bấm. */
export function useNavigateWithAd() {
  const nav = useNavigation<any>();
  return (screen: string) => showInterstitialThen(() => nav.navigate(screen));
}

/** Banner thích ứng theo chiều rộng màn hình. */
export function AdBanner() {
  if (!ADS_ENABLED) return null;
  if (!available) {
    return __DEV__ ? (
      <View style={s.placeholder}><Text style={s.ph}>Khung quảng cáo (chỉ hiện trên bản build có AdMob, không hiện trong Expo Go)</Text></View>
    ) : null;
  }
  const { BannerAd, BannerAdSize } = Ads;
  return (
    <View style={{ alignItems: 'center', backgroundColor: color.card }}>
      <BannerAd unitId={pick(AD_UNITS.banner)} size={BannerAdSize.ANCHORED_ADAPTIVE_BANNER} requestOptions={{ requestNonPersonalizedAdsOnly: true }} />
    </View>
  );
}

const s = StyleSheet.create({
  placeholder: { height: 50, justifyContent: 'center', alignItems: 'center', backgroundColor: color.line },
  ph: { color: color.sub, fontSize: 12 },
});
