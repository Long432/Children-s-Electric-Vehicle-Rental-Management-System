package org.group6.childrenelectricvehiclerentalmanagement.Utility.Filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;


@Component
@Slf4j
public class RatelimitFilter extends OncePerRequestFilter {
    Map<String,bucket> bucketList = new HashMap<>();

    private final static int MAXIMUM_REQUEST = 1; // đơn vị lần
    private final static long WINDOW_SECOND= 5; // đơn vị giây

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        String clientIp = getClientIp(request);
        long now = Instant.now().getEpochSecond();


        bucketList.computeIfAbsent(clientIp,k -> new bucket(now));

        bucket target = bucketList.get(clientIp);

        if (target.checkLimit(now)){
            response.setStatus(429);
            response.addHeader("Retry-After",String.valueOf(target.getTimeLeft(now)));
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            // X-Forwarded-For: client, proxy1, proxy2
            int comma = xff.indexOf(',');
            return (comma >= 0 ? xff.substring(0, comma) : xff).trim();
        }
        String remoteAddr = request.getRemoteAddr();
        return remoteAddr != null ? remoteAddr : "unknown";
    }

    private static class bucket{
        private long startFrom;
        private AtomicInteger counter;

        public bucket(long startFrom) {
            this.startFrom = startFrom;
            this.counter = new AtomicInteger(0);
        }

        public boolean checkLimit(long currentTime){
            if (currentTime>=this.startFrom+WINDOW_SECOND) {
                this.startFrom = currentTime;
                this.counter = new AtomicInteger(0);
            }

            return this.counter.incrementAndGet() > MAXIMUM_REQUEST;
        }

        public long getTimeLeft(long currentTime){
            return this.startFrom+WINDOW_SECOND-currentTime;
        }
    }
}
