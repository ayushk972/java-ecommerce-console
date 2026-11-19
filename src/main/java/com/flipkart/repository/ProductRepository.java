package com.flipkart.repository;

import com.flipkart.model.Product;

import java.util.List;
import java.util.Optional;

/**
 * Store for {@link Product}s.
 *
 * <p><b>No delete by design (A-04).</b> A product is never physically removed;
 * "deleting" a product means moving it to status DISCONTINUED. This keeps order
 * history, stock restoration on cancellation and audit trails intact. This
 * contract therefore has no delete or remove method, and none may be added.
 *
 * <p>Implementations contain no business rules and never expose their internal
 * collections (G9): every list returned is an unmodifiable snapshot.
 */

/**
 * @author nitin
 */
public interface ProductRepository {

    /**
     * Inserts the product, or replaces the stored product with the same id.
     * A replaced product keeps its original position in {@link #findAll()}.
     *
     * @param p the product to store
     * @return the stored product
     * @throws NullPointerException if the product or its id is null
     */
    Product save(Product p);

    /**
     * @param id the product id
     * @return the product, or empty if no product has that id
     * @throws NullPointerException if the id is null
     */
    Optional<Product> findById(String id);

    /** @return an unmodifiable snapshot of all products in insertion order */
    List<Product> findAll();

    /**
     * Finds every product whose normalised name equals the given normalised name,
     * whatever its status (DISCONTINUED products are included).
     *
     * @param normalizedName the name to match; it is normalised again, so
     *                       case and extra whitespace do not matter
     * @return an unmodifiable list of all matches in insertion order, possibly empty
     */
    List<Product> findAllByNormalizedName(String normalizedName);

    /** @return the number of stored products */
    long count();
}