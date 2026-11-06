package org.group6.childrenelectricvehiclerentalmanagement.Utility;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Component
public class JwtUtil {

    public record IssuedToken(String value, Instant expiresAt) {
    }

    private final MACSigner signer;
    private final MACVerifier verifier;
    private final long expirationMs;

    public JwtUtil(
            @Value("${jwt.secret:}") String secret,
            @Value("${jwt.expiration-ms:86400000}") long expirationMs) {
        if (secret == null || secret.isBlank() || secret.startsWith("DOI_")) {
            throw new IllegalStateException("Chưa cấu hình jwt.secret: chạy setup-local.ps1 (Windows) hoặc setup-local.sh (macOS, Linux), hoặc đặt biến môi trường JWT_SECRET");
        }
        byte[] key = secret.getBytes(StandardCharsets.UTF_8);
        try {
            this.signer = new MACSigner(key);
            this.verifier = new MACVerifier(key);
        } catch (JOSEException e) {
            throw new IllegalStateException("Khóa JWT không hợp lệ, cần tối thiểu 32 ký tự", e);
        }
        this.expirationMs = expirationMs;
    }

    public IssuedToken issue(long userId, String username) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusMillis(expirationMs);
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .jwtID(UUID.randomUUID().toString())
                .issueTime(Date.from(now))
                .expirationTime(Date.from(expiresAt))
                .build();
        SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
        try {
            jwt.sign(signer);
        } catch (JOSEException e) {
            throw new IllegalStateException("Không thể tạo JWT", e);
        }
        return new IssuedToken(jwt.serialize(), expiresAt);
    }

    public Optional<Long> parseUserId(String token) {
        try {
            SignedJWT jwt = SignedJWT.parse(token);
            if (!JWSAlgorithm.HS256.equals(jwt.getHeader().getAlgorithm())) return Optional.empty();
            if (!jwt.verify(verifier)) return Optional.empty();
            JWTClaimsSet claims = jwt.getJWTClaimsSet();
            Date expiry = claims.getExpirationTime();
            if (expiry == null || !expiry.after(new Date())) return Optional.empty();
            return Optional.of(Long.parseLong(claims.getSubject()));
        } catch (ParseException | JOSEException | NumberFormatException e) {
            return Optional.empty();
        }
    }
}
