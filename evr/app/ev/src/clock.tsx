import React, { createContext, useContext, useEffect, useState } from 'react';
import { AppState } from 'react-native';
import { serverNow } from './time';

const ClockContext = createContext<number>(0);

export const useNow = () => useContext(ClockContext);

const untilNextSecond = () => 1000 - (serverNow() % 1000);

export function ClockProvider({ children }: { children: React.ReactNode }) {
  const [now, setNow] = useState(serverNow);

  useEffect(() => {
    let timer: ReturnType<typeof setTimeout>;
    const tick = () => {
      setNow(serverNow());
      timer = setTimeout(tick, untilNextSecond());
    };
    timer = setTimeout(tick, untilNextSecond());
    const subscription = AppState.addEventListener('change', (state) => {
      if (state === 'active') setNow(serverNow());
    });
    return () => {
      clearTimeout(timer);
      subscription.remove();
    };
  }, []);

  return <ClockContext.Provider value={now}>{children}</ClockContext.Provider>;
}
