package com.flipkart.repository.impl;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.flipkart.exception.ValidationException;
import com.flipkart.model.Actor;
import com.flipkart.model.AuditAction;
import com.flipkart.model.AuditEntityType;
import com.flipkart.model.AuditOutcome;
import com.flipkart.model.AuditRecord;
import com.flipkart.repository.AuditRepository;

class InMemoryAuditRepositoryTest {

    private static final Instant NOW = Instant.parse("2026-11-16T10:00:00Z");
    private static final Actor ADMIN = Actor.admin("admin");
    private static final Actor CUSTOMER = Actor.customer(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"));

    private InMemoryAuditRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryAuditRepository();
    }

    private static AuditRecord record(int n, Actor actor, AuditAction action,
                                      AuditEntityType type, String entityId) {
        return new AuditRecord(String.format("AUD-%06d", n), NOW.plusSeconds(n), actor, action,
                type, entityId, AuditOutcome.SUCCESS, "details " + n);
    }

    private static AuditRecord record(int n) {
        return record(n, ADMIN, AuditAction.PRODUCT_CREATED, AuditEntityType.PRODUCT, "PRD-1001");
    }

    @Nested
    class Append {

        @Test
        @DisplayName("BR-AU01: append returns the stored record")
        void append_validRecord_returnsSameRecord() {
            AuditRecord r = record(1);

            assertSame(r, repository.append(r));
        }

        @Test
        void append_validRecord_increasesCount() {
            repository.append(record(1));
            repository.append(record(2));

            assertEquals(2, repository.count());
        }

        @Test
        void append_nullRecord_throwsValidationAndStoresNothing() {
            assertThrows(ValidationException.class, () -> repository.append(null));

            assertEquals(0, repository.count());
        }
    }

    @Nested
    class FindAll {

        @Test
        void findAll_emptyRepository_returnsEmptyList() {
            assertTrue(repository.findAll().isEmpty());
        }

        @Test
        @DisplayName("BR-AU01: findAll returns records oldest first")
        void findAll_severalRecords_returnsOldestFirst() {
            AuditRecord r1 = repository.append(record(1));
            AuditRecord r2 = repository.append(record(2));
            AuditRecord r3 = repository.append(record(3));

            assertEquals(List.of(r1, r2, r3), repository.findAll());
        }

        @Test
        @DisplayName("BR-AU01: findAll result cannot be modified")
        void findAll_result_isUnmodifiable() {
            repository.append(record(1));
            List<AuditRecord> snapshot = repository.findAll();

            assertAll(
                    () -> assertThrows(UnsupportedOperationException.class, () -> snapshot.add(record(2))),
                    () -> assertThrows(UnsupportedOperationException.class, () -> snapshot.remove(0)),
                    () -> assertThrows(UnsupportedOperationException.class, snapshot::clear));
        }

        @Test
        void findAll_snapshot_isNotAffectedByLaterAppends() {
            repository.append(record(1));
            List<AuditRecord> snapshot = repository.findAll();

            repository.append(record(2));

            assertAll(
                    () -> assertEquals(1, snapshot.size()),
                    () -> assertEquals(2, repository.findAll().size()));
        }
    }

    @Nested
    class Queries {

        @Test
        @DisplayName("BR-AU06: findByEntity matches entity type and id")
        void findByEntity_matchingTypeAndId_returnsOnlyThoseOldestFirst() {
            AuditRecord p1 = repository.append(record(1, ADMIN, AuditAction.PRODUCT_CREATED, AuditEntityType.PRODUCT, "PRD-1001"));
            repository.append(record(2, ADMIN, AuditAction.PRODUCT_CREATED, AuditEntityType.PRODUCT, "PRD-1002"));
            repository.append(record(3, CUSTOMER, AuditAction.ORDER_PLACED, AuditEntityType.ORDER, "PRD-1001"));
            AuditRecord p4 = repository.append(record(4, ADMIN, AuditAction.STOCK_INCREASED, AuditEntityType.PRODUCT, "PRD-1001"));

            assertEquals(List.of(p1, p4), repository.findByEntity(AuditEntityType.PRODUCT, "PRD-1001"));
        }

        @Test
        void findByEntity_noMatch_returnsEmptyList() {
            repository.append(record(1));

            assertTrue(repository.findByEntity(AuditEntityType.ORDER, "ORD-000001").isEmpty());
        }

        @Test
        void findByEntity_nullArguments_throwValidation() {
            assertAll(
                    () -> assertThrows(ValidationException.class, () -> repository.findByEntity(null, "x")),
                    () -> assertThrows(ValidationException.class, () -> repository.findByEntity(AuditEntityType.PRODUCT, null)));
        }

