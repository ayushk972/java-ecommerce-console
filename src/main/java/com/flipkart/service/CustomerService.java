package com.flipkart.service;

import com.flipkart.model.Actor;
import com.flipkart.model.Customer;
import java.util.List;
import java.util.UUID;

/**
 * Service contract for reading registered customer records.
 */
public interface CustomerService {

    List<Customer> listCustomers(Actor actor);

    Customer getCustomer(UUID id);
}