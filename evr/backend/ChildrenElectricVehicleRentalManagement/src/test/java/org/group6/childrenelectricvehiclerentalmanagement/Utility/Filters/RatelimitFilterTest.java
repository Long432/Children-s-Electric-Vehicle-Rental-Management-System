package org.group6.childrenelectricvehiclerentalmanagement.Utility.Filters;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RatelimitFilterTest {

    private final RatelimitFilter filter = new RatelimitFilter();

    private MockHttpServletResponse call(String method, String uri, String remoteAddress, String forwardedFor) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest(method, uri);
        request.setRemoteAddr(remoteAddress);
        if (forwardedFor != null) {
            request.addHeader("X-Forwarded-For", forwardedFor);
        }
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, new MockFilterChain());
        return response;
    }

    @Test
    void loginIsBlockedAfterTenAttemptsInAMinute() throws Exception {
        for (int i = 0; i < 10; i++) {
            assertEquals(200, call("POST", "/api/auth/login", "10.0.0.1", null).getStatus());
        }
        MockHttpServletResponse blocked = call("POST", "/api/auth/login", "10.0.0.1", null);
        assertEquals(429, blocked.getStatus());
        assertNotNull(blocked.getHeader("Retry-After"));
        assertTrue(Long.parseLong(blocked.getHeader("Retry-After")) > 0);
        assertTrue(blocked.getContentAsString(StandardCharsets.UTF_8).contains("RATE_LIMITED"));
    }

    @Test
    void forwardedForHeaderDoesNotBypassTheLimit() throws Exception {
        for (int i = 0; i < 10; i++) {
            assertEquals(200, call("POST", "/api/auth/login", "10.0.0.2", "203.0.113." + i).getStatus());
        }
        assertEquals(429, call("POST", "/api/auth/login", "10.0.0.2", "203.0.113.99").getStatus());
    }

    @Test
    void generalRequestsAreBlockedAfterThirtyInTenSeconds() throws Exception {
        for (int i = 0; i < 30; i++) {
            assertEquals(200, call("GET", "/api/vehicles", "10.0.0.3", null).getStatus());
        }
        assertEquals(429, call("GET", "/api/vehicles", "10.0.0.3", null).getStatus());
    }

    @Test
    void limitIsTrackedPerAddress() throws Exception {
        for (int i = 0; i < 11; i++) {
            call("POST", "/api/auth/login", "10.0.0.4", null);
        }
        assertEquals(429, call("POST", "/api/auth/login", "10.0.0.4", null).getStatus());
        assertEquals(200, call("POST", "/api/auth/login", "10.0.0.5", null).getStatus());
    }
}