        @Test
        @DisplayName("BR-AU06: findByActor matches type and id")
        void findByActor_matchingActor_returnsOnlyThatActorsRecords() {
            AuditRecord a1 = repository.append(record(1, ADMIN, AuditAction.PRODUCT_CREATED, AuditEntityType.PRODUCT, "p"));
            repository.append(record(2, CUSTOMER, AuditAction.ORDER_PLACED, AuditEntityType.ORDER, "o"));
            AuditRecord a3 = repository.append(record(3, ADMIN, AuditAction.STOCK_INCREASED, AuditEntityType.PRODUCT, "p"));

            assertEquals(List.of(a1, a3), repository.findByActor(Actor.admin("admin")));
        }

        @Test
        void findByActor_sameIdDifferentType_doesNotMatch() {
            repository.append(record(1, ADMIN, AuditAction.PRODUCT_CREATED, AuditEntityType.PRODUCT, "p"));

            assertTrue(repository.findByActor(Actor.system()).isEmpty());
        }

        @Test
        void findByActor_null_throwsValidation() {
            assertThrows(ValidationException.class, () -> repository.findByActor(null));
        }

        @Test
        @DisplayName("BR-AU06: findByAction matches the action")
        void findByAction_matchingAction_returnsOnlyThoseOldestFirst() {
            AuditRecord r1 = repository.append(record(1, ADMIN, AuditAction.ORDER_CANCELLED, AuditEntityType.ORDER, "o1"));
            repository.append(record(2, ADMIN, AuditAction.ORDER_PLACED, AuditEntityType.ORDER, "o1"));
            AuditRecord r3 = repository.append(record(3, CUSTOMER, AuditAction.ORDER_CANCELLED, AuditEntityType.ORDER, "o2"));

            assertEquals(List.of(r1, r3), repository.findByAction(AuditAction.ORDER_CANCELLED));
        }

        @Test
        void findByAction_null_throwsValidation() {
            assertThrows(ValidationException.class, () -> repository.findByAction(null));
        }

        @Test
        void queries_results_areUnmodifiable() {
            repository.append(record(1));

            assertAll(
                    () -> assertThrows(UnsupportedOperationException.class,
                            () -> repository.findByEntity(AuditEntityType.PRODUCT, "PRD-1001").clear()),
                    () -> assertThrows(UnsupportedOperationException.class,
                            () -> repository.findByActor(ADMIN).clear()),
                    () -> assertThrows(UnsupportedOperationException.class,
                            () -> repository.findByAction(AuditAction.PRODUCT_CREATED).clear()));
        }
    }

    @Nested
    class Count {

        @Test
        void count_emptyRepository_isZero() {
            assertEquals(0, repository.count());
        }

        @Test
        void count_afterThreeAppends_isThree() {
            repository.append(record(1));
            repository.append(record(2));
            repository.append(record(3));

            assertEquals(3, repository.count());
        }
    }

    @Nested
    class AppendOnlyGuard {

        private final Set<String> forbiddenPrefixes = Set.of(
                "update", "delete", "remove", "clear", "set", "put", "replace", "save", "edit", "modify", "purge", "reset");

        private boolean startsWithForbidden(String name) {
            String lower = name.toLowerCase(java.util.Locale.ROOT);
            return forbiddenPrefixes.stream().anyMatch(lower::startsWith);
        }

        @Test
        @DisplayName("BR-AU01: the AuditRepository interface exposes exactly the append-only operations")
        void interface_methods_areExactlyTheAppendOnlySet() {
            Set<String> names = new TreeSet<>();
            for (Method m : AuditRepository.class.getDeclaredMethods()) {
                names.add(m.getName());
            }

            assertEquals(new TreeSet<>(Set.of("append", "findAll", "findByEntity", "findByActor",
                    "findByAction", "count")), names);
        }

        @Test
        @DisplayName("BR-AU01: no update/delete-style method exists on the interface")
        void interface_methods_haveNoMutatingNames() {
            Arrays.stream(AuditRepository.class.getMethods())
                    .forEach(m -> assertFalse(startsWithForbidden(m.getName()), "forbidden method " + m.getName()));
        }

        @Test
        @DisplayName("BR-AU01: no update/delete-style method exists on the implementation")
        void implementation_methods_haveNoMutatingNames() {
            Arrays.stream(InMemoryAuditRepository.class.getDeclaredMethods())
                    .filter(m -> !m.isSynthetic())
                    .forEach(m -> assertFalse(startsWithForbidden(m.getName()), "forbidden method " + m.getName()));
        }

        @Test
        void implementation_publicMethods_areOnlyTheInterfaceOnes() {
            Set<String> allowed = new TreeSet<>(Set.of("append", "findAll", "findByEntity", "findByActor",
                    "findByAction", "count"));
            Set<String> actual = new TreeSet<>();
            for (Method m : InMemoryAuditRepository.class.getDeclaredMethods()) {
                if (java.lang.reflect.Modifier.isPublic(m.getModifiers()) && !m.isSynthetic()) {
                    actual.add(m.getName());
                }
            }

            assertEquals(allowed, actual);
        }
    }
}