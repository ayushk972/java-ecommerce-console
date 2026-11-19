package com.flipkart.repository.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.flipkart.exception.DuplicateResourceException;
import com.flipkart.model.Customer;
import com.flipkart.repository.CustomerRepository;
import java.lang.reflect.Method;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InMemoryCustomerRepositoryTest {

    private static final Instant NOW = Instant.parse("2026-11-18T10:00:00Z");

    private InMemoryCustomerRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryCustomerRepository();
    }

    private static Customer sampleCustomer(UUID id, String loginId, String name, String mobile) {
        return Customer.create(id, loginId, name, mobile, "1000:salt:hash", NOW);
    }

    @Test
    void save_newAndExistingCustomer_insertsAndUpdatesInPlace() {
        UUID firstId = UUID.randomUUID();
        UUID secondId = UUID.randomUUID();

        Customer first = sampleCustomer(firstId, "0004310", "Al", "9876543210");
        Customer second = sampleCustomer(secondId, "9426845", "Daksh Arya", "9123456745");

        repository.save(first);
        repository.save(second);
        assertEquals(2L, repository.count());

        // Re-save first customer with updated name; insertion order must remain [first, second]
        Customer updatedFirst = sampleCustomer(firstId, "0004310", "Al Updated", "9876543210");
        repository.save(updatedFirst);

        assertEquals(2L, repository.count());
        List<Customer> all = repository.findAll();
        assertEquals(List.of(updatedFirst, second), all);
        assertEquals("Al Updated", repository.findById(firstId).orElseThrow().getName());
    }

    @Test
    void save_duplicateLoginIdDifferentCustomer_throwsDuplicateResourceException() {
        Customer first = sampleCustomer(UUID.randomUUID(), "0004310", "Al", "9876543210");
        Customer duplicateLogin = sampleCustomer(UUID.randomUUID(), "0004310", "Other User", "9876543299");

        repository.save(first);

        assertThrows(DuplicateResourceException.class, () -> repository.save(duplicateLogin));
        assertEquals(1L, repository.count());
    }

    @Test
    void save_duplicateMobileDifferentCustomer_throwsDuplicateResourceException() {
        Customer first = sampleCustomer(UUID.randomUUID(), "0004310", "Al", "9876543210");
        Customer duplicateMobile = sampleCustomer(UUID.randomUUID(), "0004410", "Other User", "9876543210");

        repository.save(first);

        assertThrows(DuplicateResourceException.class, () -> repository.save(duplicateMobile));
        assertEquals(1L, repository.count());
    }

    @Test
    void lookupMethods_leadingZeroLoginIdAndMobile_behaveCorrectly() {
        UUID id = UUID.randomUUID();
        Customer customer = sampleCustomer(id, "0004310", "Al", "9876543210");
        repository.save(customer);

        assertEquals(Optional.of(customer), repository.findById(id));
        assertEquals(Optional.of(customer), repository.findByLoginId("0004310"));
        assertEquals(Optional.of(customer), repository.findByMobile("9876543210"));
        assertTrue(repository.existsByLoginId("0004310"));
        assertTrue(repository.existsByMobile("9876543210"));

        // Leading zeros must not be stripped
        assertEquals(Optional.empty(), repository.findByLoginId("4310"));
        assertFalse(repository.existsByLoginId("4310"));
        assertEquals(Optional.empty(), repository.findByMobile("9000000000"));
        assertFalse(repository.existsByMobile("9000000000"));
        assertEquals(Optional.empty(), repository.findById(UUID.randomUUID()));
    }

    @Test
    void findAll_returnedSnapshot_isUnmodifiable() {
        Customer customer = sampleCustomer(UUID.randomUUID(), "9426845", "Daksh Arya", "9123456745");
        repository.save(customer);

        List<Customer> snapshot = repository.findAll();
        assertThrows(UnsupportedOperationException.class, () -> snapshot.add(customer));
    }

    @Test
    void repositoryContract_deleteOrRemoveMethods_doNotExist() {
        for (Class<?> type : List.of(CustomerRepository.class, InMemoryCustomerRepository.class)) {
            for (Method method : type.getMethods()) {
                String name = method.getName();
                assertFalse(
                        name.startsWith("delete") || name.startsWith("remove"),
                        () -> "Forbidden delete/remove method found on " + type.getSimpleName() + ": " + name);
            }
        }
    }
}