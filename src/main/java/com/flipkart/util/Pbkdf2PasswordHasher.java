
package com.flipkart.util;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.Arrays;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import com.flipkart.exception.ValidationException;

public class Pbkdf2PasswordHasher implements PasswordHasher {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int SALT_LENGTH = 16;
    private static final int KEY_LENGTH = 256;
    private static final int MAX_ITERATIONS = 10_000_000;

    private final int iterations;
    private final SecureRandom secureRandom;

    public Pbkdf2PasswordHasher() {
        this(AppConstants.PBKDF2_ITERATIONS);
    }

    public Pbkdf2PasswordHasher(int iterations) {

        if (iterations <= 0 || iterations > MAX_ITERATIONS) {
            throw new IllegalArgumentException("Invalid iterations");
        }

        this.iterations = iterations;
        this.secureRandom = new SecureRandom();
    }

    @Override
    public String hash(String raw) {

        if (raw == null || raw.isBlank()) {
            throw new ValidationException(
                    List.of("Password must not be blank"));
        }

        byte[] salt = new byte[SALT_LENGTH];
        secureRandom.nextBytes(salt);

        try {
            byte[] hash = pbkdf2(raw.toCharArray(), salt, iterations);

            return iterations + ":"
                    + Base64.getEncoder().encodeToString(salt) + ":"
                    + Base64.getEncoder().encodeToString(hash);

        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Hashing failed", e);
        }
    }

    @Override
    public boolean verify(String raw, String stored) {

        if (raw == null || raw.isBlank() ||
                stored == null || stored.isBlank()) {
            return false;
        }

        try {
            String[] parts = stored.split(":", -1);

            if (parts.length != 3) {
                return false;
            }

            int storedIterations = Integer.parseInt(parts[0]);

            if (storedIterations <= 0 ||
                    storedIterations > MAX_ITERATIONS) {
                return false;
            }

            byte[] salt = Base64.getDecoder().decode(parts[1]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[2]);

            if (salt.length != SALT_LENGTH ||
                    expectedHash.length != KEY_LENGTH / 8) {
                return false;
            }

            byte[] actualHash = pbkdf2(
                    raw.toCharArray(), salt, storedIterations);

            return MessageDigest.isEqual(expectedHash, actualHash);

        } catch (GeneralSecurityException | IllegalArgumentException e) {
            return false;
        }
    }

    private byte[] pbkdf2(
            char[] password, byte[] salt, int iterations)
            throws GeneralSecurityException {

        PBEKeySpec spec =
                new PBEKeySpec(password, salt, iterations, KEY_LENGTH);

        try {
            SecretKeyFactory factory =
                    SecretKeyFactory.getInstance(ALGORITHM);

            return factory.generateSecret(spec).getEncoded();

        } finally {
            spec.clearPassword();
            Arrays.fill(password, '\0');
        }
    }

    public int getIterations() {
        return iterations;
    }
}
