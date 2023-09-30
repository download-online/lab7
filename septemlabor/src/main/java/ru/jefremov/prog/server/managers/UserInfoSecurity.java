package ru.jefremov.prog.server.managers;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.SQLException;

public class UserInfoSecurity {
    private final SecureRandom secureRandom = new SecureRandom();
    private final String symbols = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789<>?:@{!$%^&*()_+£$";
    private final int SALT_LEN = 10;

    public String getSalt() {
        var stringBuilder = new StringBuilder(SALT_LEN);
        for (int i = 0; i < SALT_LEN; i++) {
            stringBuilder.append(symbols.charAt(secureRandom.nextInt(symbols.length())));
        }
        return stringBuilder.toString();
    }

    public String getProtected(String target, String salt) {
        if (target==null || salt==null) throw new IllegalArgumentException("Target and salt should not be null");
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-512");
            byte[] saltBytes = salt.getBytes(StandardCharsets.UTF_8);
            md.update(saltBytes);
            byte[] hashedTarget = md.digest(target.getBytes(StandardCharsets.UTF_8));
            final StringBuilder sb = new StringBuilder();
            for (byte b : hashedTarget) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException ignored) {
        }
        return target;
    }
}
