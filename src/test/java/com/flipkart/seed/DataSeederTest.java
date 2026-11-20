
package com.flipkart.seed;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.flipkart.model.Product;
import com.flipkart.model.ProductStatus;
import com.flipkart.repository.ProductRepository;
import com.flipkart.util.SequentialIdGenerator;
import com.flipkart.util.StringNormalizer;

class DataSeederTest {

    private FakeProductRepository repository;
    private DataSeeder dataSeeder;

    @BeforeEach
    void setUp() {
        repository = new FakeProductRepository();

        SequentialIdGenerator idGenerator =
                new SequentialIdGenerator("PRD-", 1001, 4);

        dataSeeder = new DataSeeder(
                repository,
                idGenerator,
                java.time.Clock.systemUTC()
        );
    }

    // 1. Check that exactly 15 products are inserted
    @Test
    void shouldSeedExactly15Products() {

        int insertedCount = dataSeeder.seedProducts();

        assertEquals(15, insertedCount);
        assertEquals(15, repository.count());
    }

    // 2. Check that products belong to at least 4 categories
    @Test
    void shouldHaveAtLeastFourCategories() {

        dataSeeder.seedProducts();

        Set<String> categories = repository.findAll()
                .stream()
                .map(Product::getCategory)
                .collect(Collectors.toSet());

        assertTrue(categories.size() >= 4);
    }

    // 3. Check that product names are unique
    @Test
    void shouldHaveUniqueProductNames() {

        dataSeeder.seedProducts();

        List<String> normalizedNames = repository.findAll()
                .stream()
                .map(Product::normalizedName)
                .toList();

        Set<String> uniqueNames = normalizedNames.stream()
                .collect(Collectors.toSet());

        assertEquals(normalizedNames.size(), uniqueNames.size());
    }

    // 4. Check that at least two products have zero stock
    @Test
    void shouldHaveAtLeastTwoOutOfStockProducts() {

        dataSeeder.seedProducts();

        long outOfStockCount = repository.findAll()
                .stream()
                .filter(p -> p.getStockQuantity() == 0)
                .count();

        assertTrue(outOfStockCount >= 2);
    }

    // 5. Check that at least one product has stock between 1 and 5
    @Test
    void shouldHaveAtLeastOneLowStockProduct() {

        dataSeeder.seedProducts();

        long lowStockCount = repository.findAll()
                .stream()
                .filter(p -> p.getStockQuantity() >= 1
                        && p.getStockQuantity() <= 5)
                .count();

        assertTrue(lowStockCount >= 1);
    }

    // 6. Check that exactly one product is HIDDEN
    @Test
    void shouldHaveExactlyOneHiddenProduct() {

        dataSeeder.seedProducts();

        long hiddenCount = repository.findAll()
                .stream()
                .filter(p -> p.getStatus() == ProductStatus.HIDDEN)
                .count();

        assertEquals(1, hiddenCount);
    }

    // 7. Check generated product IDs
    @Test
    void shouldGenerateSequentialProductIds() {

        dataSeeder.seedProducts();

        List<String> actualIds = repository.findAll()
                .stream()
                .map(Product::getId)
                .toList();

        List<String> expectedIds = new ArrayList<>();

        for (int i = 1001; i <= 1015; i++) {
            expectedIds.add("PRD-" + i);
        }

        assertEquals(expectedIds, actualIds);
    }

    // 8. Check idempotency: second call should insert nothing
    @Test
    void shouldNotInsertDuplicateProductsOnSecondRun() {

        int firstRun = dataSeeder.seedProducts();
        int secondRun = dataSeeder.seedProducts();

        assertEquals(15, firstRun);
        assertEquals(0, secondRun);
        assertEquals(15, repository.count());
    }

    // 9. Fake repository for testing
    private static class FakeProductRepository
            implements ProductRepository {

        private final Map<String, Product> products =
                new LinkedHashMap<>();

        @Override
        public Product save(Product product) {
            products.put(product.getId(), product);
            return product;
        }

        @Override
        public Optional<Product> findById(String id) {
            return Optional.ofNullable(products.get(id));
        }

        @Override
        public List<Product> findAll() {
            return List.copyOf(products.values());
        }

//        @Override
//        public List<Product> findAllByNormalizedName(
//                String normalizedName) {
//
//            return products.values()
//                    .stream()
//                    .filter(p -> p.normalizedName()
//                            .equals(normalizedName))
//                    .toList();
//        }
        
        @Override
        public List<Product> findAllByNormalizedName(String normalizedName) {

            String normalized = StringNormalizer.normalize(normalizedName);

            return products.values()
                    .stream()
                    .filter(p -> p.normalizedName().equals(normalized))
                    .toList();
        }

        @Override
        public long count() {
            return products.size();
        }
    }
}
