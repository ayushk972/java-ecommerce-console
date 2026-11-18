package com.flipkart.repository;

import com.flipkart.model.Customer;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * In-memory storage contract for {@link Customer} entities.
 *
 * <p>No delete or remove methods are declared by design so that order history
 * and audit trail references remain intact.
 */
public interface CustomerRepository {

    Customer save(Customer customer);

    Optional<Customer> findById(UUID id);

    Optional<Customer> findByLoginId(String loginId);

    Optional<Customer> findByMobile(String mobile);

    boolean existsByLoginId(String loginId);

    boolean existsByMobile(String mobile);

    List<Customer> findAll();

    long count();
}