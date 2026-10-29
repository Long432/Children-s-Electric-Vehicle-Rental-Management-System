package org.group6.childrenelectricvehiclerentalmanagement.Utility;

import org.group6.childrenelectricvehiclerentalmanagement.Models.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;

import java.text.ParseException;
import java.util.Date;

@Component
public class JwtUtil{

    private final String secret;
    private final long expirationMs;

    public JwtUtil(
            @Value("${jwt.secret:childrenelectricvehiclerentalmanagement}") String secret,
            @Value("${jwt.expiration-ms:604800000}") long expirationMs) {
        this.secret = secret;
        this.expirationMs = expirationMs;
    }

    public String generateToken(User user) {
        try {
            Date now = new Date();
            Date expiry = new Date(now.getTime() + expirationMs);

            JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);
            Payload payload = new Payload(
                    "{\"sub\":\"" + user.getId() +
                            "\",\"username\":\"" + user.getUsername() +
                            "\",\"iat\":" + (now.getTime() / 1000) +
                            ",\"exp\":" + (expiry.getTime() / 1000) + "}"
            );

            JWSObject jws = new JWSObject(header, payload);
            jws.sign(new MACSigner(secret.getBytes()));
            return jws.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException("Failed to generate JWT", e);
        }
    }

    public boolean validateToken(String token) {
        if (token.startsWith("Bearer ")) token = token.substring(7);
        try {
            JWSObject jws = JWSObject.parse(token);
            jws.verify(new MACVerifier(secret.getBytes()));
            return jws.getPayload().toJSONObject().get("exp") != null &&
                    ((Number) jws.getPayload().toJSONObject().get("exp")).longValue() > (System.currentTimeMillis() / 1000);
        } catch (ParseException | JOSEException e) {
            return false;
        }
    }

    public int getUserIdFromToken(String token) throws Exception {
        try {
            JWSObject jws = JWSObject.parse(token);
            return Integer.parseInt(jws.getPayload().toJSONObject().get("sub").toString());
        } catch (ParseException e) {
            throw new Exception("token không hợp lệ");
        }
    }

}
