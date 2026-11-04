package org.group6.childrenelectricvehiclerentalmanagement.Utility.Filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.CookieNotValid;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.UserException;
import org.group6.childrenelectricvehiclerentalmanagement.Models.User;
import org.group6.childrenelectricvehiclerentalmanagement.Services.AuthService;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.JwtUtil;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.Config.CookieEncoder;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.rememberme.InvalidCookieException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
@Slf4j

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final AuthService authService;
    private final CookieEncoder cookieEncoder;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, AuthService authService, CookieEncoder cookieEncoder) {
        this.jwtUtil = jwtUtil;
        this.authService = authService;
        this.cookieEncoder = cookieEncoder;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = request.getHeader(HttpHeaders.AUTHORIZATION);

            if (token == null || !token.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                return;
            }

            if(!jwtUtil.validateToken(token)) throw new CookieNotValid("can't not validate bearer token","Token không hợp lệ, liên hệ quản trị viên");

            token = token.substring(7);
            int userId = jwtUtil.getUserIdFromToken(token);
            User user = authService.GetUser(userId);

            if (!Objects.equals(user.getCookieHash(), cookieEncoder.encode(token))) throw new CookieNotValid("token is diffirent with token inside db","Token không hợp lệ, vui lòng đăng nhập lại");

//            // Validate cookie: encode the cookie from request and compare with stored hash
//            // If no cookie found, proceed normally (remember-me may not be set)
//            Optional<Cookie> cookieOpt = java.util.Arrays.asList(request.getCookies()).stream()
//                    .filter(cookie -> "remember-me".equals(cookie.getName()))
//                    .findFirst();
//
//            if (cookieOpt.isPresent()) {
//                String rawCookieValue = cookieOpt.get().getValue();
//                String encodedCookie;
//                try {
//                    encodedCookie = cookieEncoder.encode(rawCookieValue);
//                } catch (Exception e) {
//                    log.error("Lỗi khi mã hóa cookie: {}", e.getMessage());
//                    throw new CookieNotValid("Lỗi server khi mã hóa cookie", "Lỗi server khi xác thực cookie");
//                }
//                if (!encodedCookie.equals(user.getCookieHash())) {
//                    throw new CookieNotValid("Cookie không hợp lệ", "Cookie không hợp lệ hoặc đã hết hạn");
//                }
//            }
//            // If no cookie found, proceed normally - remember-me may not be set for this request

            Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, List.of());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request,response);
        } catch (CookieNotValid e) {
            log.warn(e.getMessage());
            response.setStatus(e.getStatusCode().value());
            response.getWriter().write(e.getUserMessage());
        } catch (Exception e) {
            log.error(e.getMessage());
            response.sendError(500,"Lỗi server");
        }
    }
}
