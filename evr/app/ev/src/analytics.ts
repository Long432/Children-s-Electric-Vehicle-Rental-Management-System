type Params = Record<string, string | number | boolean>;

let analytics: any = null;
let crashlytics: any = null;
try { analytics = require('@react-native-firebase/analytics').default; } catch { analytics = null; }
try { crashlytics = require('@react-native-firebase/crashlytics').default; } catch { crashlytics = null; }

export function track(name: string, params?: Params) {
  try { analytics?.().logEvent(name, params); } catch {}
}

export function trackScreen(name: string) {
  try { analytics?.().logScreenView({ screen_name: name, screen_class: name }); } catch {}
}

export function setUser(id: string | null) {
  try {
    analytics?.().setUserId(id);
    crashlytics?.().setUserId(id ?? '');
  } catch {}
}

export function reportError(error: unknown, where: string) {
  try {
    const c = crashlytics?.();
    if (!c) return;
    c.log(where);
    c.recordError(error instanceof Error ? error : new Error(String(error)));
  } catch {}
}

export function crashTest() {
  try { crashlytics?.().crash(); } catch {}
}

export const firebaseReady = !!analytics && !!crashlytics;
