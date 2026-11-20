package com.flipkart.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import com.flipkart.exception.IdGenerationException;
import com.flipkart.exception.ValidationException;

class HashedCustomerLoginIdGeneratorTest {

    @Test
    void nameHash_al_returns43() {
        assertEquals(43,
                HashedCustomerLoginIdGenerator.nameHash("Al"));
    }

    @Test
    void nameHash_dakshArya_returns94268() {
        assertEquals(94268,
                HashedCustomerLoginIdGenerator.nameHash("Daksh Arya"));
    }

    @Test
    void nameHash_ignoresCaseAndSpecialCharacters() {
        int expected =
                HashedCustomerLoginIdGenerator.nameHash("Daksh Arya");

        assertEquals(expected,
                HashedCustomerLoginIdGenerator.nameHash("daksh  ARYA"));

        assertEquals(expected,
                HashedCustomerLoginIdGenerator.nameHash("Daksh-Arya"));
    }

    @Test
    void nameHash_null_throwsValidationException() {
        assertThrows(ValidationException.class,
                () -> HashedCustomerLoginIdGenerator.nameHash(null));
    }

    @Test
    void nameHash_noEnglishLetters_throwsValidationException() {
        assertThrows(ValidationException.class,
                () -> HashedCustomerLoginIdGenerator.nameHash("12345"));
    }

    @Test
    void nameHash_accentedCharactersAreRemoved() {
        assertEquals(
                HashedCustomerLoginIdGenerator.nameHash("jos"),
                HashedCustomerLoginIdGenerator.nameHash("Jos\u00E9"));
    }

    @Test
    void nextPrefix_incrementsPrefix() {
        assertEquals(44,
                HashedCustomerLoginIdGenerator.nextPrefix(43));
    }

    @Test
    void nextPrefix_maxPrefix_returnsZero() {
        assertEquals(0,
                HashedCustomerLoginIdGenerator.nextPrefix(99999));
    }

    @Test
    void nextPrefix_invalidPrefix_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> HashedCustomerLoginIdGenerator.nextPrefix(100000));
    }
    @Test
    void buildId_validInput_returnsSevenDigitId() {
        assertEquals("9426845",
                HashedCustomerLoginIdGenerator.buildId(
                        94268, "9876543245"));
    }

    @Test
    void buildId_smallPrefix_preservesLeadingZeros() {
        assertEquals("0004310",
                HashedCustomerLoginIdGenerator.buildId(
                        43, "9876543210"));
    }
    @Test
    void buildId_invalidMobile_throwsValidationException() {
        assertThrows(ValidationException.class,
                () -> HashedCustomerLoginIdGenerator.buildId(
                        43, "12345"));
    }

    @Test
    void generate_existingId_generatesNextPrefix() {

        Set<String> existingIds = new HashSet<>();
        existingIds.add("0004310");

        HashedCustomerLoginIdGenerator generator =
                new HashedCustomerLoginIdGenerator(existingIds::contains);

        assertEquals("0004410",
                generator.generate("Al", "9876543210"));
    }

    @Test
    void generate_sameNameAndMobile_handlesCollision() {

        Set<String> existingIds = new HashSet<>();
        existingIds.add("0004310");
        existingIds.add("0004410");

        HashedCustomerLoginIdGenerator generator =
                new HashedCustomerLoginIdGenerator(existingIds::contains);

        assertEquals("0004510",
                generator.generate("Al", "9876543210"));
    }

    @Test
    void generate_allIdsOccupied_throwsIdGenerationException() {

        AtomicInteger attempts = new AtomicInteger();

        HashedCustomerLoginIdGenerator generator =
                new HashedCustomerLoginIdGenerator(id -> {
                    attempts.incrementAndGet();
                    return true;
                });

        assertThrows(IdGenerationException.class,
                () -> generator.generate("Al", "9876543210"));

        assertEquals(100000, attempts.get());
    }

    @Test
    void constructor_nullPredicate_throwsException() {
        assertThrows(NullPointerException.class,
                () -> new HashedCustomerLoginIdGenerator(null));
    }

    @Test
    void generate_blankName_throwsValidationException() {
        HashedCustomerLoginIdGenerator generator =
                new HashedCustomerLoginIdGenerator(id -> false);

        assertThrows(ValidationException.class,
                () -> generator.generate("   ", "9876543210"));
    }

    @Test
    void generate_mobileWithCountryCode_throwsValidationException() {
        HashedCustomerLoginIdGenerator generator =
                new HashedCustomerLoginIdGenerator(id -> false);

        assertThrows(ValidationException.class,
                () -> generator.generate("Al", "+919876543210"));
    }


}
