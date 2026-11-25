import React, { useEffect, useRef } from 'react';
import { ActivityIndicator, View } from 'react-native';
import { NavigationContainer, ParamListBase, useNavigationContainerRef } from '@react-navigation/native';
import { BottomTabBar, createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import { SafeAreaProvider } from 'react-native-safe-area-context';
import { StatusBar } from 'expo-status-bar';
import { Ionicons } from '@expo/vector-icons';
import { useFonts } from 'expo-font';
import { Baloo2_700Bold, Baloo2_800ExtraBold } from '@expo-google-fonts/baloo-2';
import { BeVietnamPro_400Regular, BeVietnamPro_600SemiBold, BeVietnamPro_700Bold } from '@expo-google-fonts/be-vietnam-pro';
import { AuthProvider, useAuth } from './src/auth';
import { ClockProvider } from './src/clock';
import LoginScreen from './src/screens/LoginScreen';
import SessionScreen from './src/screens/SessionScreen';
import TicketsScreen from './src/screens/TicketsScreen';
import VehiclesScreen from './src/screens/VehiclesScreen';
import SettingsScreen from './src/screens/SettingsScreen';
import StatsScreen from './src/screens/StatsScreen';
import { color, fonts } from './src/theme';
import { useWaitingCount } from './src/hooks';
import { initNotifications } from './src/notifications';
import { trackScreen } from './src/analytics';
import { AdBanner, canShowInterstitial, initAds, showInterstitialThen } from './src/ads';

const Tab = createBottomTabNavigator();

type IconName = React.ComponentProps<typeof Ionicons>['name'];

// Mỗi tab có hai bản icon: viền mảnh khi chưa chọn, đặc khi đang chọn.
const tabIcons: Record<string, { off: IconName; on: IconName }> = {
  'Phiên': { off: 'time-outline', on: 'time' },
  'Vé': { off: 'ticket-outline', on: 'ticket' },
  'Xe': { off: 'car-sport-outline', on: 'car-sport' },
  'Thống kê': { off: 'bar-chart-outline', on: 'bar-chart' },
  'Cài đặt': { off: 'settings-outline', on: 'settings' },
};

const TAB_BAR_HEIGHT = 64;
const MAX_BADGE = 99;

function MainTabs() {
  const waiting = useWaitingCount();
  return (
    <Tab.Navigator
      screenListeners={({ navigation, route }) => ({
        tabPress: (e) => {
          // Bấm sang tab khác: hiện quảng cáo toàn màn hình, tắt xong mới chuyển.
          if (navigation.isFocused() || !canShowInterstitial()) return;
          e.preventDefault();
          showInterstitialThen(() => navigation.navigate(route.name));
        },
      })}
      tabBar={(p) => <View><AdBanner /><BottomTabBar {...p} /></View>}
      screenOptions={({ route }) => ({
        headerShown: false,
        tabBarActiveTintColor: color.action,
        tabBarInactiveTintColor: color.sub,
        tabBarLabelStyle: { fontSize: 14, fontFamily: fonts.bodyBold },
        tabBarStyle: { height: TAB_BAR_HEIGHT, paddingTop: 6 },
        tabBarIcon: ({ focused, color: tint, size }) => {
          const icon = tabIcons[route.name];
          return <Ionicons name={focused ? icon.on : icon.off} size={size} color={tint} />;
        },
      })}>
      <Tab.Screen name="Phiên" component={SessionScreen} />
      <Tab.Screen
        name="Vé"
        component={TicketsScreen}
        options={{
          tabBarBadge: waiting > 0 ? (waiting > MAX_BADGE ? `${MAX_BADGE}+` : waiting) : undefined,
          tabBarBadgeStyle: { backgroundColor: color.wait, color: color.ink, fontFamily: fonts.bodyBold, fontSize: 12 },
          tabBarAccessibilityLabel: waiting > 0 ? `Vé, ${waiting} vé đang chờ xe` : 'Vé',
        }}
      />
      <Tab.Screen name="Xe" component={VehiclesScreen} />
      <Tab.Screen name="Thống kê" component={StatsScreen} />
      <Tab.Screen name="Cài đặt" component={SettingsScreen} />
    </Tab.Navigator>
  );
}

function Root() {
  const { state } = useAuth();
  const navRef = useNavigationContainerRef<ParamListBase>();
  const lastScreen = useRef<string | undefined>(undefined);
  const onNavChange = () => {
    const name = navRef.getCurrentRoute()?.name;
    if (name && name !== lastScreen.current) { lastScreen.current = name; trackScreen(name); }
  };
  useEffect(() => { initAds(); initNotifications(); }, []);
  if (state === 'loading') return <View style={{ flex: 1, justifyContent: 'center' }}><ActivityIndicator /></View>;
  if (state === 'out') return <LoginScreen />;
  return (
    <NavigationContainer ref={navRef} onReady={onNavChange} onStateChange={onNavChange}>
      <MainTabs />
    </NavigationContainer>
  );
}

export default function App() {
  const [fontsLoaded, fontError] = useFonts({
    Baloo2_700Bold,
    Baloo2_800ExtraBold,
    BeVietnamPro_400Regular,
    BeVietnamPro_600SemiBold,
    BeVietnamPro_700Bold,
  });
  // Chờ nạp phông xong mới hiện giao diện để chữ không đổi kiểu giữa chừng. Nếu nạp lỗi thì vẫn chạy bằng phông hệ thống.
  if (!fontsLoaded && !fontError) {
    return <View style={{ flex: 1, justifyContent: 'center', backgroundColor: color.bg }}><ActivityIndicator /></View>;
  }
  return <SafeAreaProvider><StatusBar style="dark" /><AuthProvider><ClockProvider><Root /></ClockProvider></AuthProvider></SafeAreaProvider>;
}
