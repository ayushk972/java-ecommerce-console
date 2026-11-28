package com.flipkart.service.impl;

import com.flipkart.exception.DuplicateResourceException;
import com.flipkart.exception.ValidationException;
import com.flipkart.model.ActorType;
import com.flipkart.model.AuditAction;
import com.flipkart.model.AuditEntityType;
import com.flipkart.model.AuditOutcome;
import com.flipkart.model.AuditRecord;
import com.flipkart.model.Customer;
import com.flipkart.model.RegistrationRequest;
import com.flipkart.repository.CustomerRepository;
import com.flipkart.testsupport.FakeAuditService;
import com.flipkart.util.CustomerLoginIdGenerator;
import com.flipkart.util.PasswordHasher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomerRegistrationServiceImplTest {

    private static final Instant FIXED_NOW = Instant.parse("2026-04-15T10:15:30Z");

    private StubCustomerRepository customerRepository;
    private StubPasswordHasher passwordHasher;
    private FakeAuditService auditService;
    private Clock fixedClock;
    private AtomicInteger uuidSeq;
    private Supplier<UUID> uuidSupplier;
    private CustomerLoginIdGenerator loginIdGenerator;
    private CustomerRegistrationServiceImpl service;

    @BeforeEach
    void setUp() {
        customerRepository = new StubCustomerRepository();
        passwordHasher = new StubPasswordHasher();
        auditService = new FakeAuditService();
        fixedClock = Clock.fixed(FIXED_NOW, ZoneOffset.UTC);
        uuidSeq = new AtomicInteger(1);
        uuidSupplier = () -> new UUID(0L, uuidSeq.getAndIncrement());
        loginIdGenerator = (name, mobile) -> {
            String suffix = mobile.substring(mobile.length() - 2);
            int prefix = 10000;
            while (customerRepository.existsByLoginId(String.format("%05d%s", prefix, suffix))) {
                prefix++;
            }
            return String.format("%05d%s", prefix, suffix);
        };
        service = new CustomerRegistrationServiceImpl(
                customerRepository,
                passwordHasher,
                loginIdGenerator,
                auditService,
                fixedClock,
                uuidSupplier
        );
    }

    @Test
    @DisplayName("BR-A01..A05: valid registration stores customer with normalized name, 7-digit loginId, hashed password, and clock timestamp")
    void register_validRequest_storesCustomerAndWritesAudit() {
        RegistrationRequest request = new RegistrationRequest("  Daksh   Arya ", "9876543245", "Secret@123");

        Customer result = service.register(request);

        assertAll(
                () -> assertEquals(new UUID(0L, 1), result.getId()),
                () -> assertEquals("1000045", result.getLoginId()),
                () -> assertTrue(result.getLoginId().matches("\\d{7}")),
                () -> assertEquals("Daksh Arya", result.getName()),
                () -> assertEquals("9876543245", result.getMobile()),
                () -> assertNotEquals("Secret@123", result.getPasswordHash()),
                () -> assertTrue(passwordHasher.verify("Secret@123", result.getPasswordHash())),
                () -> assertEquals(FIXED_NOW, result.getRegisteredAt()),
                () -> assertEquals(1, customerRepository.count())
        );

        List<AuditRecord> records = auditService.getRecords();
        assertEquals(1, records.size());
        AuditRecord audit = records.get(0);
        assertAll(
                () -> assertEquals(ActorType.CUSTOMER, audit.getActor().type()),
                () -> assertEquals(result.getId().toString(), audit.getActor().id()),
                () -> assertEquals(AuditAction.CUSTOMER_REGISTERED, audit.getAction()),
                () -> assertEquals(AuditEntityType.CUSTOMER, audit.getEntityType()),
                () -> assertEquals(result.getId().toString(), audit.getEntityId()),
                () -> assertEquals(AuditOutcome.SUCCESS, audit.getOutcome()),
                () -> assertTrue(audit.getDetails().contains("1000045")),
                () -> assertTrue(audit.getDetails().contains("******3245")),
                () -> assertFalse(audit.getDetails().contains("9876543245")),
                () -> assertFalse(audit.getDetails().contains("Secret@123")),
                () -> assertFalse(audit.getDetails().contains(result.getPasswordHash()))
        );
    }

    @Test
    @DisplayName("AC2: validation failure saves and audits nothing")
    void register_invalidRequest_throwsValidationExceptionWithoutSideEffects() {
        RegistrationRequest invalid = new RegistrationRequest("A", "12345", "short");

        ValidationException ex = assertThrows(ValidationException.class, () -> service.register(invalid));

        assertAll(
                () -> assertTrue(ex.getErrors().size() >= 3),
                () -> assertEquals(0, customerRepository.count()),
                () -> assertTrue(auditService.getRecords().isEmpty()),
                () -> assertFalse(ex.getMessage().contains("short"))
        );
    }

    @Test
    @DisplayName("AC2: null request throws ValidationException without side effects")
    void register_nullRequest_throwsValidationException() {
        assertThrows(ValidationException.class, () -> service.register(null));
        assertEquals(0, customerRepository.count());
        assertTrue(auditService.getRecords().isEmpty());
    }

    @Test
    @DisplayName("BR-A03: duplicate mobile throws DuplicateResourceException and saves nothing")
    void register_duplicateMobile_throwsDuplicateResourceException() {
        service.register(new RegistrationRequest("First User", "9876543210", "Password@1"));

        DuplicateResourceException ex = assertThrows(
                DuplicateResourceException.class,
                () -> service.register(new RegistrationRequest("Second User", "9876543210", "Password@2"))
        );

        assertAll(
                () -> assertEquals("Mobile number is already registered", ex.getMessage()),
                () -> assertEquals(1, customerRepository.count()),
                () -> assertEquals(1, auditService.getRecords().size())
        );
    }

    @Test
    @DisplayName("BR-A05: same name and same last two mobile digits receive different loginIds")
    void register_sameNameAndSameLastTwoDigits_assignsDifferentLoginIds() {
        Customer first = service.register(new RegistrationRequest("Al", "9876543210", "Password@1"));
        Customer second = service.register(new RegistrationRequest("Al", "9123456710", "Password@2"));

        assertAll(
                () -> assertNotEquals(first.getLoginId(), second.getLoginId()),
                () -> assertEquals("1000010", first.getLoginId()),
                () -> assertEquals("1000110", second.getLoginId()),
                () -> assertEquals(2, customerRepository.count()),
                () -> assertEquals(2, auditService.getRecords().size())
        );
    }

    @Test
    @DisplayName("AC6: if repository save throws, no audit record is written")
    void register_repositorySaveThrows_writesNoAuditRecord() {
        customerRepository.failOnSave = true;

        assertThrows(
                RuntimeException.class,
                () -> service.register(new RegistrationRequest("Daksh Arya", "9876543245", "Secret@123"))
        );

        assertTrue(auditService.getRecords().isEmpty());
    }

    @Test
    @DisplayName("AC7: raw password never appears in exception messages or Customer.toString()")
    void register_rawPasswordNeverExposedInToStringOrExceptions() {
        String rawPassword = "MyVerySecretPassword99";
        Customer customer = service.register(new RegistrationRequest("Daksh Arya", "9876543245", rawPassword));

        assertFalse(customer.toString().contains(rawPassword));
        assertFalse(customer.toString().contains(customer.getPasswordHash()));

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> service.register(new RegistrationRequest("X", "9876543245", rawPassword))
        );
        assertFalse(ex.getMessage().contains(rawPassword));
    }

    private static final class StubCustomerRepository implements CustomerRepository {
        private final Map<UUID, Customer> byId = new LinkedHashMap<>();
        private boolean failOnSave = false;

        @Override
        public Customer save(Customer customer) {
            if (failOnSave) {
                throw new IllegalStateException("Simulated save failure");
            }
            byId.put(customer.getId(), customer);
            return customer;
        }

        @Override
        public Optional<Customer> findById(UUID id) {
            return Optional.ofNullable(byId.get(id));
        }

        @Override
        public Optional<Customer> findByLoginId(String loginId) {
            return byId.values().stream()
                    .filter(c -> c.getLoginId().equals(loginId))
                    .findFirst();
        }

        @Override
        public Optional<Customer> findByMobile(String mobile) {
            return byId.values().stream()
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
            return List.copyOf(new ArrayList<>(byId.values()));
        }

        @Override
        public long count() {
            return byId.size();
        }
    }

    private static final class StubPasswordHasher implements PasswordHasher {
        @Override
        public String hash(String raw) {
            return "hashed:" + raw;
        }

        @Override
        public boolean verify(String raw, String stored) {
            return stored != null && stored.equals("hashed:" + raw);
        }
    }
}
