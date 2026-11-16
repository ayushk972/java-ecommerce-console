package com.flipkart.util;

import com.flipkart.exception.ValidationException;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;

public class Pbkdf2PasswordHasher implements PasswordHasher {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int SALT_LENGTH = 16;
    private static final int KEY_LENGTH = 256;

    private final int iterations;
    private final SecureRandom secureRandom;

    public Pbkdf2PasswordHasher() {
        this(AppConstants.PBKDF2_ITERATIONS);
    }

    public Pbkdf2PasswordHasher(int iterations) {
        if (iterations <= 0) {
            throw new IllegalArgumentException(
                    "iterations must be greater than zero");
        }

        this.iterations = iterations;
        this.secureRandom = new SecureRandom();
    }

    @Override
    public String hash(String raw) {

        validatePassword(raw);

        try {
            byte[] salt = new byte[SALT_LENGTH];
            secureRandom.nextBytes(salt);

            byte[] hash = pbkdf2(
                    raw.toCharArray(),
                    salt,
                    iterations);

            String saltBase64 =
                    Base64.getEncoder().encodeToString(salt);

            String hashBase64 =
                    Base64.getEncoder().encodeToString(hash);

            return iterations
                    + ":"
                    + saltBase64
                    + ":"
                    + hashBase64;

        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(
                    "Unable to hash password",
                    e);
        }
    }

    @Override
    public boolean verify(String raw, String stored) {

        if (raw == null || stored == null || stored.isBlank()) {
            return false;
        }

        try {

            String[] parts = stored.split(":");

            if (parts.length != 3) {
                return false;
            }

            int storedIterations =
                    Integer.parseInt(parts[0]);

            byte[] salt =
                    Base64.getDecoder().decode(parts[1]);

            byte[] expectedHash =
                    Base64.getDecoder().decode(parts[2]);

            byte[] actualHash =
                    pbkdf2(
                            raw.toCharArray(),
                            salt,
                            storedIterations);

            return MessageDigest.isEqual(
                    expectedHash,
                    actualHash);

        } catch (Exception e) {
            return false;
        }
    }

    private void validatePassword(String raw) {

        if (raw == null || raw.isBlank()) {
            throw new ValidationException(
                    List.of("Password must not be blank"));
        }
    }

    private byte[] pbkdf2(
            char[] password,
            byte[] salt,
            int iterations)
            throws GeneralSecurityException {

        PBEKeySpec spec =
                new PBEKeySpec(
                        password,
                        salt,
                        iterations,
                        KEY_LENGTH);

        SecretKeyFactory factory =
                SecretKeyFactory.getInstance(ALGORITHM);

        return factory
                .generateSecret(spec)
                .getEncoded();
    }

    public int getIterations() {
        return iterations;
    }
}