package org.group6.childrenelectricvehiclerentalmanagement.Services;

import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Auth.LoginRequest;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Auth.LoginResponse;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.UserStatus;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.InvalidCredentialsException;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.UnauthorizedException;
import org.group6.childrenelectricvehiclerentalmanagement.Models.User;
import org.group6.childrenelectricvehiclerentalmanagement.Models.UserToken;
import org.group6.childrenelectricvehiclerentalmanagement.Repositories.UserRepository;
import org.group6.childrenelectricvehiclerentalmanagement.Repositories.UserTokenRepository;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.JwtUtil;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.Security.AuthenticatedUser;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.TimeUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    private static final String SECRET = "0123456789abcdef0123456789abcdef0123456789abcdef";
    private static final String PASSWORD = "MatKhau12345";

    private final Map<String, UserToken> storedTokens = new HashMap<>();
    private User user;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        PasswordEncoder encoder = new BCryptPasswordEncoder(4);
        LocalDateTime now = TimeUtil.nowUtc();
        user = new User();
        user.setId(1L);
        user.setUsername("banhang");
        user.setPasswordHash(encoder.encode(PASSWORD));
        user.setStatus(UserStatus.Offline);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        UserRepository userRepository = mock(UserRepository.class);
        when(userRepository.findByUsername("banhang")).thenReturn(Optional.of(user));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(call -> call.getArgument(0));

        UserTokenRepository tokenRepository = mock(UserTokenRepository.class);
        when(tokenRepository.save(any(UserToken.class))).thenAnswer(call -> {
            UserToken token = call.getArgument(0);
            storedTokens.put(token.getTokenHash(), token);
            return token;
        });
        when(tokenRepository.findByTokenHash(anyString()))
                .thenAnswer(call -> Optional.ofNullable(storedTokens.get(call.<String>getArgument(0))));
        when(tokenRepository.deleteByTokenHash(anyString()))
                .thenAnswer(call -> storedTokens.remove(call.<String>getArgument(0)) == null ? 0 : 1);
        when(tokenRepository.existsByUserIdAndExpiresAtAfter(anyLong(), any(LocalDateTime.class)))
                .thenAnswer(call -> storedTokens.values().stream().anyMatch(token ->
                        token.getUserId().equals(call.<Long>getArgument(0))
                                && token.getExpiresAt().isAfter(call.<LocalDateTime>getArgument(1))));

        authService = new AuthService(userRepository, tokenRepository, encoder, new JwtUtil(SECRET, 3_600_000L));
    }

    private String login() throws Exception {
        LoginResponse response = authService.login(new LoginRequest("banhang", PASSWORD));
        return response.token();
    }

    @Test
    void tokenIsRejectedAfterLogout() throws Exception {
        String token = login();
        AuthenticatedUser actor = authService.authenticate(token);
        assertEquals(1L, actor.id());

        authService.logout(actor);

        assertThrows(UnauthorizedException.class, () -> authService.authenticate(token));
        assertEquals(UserStatus.Offline, user.getStatus());
    }

    @Test
    void secondLoginDoesNotInvalidateFirstDevice() throws Exception {
        String phone = login();
        String web = login();

        assertEquals(1L, authService.authenticate(phone).id());
        assertEquals(1L, authService.authenticate(web).id());

        authService.logout(authService.authenticate(phone));

        assertThrows(UnauthorizedException.class, () -> authService.authenticate(phone));
        assertEquals(1L, authService.authenticate(web).id());
        assertEquals(UserStatus.Online, user.getStatus());
    }

    @Test
    void tokenIsRejectedWhenAccountIsDeactivated() throws Exception {
        String token = login();
        user.setStatus(UserStatus.Deactivate);
        assertThrows(UnauthorizedException.class, () -> authService.authenticate(token));
    }

    @Test
    void tokenIsRejectedWhenStoredRecordExpired() throws Exception {
        String token = login();
        storedTokens.values().forEach(stored -> stored.setExpiresAt(TimeUtil.nowUtc().minusSeconds(1)));
        assertThrows(UnauthorizedException.class, () -> authService.authenticate(token));
    }

    @Test
    void wrongPasswordIsRejected() {
        assertThrows(InvalidCredentialsException.class,
                () -> authService.login(new LoginRequest("banhang", "sai-mat-khau")));
        assertTrue(storedTokens.isEmpty());
    }
}
