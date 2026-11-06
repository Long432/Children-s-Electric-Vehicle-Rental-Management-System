package org.group6.childrenelectricvehiclerentalmanagement.Services;

import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Auth.LoginRequest;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Auth.LoginResponse;
import org.group6.childrenelectricvehiclerentalmanagement.DTOs.Auth.UserResponse;
import org.group6.childrenelectricvehiclerentalmanagement.Enum.UserStatus;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.AbstractException;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.AccountDeactivatedException;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.InvalidCredentialsException;
import org.group6.childrenelectricvehiclerentalmanagement.Exceptions.UnauthorizedException;
import org.group6.childrenelectricvehiclerentalmanagement.Models.User;
import org.group6.childrenelectricvehiclerentalmanagement.Models.UserToken;
import org.group6.childrenelectricvehiclerentalmanagement.Repositories.UserRepository;
import org.group6.childrenelectricvehiclerentalmanagement.Repositories.UserTokenRepository;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.HashUtil;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.JwtUtil;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.Security.AuthenticatedUser;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.TimeUtil;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.Validators.LoginRequestValidator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.Optional;

@Service
public class AuthService {

    private static final LoginRequestValidator LOGIN_VALIDATOR = new LoginRequestValidator();

    private final UserRepository userRepository;
    private final UserTokenRepository userTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final String unknownUserHash;

    public AuthService(UserRepository userRepository, UserTokenRepository userTokenRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.userTokenRepository = userTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.unknownUserHash = passwordEncoder.encode("khong-ton-tai");
    }

    @Transactional(rollbackFor = Exception.class)
    public LoginResponse login(LoginRequest request) throws AbstractException {
        LOGIN_VALIDATOR.validate(request);
        Optional<User> found = userRepository.findByUsername(request.username().trim());
        if (found.isEmpty()) {
            passwordEncoder.matches(request.password(), unknownUserHash);
            throw new InvalidCredentialsException();
        }
        User user = found.get();
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }
        if (user.getStatus() == UserStatus.Deactivate) {
            throw new AccountDeactivatedException();
        }
        JwtUtil.IssuedToken token = jwtUtil.issue(user.getId(), user.getUsername());
        LocalDateTime now = TimeUtil.nowUtc();
        LocalDateTime expiresAt = LocalDateTime.ofInstant(token.expiresAt(), ZoneOffset.UTC);
        userTokenRepository.deleteExpiredByUserId(user.getId(), now);
        userTokenRepository.save(new UserToken(user.getId(), HashUtil.sha256Hex(token.value()), expiresAt, now));
        user.setStatus(UserStatus.Online);
        user.setCookieHash(null);
        user.setCookieExpiresAt(null);
        user.setLastLoginAt(now);
        user.setUpdatedAt(now);
        userRepository.save(user);
        return new LoginResponse(token.value(), TimeUtil.toIsoUtc(expiresAt), UserResponse.from(user));
    }

    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public AuthenticatedUser authenticate(String token) throws UnauthorizedException {
        long userId = jwtUtil.parseUserId(token).orElseThrow(UnauthorizedException::new);
        String tokenHash = HashUtil.sha256Hex(token);
        UserToken stored = userTokenRepository.findByTokenHash(tokenHash).orElseThrow(UnauthorizedException::new);
        if (!Objects.equals(stored.getUserId(), userId) || !stored.getExpiresAt().isAfter(TimeUtil.nowUtc())) {
            throw new UnauthorizedException();
        }
        User user = userRepository.findById(userId).orElseThrow(UnauthorizedException::new);
        if (user.getStatus() == UserStatus.Deactivate) {
            throw new UnauthorizedException();
        }
        return new AuthenticatedUser(user.getId(), user.getUsername(), tokenHash);
    }

    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public UserResponse getProfile(AuthenticatedUser actor) throws UnauthorizedException {
        User user = userRepository.findById(actor.id()).orElseThrow(UnauthorizedException::new);
        return UserResponse.from(user);
    }

    @Transactional(rollbackFor = Exception.class)
    public void logout(AuthenticatedUser actor) throws UnauthorizedException {
        if (actor.tokenHash() != null) {
            userTokenRepository.deleteByTokenHash(actor.tokenHash());
        }
        User user = userRepository.findById(actor.id()).orElseThrow(UnauthorizedException::new);
        LocalDateTime now = TimeUtil.nowUtc();
        if (user.getStatus() == UserStatus.Online && !userTokenRepository.existsByUserIdAndExpiresAtAfter(user.getId(), now)) {
            user.setStatus(UserStatus.Offline);
            user.setUpdatedAt(now);
            userRepository.save(user);
        }
    }
}
