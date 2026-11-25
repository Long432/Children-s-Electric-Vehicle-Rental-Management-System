package org.group6.childrenelectricvehiclerentalmanagement.Utility.Filters;

import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.UnauthorizedException;
import org.group6.childrenelectricvehiclerentalmanagement.Services.AuthService;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.Security.AuthenticatedUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtAuthenticationFilterTest {

    private final AuthService authService = mock(AuthService.class);
    private final JwtAuthenticationFilter filter = new JwtAuthenticationFilter(authService);

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private MockFilterChain run(String authorization) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/vehicles");
        if (authorization != null) {
            request.addHeader("Authorization", authorization);
        }
        MockFilterChain chain = new MockFilterChain();
        filter.doFilter(request, new MockHttpServletResponse(), chain);
        return chain;
    }

    @Test
    void rejectedTokenLeavesRequestUnauthenticated() throws Exception {
        when(authService.authenticate("token-sai")).thenThrow(new UnauthorizedException());
        MockFilterChain chain = run("Bearer token-sai");
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertNotNull(chain.getRequest());
    }

    @Test
    void missingHeaderLeavesRequestUnauthenticated() throws Exception {
        MockFilterChain chain = run(null);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertNotNull(chain.getRequest());
    }

    @Test
    void acceptedTokenAuthenticatesRequest() throws Exception {
        AuthenticatedUser actor = new AuthenticatedUser(1L, "banhang", "hash");
        when(authService.authenticate("token-dung")).thenReturn(actor);
        run("Bearer token-dung");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertEquals(actor, authentication.getPrincipal());
    }
}
