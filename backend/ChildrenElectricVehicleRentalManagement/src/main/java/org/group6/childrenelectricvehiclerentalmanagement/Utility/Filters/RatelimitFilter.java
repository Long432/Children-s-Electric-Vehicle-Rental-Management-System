package org.group6.childrenelectricvehiclerentalmanagement.Utility.Filters;

import org.group6.childrenelectricvehiclerentalmanagement.Utility.ApiErrorWriter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class RatelimitFilter extends OncePerRequestFilter {

    private static final int GENERAL_LIMIT = 30;
    private static final long GENERAL_WINDOW_SECONDS = 10;
    private static final int LOGIN_LIMIT = 10;
    private static final long LOGIN_WINDOW_SECONDS = 60;
    private static final long CLEANUP_INTERVAL_SECONDS = 60;
    private static final String LOGIN_PATH = "/api/auth/login";

    private final Map<String, Window> windows = new ConcurrentHashMap<>();
    private final AtomicLong lastCleanup = new AtomicLong(0);

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        long now = Instant.now().getEpochSecond();
        String client = request.getRemoteAddr();
        purgeExpired(now);
        long retryAfter = hit("all:" + client, GENERAL_LIMIT, GENERAL_WINDOW_SECONDS, now);
        if (retryAfter == 0 && isLogin(request)) {
            retryAfter = hit("login:" + client, LOGIN_LIMIT, LOGIN_WINDOW_SECONDS, now);
        }
        if (retryAfter > 0) {
            response.setHeader("Retry-After", String.valueOf(retryAfter));
            ApiErrorWriter.write(response, 429, "RATE_LIMITED", "Bạn thao tác quá nhanh, vui lòng thử lại sau ít giây");
            return;
        }
        filterChain.doFilter(request, response);
    }

    private boolean isLogin(HttpServletRequest request) {
        return "POST".equalsIgnoreCase(request.getMethod()) && LOGIN_PATH.equals(request.getRequestURI());
    }

    private long hit(String key, int limit, long windowSeconds, long now) {
        Window window = windows.computeIfAbsent(key, k -> new Window(now, windowSeconds));
        return window.hit(now, limit);
    }

    private void purgeExpired(long now) {
        long last = lastCleanup.get();
        if (now - last < CLEANUP_INTERVAL_SECONDS || !lastCleanup.compareAndSet(last, now)) return;
        windows.entrySet().removeIf(entry -> entry.getValue().expired(now));
    }

    private static final class Window {

        private final long windowSeconds;
        private long start;
        private int count;

        private Window(long start, long windowSeconds) {
            this.start = start;
            this.windowSeconds = windowSeconds;
        }

        private synchronized long hit(long now, int limit) {
            if (now - start >= windowSeconds) {
                start = now;
                count = 0;
            }
            count++;
            if (count <= limit) return 0;
            return Math.max(1, start + windowSeconds - now);
        }

        private synchronized boolean expired(long now) {
            return now - start >= windowSeconds;
        }
    }
}
