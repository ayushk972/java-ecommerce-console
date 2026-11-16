package com.flipkart.util;

import com.flipkart.exception.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Pbkdf2PasswordHasherTest {

    private static final String PASSWORD = "Password@123";

    @Test
    @DisplayName("AC2: Hashing the same password twice produces different hashes")
    void hash_samePasswordTwice_producesDifferentHashes() {

        Pbkdf2PasswordHasher hasher =
                new Pbkdf2PasswordHasher(1000);

        String hash1 = hasher.hash(PASSWORD);
        String hash2 = hasher.hash(PASSWORD);

        assertNotEquals(hash1, hash2);
    }

    @Test
    @DisplayName("AC1: Generated hash uses expected storage format")
    void hash_validPassword_returnsExpectedFormat() {

        Pbkdf2PasswordHasher hasher =
                new Pbkdf2PasswordHasher(1000);

        String hash = hasher.hash(PASSWORD);

        String[] parts = hash.split(":");

        assertEquals(3, parts.length);
        assertEquals("1000", parts[0]);

        assertFalse(parts[1].isBlank());
        assertFalse(parts[2].isBlank());
    }

    @Test
    @DisplayName("AC3: verify returns true for correct password")
    void verify_correctPassword_returnsTrue() {

        Pbkdf2PasswordHasher hasher =
                new Pbkdf2PasswordHasher(1000);

        String storedHash = hasher.hash(PASSWORD);

        assertTrue(
                hasher.verify(PASSWORD, storedHash));
    }

    @Test
    @DisplayName("AC3: verify returns false for wrong password")
    void verify_wrongPassword_returnsFalse() {

        Pbkdf2PasswordHasher hasher =
                new Pbkdf2PasswordHasher(1000);

        String storedHash = hasher.hash(PASSWORD);

        assertFalse(
                hasher.verify("WrongPassword", storedHash));
    }

    @Test
    @DisplayName("AC4: verify returns false when stored value is null")
    void verify_nullStored_returnsFalse() {

        Pbkdf2PasswordHasher hasher =
                new Pbkdf2PasswordHasher(1000);

        assertFalse(
                hasher.verify(PASSWORD, null));
    }

    @Test
    @DisplayName("AC4: verify returns false when raw password is null")
    void verify_nullPassword_returnsFalse() {

        Pbkdf2PasswordHasher hasher =
                new Pbkdf2PasswordHasher(1000);

        String storedHash = hasher.hash(PASSWORD);

        assertFalse(
                hasher.verify(null, storedHash));
    }

    @Test
    @DisplayName("AC4: verify returns false for malformed value")
    void verify_malformedStored_returnsFalse() {

        Pbkdf2PasswordHasher hasher =
                new Pbkdf2PasswordHasher(1000);

        assertAll(
                () -> assertFalse(
                        hasher.verify(PASSWORD, "invalid")),

                () -> assertFalse(
                        hasher.verify(PASSWORD, "1:2")),

                () -> assertFalse(
                        hasher.verify(PASSWORD, ":::")),

                () -> assertFalse(
                        hasher.verify(PASSWORD, "abc:def:ghi")),

                () -> assertFalse(
                        hasher.verify(PASSWORD, "not-a-number:abc:def"))
        );
    }

    @Test
    @DisplayName("AC4: verify returns false for blank stored value")
    void verify_blankStored_returnsFalse() {

        Pbkdf2PasswordHasher hasher =
                new Pbkdf2PasswordHasher(1000);

        assertFalse(
                hasher.verify(PASSWORD, " "));
    }

    @Test
    @DisplayName("AC7: hash throws ValidationException for null password")
    void hash_nullPassword_throwsValidationException() {

        Pbkdf2PasswordHasher hasher =
                new Pbkdf2PasswordHasher(1000);

        assertThrows(
                ValidationException.class,
                () -> hasher.hash(null));
    }

    @Test
    @DisplayName("AC7: hash throws ValidationException for blank password")
    void hash_blankPassword_throwsValidationException() {

        Pbkdf2PasswordHasher hasher =
                new Pbkdf2PasswordHasher(1000);

        assertAll(
                () -> assertThrows(
                        ValidationException.class,
                        () -> hasher.hash("")),

                () -> assertThrows(
                        ValidationException.class,
                        () -> hasher.hash(" ")),

                () -> assertThrows(
                        ValidationException.class,
                        () -> hasher.hash("     "))
        );
    }

    @Test
    @DisplayName("AC6: custom constructor uses provided iteration count")
    void constructor_usesProvidedIterationCount() {

        int iterations = 1000;

        Pbkdf2PasswordHasher hasher =
                new Pbkdf2PasswordHasher(iterations);

        String hash = hasher.hash(PASSWORD);

        assertTrue(
                hash.startsWith(iterations + ":"));
    }

    @Test
    @DisplayName("AC6: default constructor uses AppConstants iteration count")
    void defaultConstructor_usesAppConstant() {

        Pbkdf2PasswordHasher hasher =
                new Pbkdf2PasswordHasher();

        String hash = hasher.hash(PASSWORD);

        assertTrue(
                hash.startsWith(
                        AppConstants.PBKDF2_ITERATIONS + ":"));
    }

    @Test
    @DisplayName("AC1: hash contains Base64 encoded salt and hash values")
    void hash_containsEncodedSaltAndHash() {

        Pbkdf2PasswordHasher hasher =
                new Pbkdf2PasswordHasher(1000);

        String storedHash = hasher.hash(PASSWORD);

        String[] parts = storedHash.split(":");

        assertAll(
                () -> assertDoesNotThrow(() ->
                        java.util.Base64.getDecoder().decode(parts[1])),

                () -> assertDoesNotThrow(() ->
                        java.util.Base64.getDecoder().decode(parts[2]))
        );
    }
}