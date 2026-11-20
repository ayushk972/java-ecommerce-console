
package com.flipkart.seed;

import com.flipkart.model.Product;
import com.flipkart.model.ProductStatus;
import com.flipkart.repository.ProductRepository;
import com.flipkart.util.IdGenerator;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

public class DataSeeder {

    private final ProductRepository productRepository;
    private final IdGenerator idGenerator;
    private final Clock clock;

    public DataSeeder(ProductRepository productRepository,
                      IdGenerator idGenerator,
                      Clock clock) {

        this.productRepository = Objects.requireNonNull(productRepository);
        this.idGenerator = Objects.requireNonNull(idGenerator);
        this.clock = Objects.requireNonNull(clock);
    }

    public int seedProducts() {

        int insertedCount = 0;

        for (ProductSeedData.SeedProduct seed : ProductSeedData.getProducts()) {

            // Skip products that already exist by normalized name
            if (!productRepository
                    .findAllByNormalizedName(seed.name())
                    .isEmpty()) {
                continue;
            }

            // Generate ID only for products being inserted
            String id = idGenerator.nextId();

            // Get current time from injected Clock
            Instant now = clock.instant();

            // Create product with ACTIVE status
            Product product = Product.create(
                    id,
                    seed.name(),
                    seed.description(),
                    seed.category(),
                    seed.price(),
                    seed.stockQuantity(),
                    now
            );

            // Change status to HIDDEN if required
            if (seed.hidden()) {
                product = product.withStatus(ProductStatus.HIDDEN, now);
            }

            // Save product
            productRepository.save(product);

            insertedCount++;
        }

        return insertedCount;
    }
}
