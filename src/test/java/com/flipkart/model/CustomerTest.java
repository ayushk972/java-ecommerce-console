package com.flipkart.model;

/**
 * 
 * @author daksh
 * 
 */




import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CustomerTest {

    private Customer createCustomer(
            UUID id,
            String loginId,
            String hash) {

        return Customer.create(
                id,
                loginId,
                "Daksh Arya",
                "9876543210",
                hash,
                Instant.parse("2026-11-16T10:00:00Z")
        );
    }

    @Test
    void shouldCreateCustomerWithLeadingZeroLoginId() {
        Customer customer = createCustomer(
                UUID.randomUUID(), "0004310", "hashed-secret");

        assertEquals("0004310", customer.getLoginId());
    }

    @Test
    void shouldRejectNullFields() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();

        assertAll(
            () -> assertThrows(NullPointerException.class,
                    () -> Customer.create(null, "1234567",
                            "Daksh Arya", "9876543210",
                            "hash", now)),

            () -> assertThrows(NullPointerException.class,
                    () -> Customer.create(id, null,
                            "Daksh Arya", "9876543210",
                            "hash", now)),

            () -> assertThrows(NullPointerException.class,
                    () -> Customer.create(id, "1234567",
                            null, "9876543210", "hash", now)),

            () -> assertThrows(NullPointerException.class,
                    () -> Customer.create(id, "1234567",
                            "Daksh Arya", null, "hash", now)),

            () -> assertThrows(NullPointerException.class,
                    () -> Customer.create(id, "1234567",
                            "Daksh Arya", "9876543210", null, now)),

            () -> assertThrows(NullPointerException.class,
                    () -> Customer.create(id, "1234567",
                            "Daksh Arya", "9876543210",
                            "hash", null))
        );
    }

    @Test
    void shouldRejectInvalidLoginId() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();

        assertAll(
            () -> assertThrows(IllegalArgumentException.class,
                    () -> Customer.create(id, "123456",
                            "Daksh Arya", "9876543210", "hash", now)),

            () -> assertThrows(IllegalArgumentException.class,
                    () -> Customer.create(id, "12345678",
                            "Daksh Arya", "9876543210", "hash", now)),

            () -> assertThrows(IllegalArgumentException.class,
                    () -> Customer.create(id, "12A4567",
                            "Daksh Arya", "9876543210", "hash", now))
        );
    }

    @Test
    void shouldMaskMobileNumber() {
        Customer customer = createCustomer(
                UUID.randomUUID(), "1234567", "hash");

        assertEquals("******3210", customer.maskedMobile());
    }

    @Test
    void shouldUseIdForEqualsAndHashCode() {
        UUID id = UUID.randomUUID();

        Customer first = createCustomer(id, "1234567", "hash-one");
        Customer second = createCustomer(id, "7654321", "hash-two");

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void shouldNotBeEqualWhenIdsDiffer() {
        Customer first = createCustomer(
                UUID.randomUUID(), "1234567", "hash");

        Customer second = createCustomer(
                UUID.randomUUID(), "1234567", "hash");

        assertNotEquals(first, second);
    }

    @Test
    void toStringMustNotExposePasswordHash() {
        String secretHash = "VERY_SECRET_PASSWORD_HASH";

        Customer customer = createCustomer(
                UUID.randomUUID(), "1234567", secretHash);

        assertFalse(customer.toString().contains(secretHash));
        assertFalse(customer.toString().contains("9876543210"));
        assertTrue(customer.toString().contains("******3210"));
    }
}

