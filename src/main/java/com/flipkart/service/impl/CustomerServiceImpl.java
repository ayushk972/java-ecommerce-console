package com.flipkart.service.impl;

import com.flipkart.exception.AuthorizationException;
import com.flipkart.exception.ResourceNotFoundException;
import com.flipkart.model.Actor;
import com.flipkart.model.ActorType;
import com.flipkart.model.Customer;
import com.flipkart.repository.CustomerRepository;
import com.flipkart.service.CustomerService;
import com.flipkart.util.ErrorMessages;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Default implementation of {@link CustomerService} for Admin customer read operations.
 */
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerServiceImpl(CustomerRepository customerRepository) {
        this.customerRepository = Objects.requireNonNull(
                customerRepository, "customerRepository must not be null");
    }

    @Override
    public List<Customer> listCustomers(Actor actor) {
        if (actor == null || actor.type() != ActorType.ADMIN) {
            throw new AuthorizationException(ErrorMessages.ACCESS_DENIED);
        }
        return customerRepository.findAll();
    }

    @Override
    public Customer getCustomer(UUID id) {
        Objects.requireNonNull(id, "id must not be null");
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", id.toString()));
    }
}