import React, { useEffect } from 'react';
import { ActivityIndicator, View } from 'react-native';
import { NavigationContainer } from '@react-navigation/native';
import { BottomTabBar, createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import { SafeAreaProvider } from 'react-native-safe-area-context';
import { StatusBar } from 'expo-status-bar';
import { AuthProvider, useAuth } from './src/auth';
import LoginScreen from './src/screens/LoginScreen';
import SessionScreen from './src/screens/SessionScreen';
import TicketsScreen from './src/screens/TicketsScreen';
import VehiclesScreen from './src/screens/VehiclesScreen';
import SettingsScreen from './src/screens/SettingsScreen';
import { color } from './src/theme';
import { AdBanner, canShowInterstitial, initAds, showInterstitialThen } from './src/ads';

const Tab = createBottomTabNavigator();

function Root() {
  const { state } = useAuth();
  useEffect(() => { initAds(); }, []);
  if (state === 'loading') return <View style={{ flex: 1, justifyContent: 'center' }}><ActivityIndicator /></View>;
  if (state === 'out') return <LoginScreen />;
  return (
    <NavigationContainer>
      <Tab.Navigator
        screenListeners={({ navigation, route }) => ({
          tabPress: (e) => {
            // Bấm sang tab khác: hiện quảng cáo toàn màn hình, tắt xong mới chuyển.
            if (navigation.isFocused() || !canShowInterstitial()) return;
            e.preventDefault();
            showInterstitialThen(() => navigation.navigate(route.name));
          },
        })}
        tabBar={(p) => <View><AdBanner /><BottomTabBar {...p} /></View>} screenOptions={{ headerShown: false, tabBarActiveTintColor: color.action, tabBarInactiveTintColor: color.sub,
        tabBarLabelStyle: { fontSize: 14, fontWeight: '700' }, tabBarIconStyle: { display: 'none' }, tabBarStyle: { height: 58, paddingTop: 6 } }}>
        <Tab.Screen name="Phiên" component={SessionScreen} />
        <Tab.Screen name="Vé" component={TicketsScreen} />
        <Tab.Screen name="Xe" component={VehiclesScreen} />
        <Tab.Screen name="Cài đặt" component={SettingsScreen} />
      </Tab.Navigator>
    </NavigationContainer>
  );
}

export default function App() {
  return <SafeAreaProvider><StatusBar style="dark" /><AuthProvider><Root /></AuthProvider></SafeAreaProvider>;
}
