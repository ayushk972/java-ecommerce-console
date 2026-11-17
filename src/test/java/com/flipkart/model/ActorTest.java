package com.flipkart.model;

import com.flipkart.exception.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
/**
 * @author nitin
 */
class ActorTest {

    @Nested
    class Factories {

        @Test
        @DisplayName("BR-AU02: guest() has type GUEST and id guest")
        void guest_always_hasGuestTypeAndId() {
            Actor actor = Actor.guest();

            assertAll(
                    () -> assertEquals(ActorType.GUEST, actor.type()),
                    () -> assertEquals("guest", actor.id()));
        }

        @Test
        @DisplayName("BR-AU02: system() has type SYSTEM and id system")
        void system_always_hasSystemTypeAndId() {
            Actor actor = Actor.system();

            assertAll(
                    () -> assertEquals(ActorType.SYSTEM, actor.type()),
                    () -> assertEquals("system", actor.id()));
        }

        @Test
        @DisplayName("BR-AU02: admin(id) has type ADMIN and the given id")
        void admin_validId_hasAdminTypeAndId() {
            Actor actor = Actor.admin("admin");

            assertAll(
                    () -> assertEquals(ActorType.ADMIN, actor.type()),
                    () -> assertEquals("admin", actor.id()));
        }

        @Test
        @DisplayName("BR-AU02: customer(uuid) has type CUSTOMER and the UUID text as id")
        void customer_validUuid_hasCustomerTypeAndUuidText() {
            UUID uuid = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

            Actor actor = Actor.customer(uuid);

            assertAll(
                    () -> assertEquals(ActorType.CUSTOMER, actor.type()),
                    () -> assertEquals("123e4567-e89b-12d3-a456-426614174000", actor.id()));
        }

        @Test
        void admin_idWithSurroundingSpaces_isTrimmed() {
            assertEquals("admin", Actor.admin("  admin  ").id());
        }
    }

    @Nested
    class Validation {

        @Test
        void admin_nullId_throwsValidation() {
            assertThrows(ValidationException.class, () -> Actor.admin(null));
        }

        @ParameterizedTest
        @ValueSource(strings = {"", " ", "   ", "\t", "\n"})
        void admin_blankId_throwsValidation(String blank) {
            assertThrows(ValidationException.class, () -> Actor.admin(blank));
        }

        @Test
        void customer_nullUuid_throwsValidation() {
            assertThrows(ValidationException.class, () -> Actor.customer(null));
        }

        @Test
        void constructor_nullType_throwsValidation() {
            ValidationException ex =
                    assertThrows(ValidationException.class, () -> new Actor(null, "x"));

            assertEquals(1, ex.getErrors().size());
        }

        @Test
        void constructor_nullTypeAndBlankId_reportsBothErrors() {
            ValidationException ex =
                    assertThrows(ValidationException.class, () -> new Actor(null, " "));

            assertEquals(2, ex.getErrors().size());
        }
    }

    @Nested
    class Equality {

        @Test
        void equals_sameTypeAndId_areEqual() {
            assertEquals(Actor.admin("admin"), Actor.admin("admin"));
            assertEquals(Actor.admin("admin").hashCode(), Actor.admin("admin").hashCode());
        }

        @Test
        void equals_sameIdDifferentType_areNotEqual() {
            assertNotEquals(new Actor(ActorType.ADMIN, "x"), new Actor(ActorType.CUSTOMER, "x"));
        }
    }
}
