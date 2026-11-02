package org.group6.childrenelectricvehiclerentalmanagement.Utility.Config;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public class CookieEncoder{
    public CookieEncoder() {
    }

    public String encode(String rawCookie) throws NoSuchAlgorithmException {
        byte[] d = MessageDigest.getInstance("SHA-256").digest(rawCookie.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(d);
    }
}
