package com.flipkart.repository.impl;

import com.flipkart.model.Product;
import com.flipkart.model.ProductStatus;
import com.flipkart.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
/**
 * @author nitin
 */
class InMemoryProductRepositoryTest {

    private static final Instant NOW = Instant.parse("2026-11-18T10:00:00Z");

    private InMemoryProductRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryProductRepository();
    }

    private static Product product(String id, String name, ProductStatus status) {
        return new Product(id, name, "description", "category", new BigDecimal("10.00"), 5, status, NOW, NOW);
    }

    private static Product product(String id, String name) {
        return product(id, name, ProductStatus.ACTIVE);
    }

    @Nested
    class Save {

        @Test
        @DisplayName("AC1: save inserts a new product and returns it")
        void save_newProduct_isStoredAndReturned() {
            Product p = product("PRD-1001", "Phone");

            assertSame(p, repository.save(p));
            assertEquals(Optional.of(p), repository.findById("PRD-1001"));
        }

        @Test
        @DisplayName("AC1: save with an existing id replaces the product")
        void save_existingId_replacesProduct() {
            repository.save(product("PRD-1001", "Phone"));
            Product replacement = product("PRD-1001", "Phone Pro");

            repository.save(replacement);

            assertAll(
                    () -> assertEquals(1, repository.count()),
                    () -> assertSame(replacement, repository.findById("PRD-1001").orElseThrow()));
        }

        @Test
        @DisplayName("AC1: insertion order is preserved when a product is replaced")
        void save_replacement_keepsOriginalPosition() {
            Product a = product("PRD-1001", "A");
            Product b = product("PRD-1002", "B");
            Product c = product("PRD-1003", "C");
            repository.save(a);
            repository.save(b);
            repository.save(c);
            Product a2 = product("PRD-1001", "A changed");

            repository.save(a2);

            assertEquals(List.of(a2, b, c), repository.findAll());
        }

        @Test
        void save_nullProduct_throwsNullPointerWithMessage() {
            NullPointerException ex = assertThrows(NullPointerException.class, () -> repository.save(null));

            assertNotNull(ex.getMessage());
        }
    }

    @Nested
    class FindById {

        @Test
        void findById_knownId_returnsProduct() {
            Product p = repository.save(product("PRD-1001", "Phone"));

            assertEquals(Optional.of(p), repository.findById("PRD-1001"));
        }

        @Test
        @DisplayName("AC2: unknown id returns Optional.empty()")
        void findById_unknownId_returnsEmpty() {
            repository.save(product("PRD-1001", "Phone"));

            assertEquals(Optional.empty(), repository.findById("PRD-9999"));
        }

        @Test
        @DisplayName("AC2: null id throws NullPointerException with a clear message")
        void findById_nullId_throwsNullPointerWithClearMessage() {
            NullPointerException ex = assertThrows(NullPointerException.class, () -> repository.findById(null));

            assertTrue(ex.getMessage() != null && ex.getMessage().contains("id"),
                    "message should mention the id but was: " + ex.getMessage());
        }
    }

    @Nested
    class FindAll {

        @Test
        void findAll_emptyRepository_returnsEmptyList() {
            assertTrue(repository.findAll().isEmpty());
        }

        @Test
        @DisplayName("AC3: findAll returns products in insertion order")
        void findAll_severalProducts_returnsInsertionOrder() {
            Product p3 = repository.save(product("PRD-1003", "C"));
            Product p1 = repository.save(product("PRD-1001", "A"));
            Product p2 = repository.save(product("PRD-1002", "B"));

            assertEquals(List.of(p3, p1, p2), repository.findAll());
        }

        @Test
        @DisplayName("AC3: findAll returns an unmodifiable list")
        void findAll_result_isUnmodifiable() {
            repository.save(product("PRD-1001", "A"));
            List<Product> snapshot = repository.findAll();

            assertAll(
                    () -> assertThrows(UnsupportedOperationException.class, () -> snapshot.add(product("PRD-1002", "B"))),
                    () -> assertThrows(UnsupportedOperationException.class, () -> snapshot.remove(0)),
                    () -> assertThrows(UnsupportedOperationException.class, snapshot::clear));
        }

        @Test
        @DisplayName("AC7: mutating the returned list does not affect the repository")
        void findAll_mutationAttempt_leavesRepositoryUnchanged() {
            Product p = repository.save(product("PRD-1001", "A"));
            List<Product> snapshot = repository.findAll();

            assertThrows(UnsupportedOperationException.class, snapshot::clear);

            assertAll(
                    () -> assertEquals(1, repository.count()),
                    () -> assertEquals(List.of(p), repository.findAll()));
        }

        @Test
        @DisplayName("AC7: an earlier snapshot is not changed by later saves")
        void findAll_snapshot_isNotAffectedByLaterSaves() {
            repository.save(product("PRD-1001", "A"));
            List<Product> snapshot = repository.findAll();

            repository.save(product("PRD-1002", "B"));

            assertAll(
                    () -> assertEquals(1, snapshot.size()),
                    () -> assertEquals(2, repository.findAll().size()));
        }
    }

    @Nested
    class FindAllByNormalizedName {

        @Test
        @DisplayName("AC4: matches stored names case-insensitively")
        void findAllByNormalizedName_differentCase_matches() {
            Product p = repository.save(product("PRD-1001", "iPhone 15"));

            assertEquals(List.of(p), repository.findAllByNormalizedName("iphone 15"));
        }

        @Test
        void findAllByNormalizedName_storedNameWithExtraSpaces_matches() {
            Product p = repository.save(product("PRD-1001", "  Blue   Shirt "));

            assertEquals(List.of(p), repository.findAllByNormalizedName("blue shirt"));
        }

        @Test
        void findAllByNormalizedName_uppercaseArgument_matches() {
            Product p = repository.save(product("PRD-1001", "Blue Shirt"));

            assertEquals(List.of(p), repository.findAllByNormalizedName("BLUE SHIRT"));
        }

        @Test
        @DisplayName("AC4: returns ALL matches including DISCONTINUED, in insertion order")
        void findAllByNormalizedName_severalMatches_returnsAllIncludingDiscontinued() {
            Product active = repository.save(product("PRD-1001", "Mug", ProductStatus.ACTIVE));
            repository.save(product("PRD-1002", "Plate"));
            Product discontinued = repository.save(product("PRD-1003", "MUG", ProductStatus.DISCONTINUED));
            Product hidden = repository.save(product("PRD-1004", "mug", ProductStatus.HIDDEN));

            assertEquals(List.of(active, discontinued, hidden), repository.findAllByNormalizedName("mug"));
        }

        @Test
        void findAllByNormalizedName_noMatch_returnsEmptyList() {
            repository.save(product("PRD-1001", "Mug"));

            assertTrue(repository.findAllByNormalizedName("cup").isEmpty());
        }

        @Test
        void findAllByNormalizedName_partialName_doesNotMatch() {
            repository.save(product("PRD-1001", "Coffee Mug"));

            assertTrue(repository.findAllByNormalizedName("mug").isEmpty());
        }

        @Test
        void findAllByNormalizedName_result_isUnmodifiable() {
            repository.save(product("PRD-1001", "Mug"));

            List<Product> result = repository.findAllByNormalizedName("mug");

            assertThrows(UnsupportedOperationException.class, result::clear);
        }
    }

    @Nested
    class Count {

        @Test
        @DisplayName("AC5: count is zero for an empty repository")
        void count_empty_isZero() {
            assertEquals(0, repository.count());
        }

        @Test
        @DisplayName("AC5: count reflects stored products, not save calls")
        void count_afterInsertsAndReplacement_countsDistinctIds() {
            repository.save(product("PRD-1001", "A"));
            repository.save(product("PRD-1002", "B"));
            repository.save(product("PRD-1001", "A again"));

            assertEquals(2, repository.count());
        }
    }

    @Nested
    class NoDeleteByDesign {

        private boolean isDeleteLike(String name) {
            String lower = name.toLowerCase(java.util.Locale.ROOT);
            return lower.startsWith("delete") || lower.startsWith("remove") || lower.startsWith("clear");
        }

        @Test
        @DisplayName("AC6: ProductRepository declares no delete/remove method")
        void interface_hasNoDeleteOrRemoveMethod() {
            for (Method m : ProductRepository.class.getMethods()) {
                assertFalse(isDeleteLike(m.getName()), "forbidden method on interface: " + m.getName());
            }
        }

        @Test
        @DisplayName("AC6: InMemoryProductRepository declares no delete/remove method")
        void implementation_hasNoDeleteOrRemoveMethod() {
            for (Method m : InMemoryProductRepository.class.getDeclaredMethods()) {
                if (!m.isSynthetic()) {
                    assertFalse(isDeleteLike(m.getName()), "forbidden method on implementation: " + m.getName());
                }
            }
        }
    }
}