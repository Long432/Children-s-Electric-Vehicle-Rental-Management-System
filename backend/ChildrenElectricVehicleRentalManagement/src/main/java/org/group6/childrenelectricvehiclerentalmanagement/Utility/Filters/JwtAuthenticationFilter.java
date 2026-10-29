package org.group6.childrenelectricvehiclerentalmanagement.Utility.Filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.UserException;
import org.group6.childrenelectricvehiclerentalmanagement.Models.User;
import org.group6.childrenelectricvehiclerentalmanagement.Services.AuthService;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.JwtUtil;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final AuthService authService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (token == null || !token.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                return;
            }

            if(!jwtUtil.validateToken(token)) throw new Exception("token không hợp lệ");

            token = token.substring(7);
            int userId = jwtUtil.getUserIdFromToken(token);
            User user = authService.GetUser(userId);

            Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, List.of());
            SecurityContextHolder.getContext().setAuthentication(authentication);

            filterChain.doFilter(request,response);
        } catch (Exception e) {
            log.error(e.getMessage());

            response.sendError(500,"Lỗi server");
        }
    }
}
