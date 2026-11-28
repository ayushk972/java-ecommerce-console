package com.flipkart.service;

import com.flipkart.model.Customer;
import com.flipkart.model.RegistrationRequest;

/**
 * Service contract for registering new customer accounts (BR-A01..A05).
 */
public interface CustomerRegistrationService {

    /**
     * Validates the registration request, generates a unique 7-digit login ID,
     * hashes the password, persists the customer, and records a
     * {@code CUSTOMER_REGISTERED} audit entry.
     *
     * @param request customer registration details (must not be {@code null})
     * @return the newly created and persisted {@link Customer}
     * @throws com.flipkart.exception.ValidationException if the request or any field is invalid
     * @throws com.flipkart.exception.DuplicateResourceException if the mobile number is already registered
     * @throws com.flipkart.exception.IdGenerationException if a unique login ID cannot be generated
     */
    Customer register(RegistrationRequest request);
}
