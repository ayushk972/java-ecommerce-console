package com.flipkart.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.flipkart.exception.AuthorizationException;
import com.flipkart.exception.ResourceNotFoundException;
import com.flipkart.model.Actor;
import com.flipkart.model.Customer;
import com.flipkart.repository.CustomerRepository;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CustomerServiceImplTest {

    private static final Instant NOW = Instant.parse("2026-11-18T10:00:00Z");

    private FakeCustomerRepository fakeRepository;
    private CustomerServiceImpl customerService;

    @BeforeEach
    void setUp() {
        fakeRepository = new FakeCustomerRepository();
        customerService = new CustomerServiceImpl(fakeRepository);
    }

    private static Customer sampleCustomer(UUID id, String loginId, String name, String mobile) {
        return Customer.create(id, loginId, name, mobile, "1000:salt:hash", NOW);
    }

    @Test
    void listCustomers_adminActor_returnsAllCustomersInOrder() {
        Customer first = sampleCustomer(UUID.randomUUID(), "0004310", "Al", "9876543210");
        Customer second = sampleCustomer(UUID.randomUUID(), "9426845", "Daksh Arya", "9123456745");
        fakeRepository.save(first);
        fakeRepository.save(second);

        List<Customer> result = customerService.listCustomers(Actor.admin("admin"));

        assertEquals(List.of(first, second), result);
    }

    @Test
    void listCustomers_nonAdminOrNullActor_throwsAuthorizationException() {
        Actor customerActor = Actor.customer(UUID.randomUUID());
        Actor guestActor = Actor.guest();

        assertThrows(AuthorizationException.class, () -> customerService.listCustomers(customerActor));
        assertThrows(AuthorizationException.class, () -> customerService.listCustomers(guestActor));
        assertThrows(AuthorizationException.class, () -> customerService.listCustomers(null));
    }

    @Test
    void getCustomer_existingId_returnsCustomer() {
        UUID id = UUID.randomUUID();
        Customer expected = sampleCustomer(id, "9426845", "Daksh Arya", "9123456745");
        fakeRepository.save(expected);

        Customer actual = customerService.getCustomer(id);

        assertEquals(expected, actual);
    }

    @Test
    void getCustomer_unknownId_throwsResourceNotFoundException() {
        UUID unknownId = UUID.randomUUID();

        assertThrows(ResourceNotFoundException.class, () -> customerService.getCustomer(unknownId));
    }

    /**
     * Nested fake repository for isolated service unit testing.
     */
    private static final class FakeCustomerRepository implements CustomerRepository {

        private final Map<UUID, Customer> store = new LinkedHashMap<>();

        @Override
        public Customer save(Customer customer) {
            store.put(customer.getId(), customer);
            return customer;
        }

        @Override
        public Optional<Customer> findById(UUID id) {
            return Optional.ofNullable(store.get(id));
        }

        @Override
        public Optional<Customer> findByLoginId(String loginId) {
            return store.values().stream()
                    .filter(c -> c.getLoginId().equals(loginId))
                    .findFirst();
        }

        @Override
        public Optional<Customer> findByMobile(String mobile) {
            return store.values().stream()
                    .filter(c -> c.getMobile().equals(mobile))
                    .findFirst();
        }

        @Override
        public boolean existsByLoginId(String loginId) {
            return findByLoginId(loginId).isPresent();
        }

        @Override
        public boolean existsByMobile(String mobile) {
            return findByMobile(mobile).isPresent();
        }

        @Override
        public List<Customer> findAll() {
            return List.copyOf(store.values());
        }

        @Override
        public long count() {
            return store.size();
        }
    }
}