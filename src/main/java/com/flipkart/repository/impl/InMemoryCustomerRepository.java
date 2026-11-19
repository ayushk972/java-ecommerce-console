package com.flipkart.repository.impl;

import com.flipkart.exception.DuplicateResourceException;
import com.flipkart.model.Customer;
import com.flipkart.repository.CustomerRepository;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * In-memory implementation of {@link CustomerRepository} backed by a
 * {@link LinkedHashMap} to preserve insertion order plus a {@code loginId} index.
 *
 * <p>No delete or remove methods are provided by design.
 */
public class InMemoryCustomerRepository implements CustomerRepository {

    private final Map<UUID, Customer> customersById = new LinkedHashMap<>();
    private final Map<String, UUID> customerIdByLoginId = new HashMap<>();

    @Override
    public Customer save(Customer customer) {
        Objects.requireNonNull(customer, "customer must not be null");

        UUID id = customer.getId();
        String loginId = customer.getLoginId();
        String mobile = customer.getMobile();

        UUID existingOwnerForLogin = customerIdByLoginId.get(loginId);
        if (existingOwnerForLogin != null && !existingOwnerForLogin.equals(id)) {
            throw new DuplicateResourceException("Login ID is already registered: " + loginId);
        }

        for (Customer existing : customersById.values()) {
            if (!existing.getId().equals(id) && existing.getMobile().equals(mobile)) {
                throw new DuplicateResourceException("Mobile number is already registered");
            }
        }

        Customer previous = customersById.put(id, customer);
        if (previous != null && !previous.getLoginId().equals(loginId)) {
            customerIdByLoginId.remove(previous.getLoginId());
        }
        customerIdByLoginId.put(loginId, id);

        return customer;
    }

    @Override
    public Optional<Customer> findById(UUID id) {
        Objects.requireNonNull(id, "id must not be null");
        return Optional.ofNullable(customersById.get(id));
    }

    @Override
    public Optional<Customer> findByLoginId(String loginId) {
        Objects.requireNonNull(loginId, "loginId must not be null");
        UUID id = customerIdByLoginId.get(loginId);
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(customersById.get(id));
    }

    @Override
    public Optional<Customer> findByMobile(String mobile) {
        Objects.requireNonNull(mobile, "mobile must not be null");
        for (Customer customer : customersById.values()) {
            if (customer.getMobile().equals(mobile)) {
                return Optional.of(customer);
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean existsByLoginId(String loginId) {
        Objects.requireNonNull(loginId, "loginId must not be null");
        return customerIdByLoginId.containsKey(loginId);
    }

    @Override
    public boolean existsByMobile(String mobile) {
        Objects.requireNonNull(mobile, "mobile must not be null");
        return findByMobile(mobile).isPresent();
    }

    @Override
    public List<Customer> findAll() {
        return List.copyOf(customersById.values());
    }

    @Override
    public long count() {
        return customersById.size();
    }
}