/**
 * @author Aaditya Kumar
 */
package com.flipkart.service;

import com.flipkart.model.Actor;
import com.flipkart.model.StockRequest;

import java.util.List;

/**
 * The only way stock changes (A-06), so every change is audited.
 */
public interface StockService {

    /**
     * Admin adds stock to an ACTIVE or HIDDEN product.
     *
     * @throws com.flipkart.exception.AuthorizationException if the actor is not an admin
     * @throws com.flipkart.exception.ValidationException bad quantity or reason, a discontinued
     *         product, or a result above 1,000,000
     * @throws com.flipkart.exception.ResourceNotFoundException if the product does not exist
     */
    void increaseStock(String productId, int quantity, Actor admin, String reason);

    /**
     * Admin removes stock from an ACTIVE or HIDDEN product.
     *
     * @throws com.flipkart.exception.InsufficientStockException if quantity exceeds the stock
     *         (nothing changes); the other exceptions are as for {@link #increaseStock}
     */
    void decreaseStock(String productId, int quantity, Actor admin, String reason);

    /**
     * All-or-nothing decrease used by checkout. Duplicate product ids are aggregated.
     * Every product is validated before any stock changes.
     */
    void reserveAll(List<StockRequest> requests, Actor actor, String reference);

    /** Restores stock (cancellation). Works for products in any status. */
    void releaseAll(List<StockRequest> requests, Actor actor, String reference);
}