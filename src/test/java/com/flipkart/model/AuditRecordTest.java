package com.flipkart.model;

import com.flipkart.exception.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
/**
 * @author nitin 
 */
class AuditRecordTest {

    private static final Instant NOW = Instant.parse("2026-11-16T10:00:00Z");
    private static final Actor ADMIN = Actor.admin("admin");

    private static AuditRecord withDetails(String details) {
        return new AuditRecord("AUD-000001", NOW, ADMIN, AuditAction.PRODUCT_CREATED,
                AuditEntityType.PRODUCT, "PRD-1001", AuditOutcome.SUCCESS, details);
    }

    @Nested
    class Construction {

        @Test
        @DisplayName("BR-AU02: all fields are stored and exposed")
        void constructor_validArguments_exposesEveryField() {
            AuditRecord r = withDetails("created");

            assertAll(
                    () -> assertEquals("AUD-000001", r.getAuditId()),
                    () -> assertEquals(NOW, r.getTimestamp()),
                    () -> assertEquals(ADMIN, r.getActor()),
                    () -> assertEquals(AuditAction.PRODUCT_CREATED, r.getAction()),
                    () -> assertEquals(AuditEntityType.PRODUCT, r.getEntityType()),
                    () -> assertEquals("PRD-1001", r.getEntityId()),
                    () -> assertEquals(AuditOutcome.SUCCESS, r.getOutcome()),
                    () -> assertEquals("created", r.getDetails()));
        }

        @Test
        void equals_sameValues_areEqual() {
            assertEquals(withDetails("a"), withDetails("a"));
            assertEquals(withDetails("a").hashCode(), withDetails("a").hashCode());
        }

        @Test
        void equals_differentDetails_areNotEqual() {
            assertNotEquals(withDetails("a"), withDetails("b"));
        }
    }

    @Nested
    class NullRejection {

        @Test
        @DisplayName("BR-AU02: null auditId is rejected")
        void constructor_nullAuditId_throws() {
            assertThrows(ValidationException.class, () -> new AuditRecord(null, NOW, ADMIN,
                    AuditAction.PRODUCT_CREATED, AuditEntityType.PRODUCT, "p", AuditOutcome.SUCCESS, ""));
        }

        @Test
        void constructor_nullTimestamp_throws() {
            assertThrows(ValidationException.class, () -> new AuditRecord("a", null, ADMIN,
                    AuditAction.PRODUCT_CREATED, AuditEntityType.PRODUCT, "p", AuditOutcome.SUCCESS, ""));
        }

        @Test
        void constructor_nullActor_throws() {
            assertThrows(ValidationException.class, () -> new AuditRecord("a", NOW, null,
                    AuditAction.PRODUCT_CREATED, AuditEntityType.PRODUCT, "p", AuditOutcome.SUCCESS, ""));
        }

        @Test
        void constructor_nullAction_throws() {
            assertThrows(ValidationException.class, () -> new AuditRecord("a", NOW, ADMIN,
                    null, AuditEntityType.PRODUCT, "p", AuditOutcome.SUCCESS, ""));
        }

        @Test
        void constructor_nullEntityType_throws() {
            assertThrows(ValidationException.class, () -> new AuditRecord("a", NOW, ADMIN,
                    AuditAction.PRODUCT_CREATED, null, "p", AuditOutcome.SUCCESS, ""));
        }

        @Test
        void constructor_nullEntityId_throws() {
            assertThrows(ValidationException.class, () -> new AuditRecord("a", NOW, ADMIN,
                    AuditAction.PRODUCT_CREATED, AuditEntityType.PRODUCT, null, AuditOutcome.SUCCESS, ""));
        }

        @Test
        void constructor_nullOutcome_throws() {
            assertThrows(ValidationException.class, () -> new AuditRecord("a", NOW, ADMIN,
                    AuditAction.PRODUCT_CREATED, AuditEntityType.PRODUCT, "p", null, ""));
        }

        @Test
        void constructor_everythingNull_reportsAllSevenErrors() {
            ValidationException ex = assertThrows(ValidationException.class,
                    () -> new AuditRecord(null, null, null, null, null, null, null, null));

            assertEquals(7, ex.getErrors().size());
        }
    }

    @Nested
    class Details {

        @Test
        @DisplayName("BR-AU02: null details become the empty string")
        void constructor_nullDetails_becomesEmpty() {
            assertEquals("", withDetails(null).getDetails());
        }

        @Test
        void constructor_emptyDetails_staysEmpty() {
            assertEquals("", withDetails("").getDetails());
        }

        @Test
        @DisplayName("BR-AU02: details of exactly 500 characters are kept unchanged")
        void constructor_exactly500Characters_unchanged() {
            String text = "x".repeat(500);

            assertEquals(text, withDetails(text).getDetails());
        }

        @Test
        @DisplayName("BR-AU02: details of 501 characters are truncated to 500 ending with an ellipsis")
        void constructor_501Characters_truncatedWithEllipsis() {
            String text = "x".repeat(501);

            String stored = withDetails(text).getDetails();

            assertAll(
                    () -> assertEquals(500, stored.length()),
                    () -> assertTrue(stored.endsWith("...")),
                    () -> assertEquals("x".repeat(497), stored.substring(0, 497)));
        }

        @Test
        void constructor_veryLongDetails_truncatedToLimit() {
            String stored = withDetails("ab".repeat(5000)).getDetails();

            assertAll(
                    () -> assertEquals(500, stored.length()),
                    () -> assertTrue(stored.startsWith("abab")),
                    () -> assertTrue(stored.endsWith("...")));
        }

        @Test
        void constructor_cutFallsInsideSurrogatePair_doesNotSplitThePair() {
            // 496 plain chars, then an emoji (2 chars) starting at index 496, then padding
            String text = "x".repeat(496) + "\uD83D\uDE00" + "y".repeat(50);

            String stored = withDetails(text).getDetails();

            assertAll(
                    () -> assertTrue(stored.length() <= 500),
                    () -> assertTrue(stored.endsWith("...")),
                    () -> assertFalse(Character.isHighSurrogate(stored.charAt(stored.length() - 4))));
        }
    }

    @Nested
    class Immutability {

        @Test
        @DisplayName("BR-AU01: every instance field is private and final")
        void fields_allInstanceFields_arePrivateFinal() {
            for (Field f : AuditRecord.class.getDeclaredFields()) {
                if (Modifier.isStatic(f.getModifiers())) {
                    continue;
                }
                assertTrue(Modifier.isFinal(f.getModifiers()), f.getName() + " must be final");
                assertTrue(Modifier.isPrivate(f.getModifiers()), f.getName() + " must be private");
            }
        }

        @Test
        @DisplayName("BR-AU01: no setter or mutator method exists")
        void methods_noSetters() {
            for (Method m : AuditRecord.class.getDeclaredMethods()) {
                assertFalse(m.getName().startsWith("set"), "unexpected setter " + m.getName());
                assertFalse(m.getName().startsWith("with"), "unexpected mutator " + m.getName());
            }
        }

        @Test
        void class_isFinal() {
            assertTrue(Modifier.isFinal(AuditRecord.class.getModifiers()));
        }
    }
}
