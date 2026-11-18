package com.flipkart.repository.impl;

import com.flipkart.model.Product;
import com.flipkart.repository.ProductRepository;
import com.flipkart.util.StringNormalizer;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * In-memory {@link ProductRepository} backed by a {@link LinkedHashMap} keyed
 * by product id.
 *
 * <p><b>No delete by design (A-04).</b> Products are never physically removed;
 * they are soft-deleted by moving to status DISCONTINUED, which keeps order
 * history, stock restoration and audit trails safe. This class therefore has no
 * delete or remove method.
 *
 * <p>Insertion order is preserved, also when a product is replaced. Every list
 * returned is an unmodifiable copy, never the internal collection (G9). The class
 * holds no business rules.
 */
/**
 * @author nitin
 */
public class InMemoryProductRepository implements ProductRepository {

    private final Map<String, Product> products = new LinkedHashMap<>();

    /**
     * Inserts a new product or replaces the one with the same id. A replacement
     * keeps the original insertion position.
     *
     * @param product the product to store
     * @return the stored product (the same instance)
     * @throws NullPointerException if the product or its id is null
     */
    @Override
    public Product save(Product product) {
        Objects.requireNonNull(product, "Product must not be null");
        String id = Objects.requireNonNull(product.getId(), "Product id must not be null");
        products.put(id, product);
        return product;
    }

    /**
     * @param id the product id
     * @return the product, or empty if unknown
     * @throws NullPointerException if the id is null
     */
    @Override
    public Optional<Product> findById(String id) {
        Objects.requireNonNull(id, "Product id must not be null");
        return Optional.ofNullable(products.get(id));
    }

    /** @return an unmodifiable snapshot of all products in insertion order */
    @Override
    public List<Product> findAll() {
        return List.copyOf(products.values());
    }

    /**
     * @param normalizedName the name to match (normalised again with StringNormalizer)
     * @return unmodifiable list of every product with that name, including
     *         DISCONTINUED ones, in insertion order
     */
    @Override
    public List<Product> findAllByNormalizedName(String normalizedName) {
        String target = StringNormalizer.normalize(normalizedName);
        return products.values().stream()
                .filter(p -> StringNormalizer.normalize(p.getName()).equals(target))
                .toList();
    }

    /** @return the number of stored products */
    @Override
    public long count() {
        return products.size();
    }
}