package org.group6.childrenelectricvehiclerentalmanagement.Utility;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.PlainJWT;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtUtilTest {

    private static final String SECRET = "0123456789abcdef0123456789abcdef0123456789abcdef";
    private static final String OTHER_SECRET = "fedcba9876543210fedcba9876543210fedcba9876543210";
    private static final long ONE_HOUR_MS = 3_600_000L;

    @Test
    void validTokenReturnsUserId() {
        JwtUtil jwtUtil = new JwtUtil(SECRET, ONE_HOUR_MS);
        String token = jwtUtil.issue(7L, "banhang").value();
        assertEquals(Optional.of(7L), jwtUtil.parseUserId(token));
    }

    @Test
    void tokenSignedWithAnotherKeyIsRejected() {
        String forged = new JwtUtil(OTHER_SECRET, ONE_HOUR_MS).issue(7L, "banhang").value();
        assertTrue(new JwtUtil(SECRET, ONE_HOUR_MS).parseUserId(forged).isEmpty());
    }

    @Test
    void tokenWithSwappedPayloadIsRejected() {
        JwtUtil jwtUtil = new JwtUtil(SECRET, ONE_HOUR_MS);
        String[] own = jwtUtil.issue(7L, "banhang").value().split("\\.");
        String[] other = jwtUtil.issue(1L, "admin").value().split("\\.");
        String forged = own[0] + "." + other[1] + "." + own[2];
        assertTrue(jwtUtil.parseUserId(forged).isEmpty());
    }

    @Test
    void expiredTokenIsRejected() {
        JwtUtil jwtUtil = new JwtUtil(SECRET, -1_000L);
        String token = jwtUtil.issue(7L, "banhang").value();
        assertTrue(jwtUtil.parseUserId(token).isEmpty());
    }

    @Test
    void unsignedTokenIsRejected() {
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject("7")
                .expirationTime(Date.from(Instant.now().plusSeconds(3600)))
                .build();
        String unsigned = new PlainJWT(claims).serialize();
        assertTrue(new JwtUtil(SECRET, ONE_HOUR_MS).parseUserId(unsigned).isEmpty());
    }

    @Test
    void malformedTokenIsRejected() {
        JwtUtil jwtUtil = new JwtUtil(SECRET, ONE_HOUR_MS);
        assertTrue(jwtUtil.parseUserId("khong-phai-token").isEmpty());
        assertTrue(jwtUtil.parseUserId("").isEmpty());
    }

    @Test
    void missingOrPlaceholderSecretStopsStartup() {
        assertThrows(IllegalStateException.class, () -> new JwtUtil("", ONE_HOUR_MS));
        assertThrows(IllegalStateException.class, () -> new JwtUtil("DOI_THANH_CHUOI_NGAU_NHIEN_DAI_IT_NHAT_32_KY_TU", ONE_HOUR_MS));
    }
}
