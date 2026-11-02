package org.group6.childrenelectricvehiclerentalmanagement.Services;

import lombok.RequiredArgsConstructor;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.AuthController.LoginRequest;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.AuthController.LoginResponse;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.AuthController.LoginResult;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.AuthController.MeResponse;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.UserStatus;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.AbstractException;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.UserNotExitsException;
import org.group6.childrenelectricvehiclerentalmanagement.Models.User;
import org.group6.childrenelectricvehiclerentalmanagement.Repositories.UserRepository;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.Config.CookieEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final boolean SESSION_REPLACE_EXISTING = true;
    private static final int COOKIE_BYTE_LENGTH = 32;
    private static final int COOKIE_MAX_AGE_DAYS = 30;
    private static final String LOGIN_FAILED_CODE = "INVALID_CREDENTIALS";
    private static final String LOGIN_FAILED_MESSAGE = "Thông tin đăng nhập không đúng";

    public User GetUser(long id) throws Exception {
        return userRepository.findById(id).orElseThrow(() -> new UserNotExitsException(id));
    }

    public User GetUser(int id) throws Exception {
        return this.GetUser(Long.parseLong(String.valueOf(id)));
    }

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CookieEncoder cookieEncoder;
    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public LoginResult login(LoginRequest request) throws Exception {
        String username = request.username() == null ? null : request.username().trim();
        String password = request.password();

        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            throw new AbstractException(LOGIN_FAILED_CODE, LOGIN_FAILED_MESSAGE, 401);
        }

        User user = userRepository.findByUsername(username).orElse(null);

        if (user == null) {
            dummyPasswordMatch(password);
            throw new AbstractException(LOGIN_FAILED_CODE, LOGIN_FAILED_MESSAGE, 401);
        }

        boolean passwordMatches;
        try {
            passwordMatches = passwordEncoder.matches(password, user.getPasswordHash());
        } catch (Exception e) {
            passwordMatches = false;
        }
        if (!passwordMatches) {
            throw new AbstractException(LOGIN_FAILED_CODE, LOGIN_FAILED_MESSAGE, 401);
        }

        if (user.getStatus() == UserStatus.Deactivate) {
            throw new AbstractException("ACCOUNT_DEACTIVATED", "Tài khoản đã bị vô hiệu hóa", 401);
        }

        applyLoginPolicy(user);

        String rawCookie = generateCookie();
        String cookieHash;
        try {
            cookieHash = cookieEncoder.encode(rawCookie);
        } catch (NoSuchAlgorithmException e) {
            throw new AbstractException("COOKEN_ERROR", "Không thể tạo phiên đăng nhập", 500);
        }

        LocalDateTime expiresAt = LocalDateTime.now().plusDays(COOKIE_MAX_AGE_DAYS);

        user.setCookieHash(cookieHash);
        user.setCookieExpiresAt(expiresAt);
        user.setLastLoginAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setStatus(UserStatus.Online);
        userRepository.save(user);

        LoginResponse response = new LoginResponse(user.getId(), user.getUsername(), user.getStatus());
        int maxAgeSeconds = (int) Duration.between(LocalDateTime.now(), expiresAt).getSeconds();
        return new LoginResult(response, rawCookie, maxAgeSeconds);
    }

    @Transactional(readOnly = true)
    public MeResponse getMe(User actor) throws Exception {
        return new MeResponse(actor.getId(), actor.getUsername(), actor.getStatus());
    }

    @Transactional
    public void logout(User actor) throws Exception {
        actor.setCookieHash(null);
        actor.setCookieExpiresAt(null);
        actor.setStatus(UserStatus.Offline);
        actor.setUpdatedAt(LocalDateTime.now());
        userRepository.save(actor);
    }

    private void applyLoginPolicy(User user) throws Exception {
        if (SESSION_REPLACE_EXISTING) {
            return;
        }
    }

    private void dummyPasswordMatch(String password) {
        try {
            String dummyHash = "$2a$10$abcdefghijklmnopqrstuvwxyz0123456789ABCDEFGHI";
            passwordEncoder.matches(password, dummyHash);
        } catch (Exception ignored) {}
    }

    private String generateCookie() {
        byte[] random = new byte[COOKIE_BYTE_LENGTH];
        secureRandom.nextBytes(random);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(random);
    }
}
