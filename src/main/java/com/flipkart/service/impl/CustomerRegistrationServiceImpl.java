package com.flipkart.service.impl;

import com.flipkart.exception.DuplicateResourceException;
import com.flipkart.model.Actor;
import com.flipkart.model.AuditAction;
import com.flipkart.model.AuditEntityType;
import com.flipkart.model.AuditOutcome;
import com.flipkart.model.Customer;
import com.flipkart.model.RegistrationRequest;
import com.flipkart.repository.CustomerRepository;
import com.flipkart.service.AuditService;
import com.flipkart.service.CustomerRegistrationService;
import com.flipkart.util.CustomerLoginIdGenerator;
import com.flipkart.util.CustomerValidator;
import com.flipkart.util.PasswordHasher;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Default implementation of {@link CustomerRegistrationService}.
 */
public class CustomerRegistrationServiceImpl implements CustomerRegistrationService {

    private final CustomerRepository customerRepository;
    private final PasswordHasher passwordHasher;
    private final CustomerLoginIdGenerator loginIdGenerator;
    private final AuditService auditService;
    private final Clock clock;
    private final Supplier<UUID> uuidSupplier;

    /**
     * Creates a registration service with all required collaborators.
     */
    public CustomerRegistrationServiceImpl(
            CustomerRepository customerRepository,
            PasswordHasher passwordHasher,
            CustomerLoginIdGenerator loginIdGenerator,
            AuditService auditService,
            Clock clock,
            Supplier<UUID> uuidSupplier) {
        this.customerRepository = Objects.requireNonNull(customerRepository, "customerRepository must not be null");
        this.passwordHasher = Objects.requireNonNull(passwordHasher, "passwordHasher must not be null");
        this.loginIdGenerator = Objects.requireNonNull(loginIdGenerator, "loginIdGenerator must not be null");
        this.auditService = Objects.requireNonNull(auditService, "auditService must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
        this.uuidSupplier = Objects.requireNonNull(uuidSupplier, "uuidSupplier must not be null");
    }

    @Override
    public Customer register(RegistrationRequest request) {
        CustomerValidator.validateRegistration(request);

        String normalizedName = CustomerValidator.normalizeName(request.name());
        String mobile = request.mobile();

        if (customerRepository.existsByMobile(mobile)) {
            throw new DuplicateResourceException("Mobile number is already registered");
        }

        String loginId = loginIdGenerator.generate(normalizedName, mobile);
        String passwordHash = passwordHasher.hash(request.password());
        UUID id = Objects.requireNonNull(uuidSupplier.get(), "uuidSupplier returned null");
        Instant now = clock.instant();

        Customer customer = Customer.create(id, loginId, normalizedName, mobile, passwordHash, now);
        Customer saved = customerRepository.save(customer);

        auditService.record(
                Actor.customer(saved.getId()),
                AuditAction.CUSTOMER_REGISTERED,
                AuditEntityType.CUSTOMER,
                saved.getId().toString(),
                AuditOutcome.SUCCESS,
                "loginId=" + saved.getLoginId() + ", mobile=" + saved.maskedMobile()
        );

        return saved;
    }
}
