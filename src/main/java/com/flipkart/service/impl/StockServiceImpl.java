/**
 * @author Aaditya Kumar
 */
package com.flipkart.service.impl;

import com.flipkart.exception.AuthorizationException;
import com.flipkart.exception.InsufficientStockException;
import com.flipkart.exception.ResourceNotFoundException;
import com.flipkart.exception.ValidationException;
import com.flipkart.model.Actor;
import com.flipkart.model.ActorType;
import com.flipkart.model.AuditAction;
import com.flipkart.model.AuditEntityType;
import com.flipkart.model.AuditOutcome;
import com.flipkart.model.Product;
import com.flipkart.model.ProductStatus;
import com.flipkart.model.StockRequest;
import com.flipkart.repository.ProductRepository;
import com.flipkart.service.AuditService;
import com.flipkart.service.StockService;
import com.flipkart.util.ErrorMessages;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Stock rules BR-S01..S06. Stock is an int between 0 and 1,000,000. Products change
 * only through {@code Product.withStock} and the repository save.
 *
 * @author Aaditya Kumar
 */
public class StockServiceImpl implements StockService {

    static final int MAX_STOCK = 1_000_000;
    static final int MAX_REASON_LENGTH = 100;

    private final ProductRepository productRepository;
    private final AuditService auditService;
    private final Clock clock;

    public StockServiceImpl(ProductRepository productRepository, AuditService auditService, Clock clock) {
        this.productRepository = Objects.requireNonNull(productRepository, "productRepository");
        this.auditService = Objects.requireNonNull(auditService, "auditService");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    /** One planned stock change: the product as stored now, the quantity moved, the new stock. */
    private record Change(Product original, int quantity, int newStock) { }

    @Override
    public void increaseStock(String productId, int quantity, Actor admin, String reason) {
        changeStock(productId, quantity, admin, reason, true);
    }

    @Override
    public void decreaseStock(String productId, int quantity, Actor admin, String reason) {
        changeStock(productId, quantity, admin, reason, false);
    }

    @Override
    public void reserveAll(List<StockRequest> requests, Actor actor, String reference) {
        Map<String, Integer> totals = validateAndAggregate(requests, actor, reference);

        List<Change> changes = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : totals.entrySet()) {
            Product product = load(entry.getKey());
            if (product.getStatus() != ProductStatus.ACTIVE) {
                throw new ValidationException(List.of("Product " + product.getId() + " is not available"));
            }
            int before = product.getStockQuantity();
            if (entry.getValue() > before) {
                throw new InsufficientStockException(product.getId(), entry.getValue(), before);
            }
            changes.add(new Change(product, entry.getValue(), before - entry.getValue()));
        }

        applyAll(changes);
        auditAll(actor, AuditAction.STOCK_RESERVED, changes, reference);
    }

    @Override
    public void releaseAll(List<StockRequest> requests, Actor actor, String reference) {
        Map<String, Integer> totals = validateAndAggregate(requests, actor, reference);

        List<Change> changes = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : totals.entrySet()) {
            Product product = load(entry.getKey());          // any status is allowed (BR-S04)
            int before = product.getStockQuantity();
            if (entry.getValue() > MAX_STOCK - before) {
                throw new ValidationException(List.of(
                        "Stock for " + product.getId() + " cannot exceed " + MAX_STOCK));
            }
            changes.add(new Change(product, entry.getValue(), before + entry.getValue()));
        }

        applyAll(changes);
        auditAll(actor, AuditAction.STOCK_RELEASED, changes, reference);
    }

    // ---------- admin increase / decrease ----------

    private void changeStock(String productId, int quantity, Actor admin, String reason, boolean increase) {
        requireAdmin(admin);
        validateAdminInput(productId, quantity, reason);

        Product product = load(productId);
        if (product.getStatus() == ProductStatus.DISCONTINUED) {
            throw new ValidationException(List.of("Stock cannot be changed for a discontinued product"));
        }

        int before = product.getStockQuantity();
        int after;
        if (increase) {
            if (quantity > MAX_STOCK - before) {           // checked before adding: no overflow
                throw new ValidationException(List.of("Stock cannot exceed " + MAX_STOCK));
            }
            after = before + quantity;
        } else {
            if (quantity > before) {
                throw new InsufficientStockException(productId, quantity, before);
            }
            after = before - quantity;
        }

        productRepository.save(product.withStock(after, clock.instant()));
        auditService.record(admin,
                increase ? AuditAction.STOCK_INCREASED : AuditAction.STOCK_DECREASED,
                AuditEntityType.PRODUCT, productId, AuditOutcome.SUCCESS,
                "quantity=" + quantity + ", before=" + before + ", after=" + after
                        + ", reason=" + reason.trim());
    }

    private static void requireAdmin(Actor actor) {
        if (actor == null || actor.type() != ActorType.ADMIN) {
            throw new AuthorizationException(ErrorMessages.ACCESS_DENIED);
        }
    }

    private static void validateAdminInput(String productId, int quantity, String reason) {
        List<String> errors = new ArrayList<>();
        if (isBlank(productId)) {
            errors.add("Product ID is required");
        }
        if (quantity <= 0) {
            errors.add("Quantity must be a positive number");
        }
        if (isBlank(reason)) {
            errors.add("Reason is required");
        } else if (reason.trim().length() > MAX_REASON_LENGTH) {
            errors.add("Reason must be at most " + MAX_REASON_LENGTH + " characters");
        }
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }

    // ---------- reserve / release helpers ----------

    private static Map<String, Integer> validateAndAggregate(List<StockRequest> requests,
                                                             Actor actor, String reference) {
        Objects.requireNonNull(actor, "actor");
        List<String> errors = new ArrayList<>();
        if (isBlank(reference)) {
            errors.add("Reference is required");
        }
        if (requests == null || requests.isEmpty()) {
            errors.add("At least one stock request is required");
        } else {
            for (StockRequest request : requests) {
                if (request == null) {
                    errors.add("Stock request must not be null");
                } else {
                    if (isBlank(request.productId())) {
                        errors.add("Product ID is required");
                    }
                    if (request.quantity() <= 0) {
                        errors.add("Quantity must be positive for product " + request.productId());
                    }
                }
            }
        }
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }

        Map<String, Integer> totals = new LinkedHashMap<>();
        try {
            for (StockRequest request : requests) {
                totals.merge(request.productId(), request.quantity(), (a, b) -> Math.addExact(a, b));
            }
        } catch (ArithmeticException overflow) {
            throw new ValidationException(List.of("Total quantity is too large"));
        }
        return totals;
    }

    /** Saves every change; if one save fails, the ones already saved are put back. */
    private void applyAll(List<Change> changes) {
        Instant now = clock.instant();
        List<Change> applied = new ArrayList<>();
        try {
            for (Change change : changes) {
                productRepository.save(change.original().withStock(change.newStock(), now));
                applied.add(change);
            }
        } catch (RuntimeException failure) {
            for (int i = applied.size() - 1; i >= 0; i--) {
                try {
                    productRepository.save(applied.get(i).original());
                } catch (RuntimeException rollbackFailure) {
                    failure.addSuppressed(rollbackFailure);
                }
            }
            throw failure;
        }
    }

    private void auditAll(Actor actor, AuditAction action, List<Change> changes, String reference) {
        for (Change change : changes) {
            auditService.record(actor, action, AuditEntityType.PRODUCT,
                    change.original().getId(), AuditOutcome.SUCCESS,
                    "reference=" + reference.trim() + ", quantity=" + change.quantity()
                            + ", before=" + change.original().getStockQuantity()
                            + ", after=" + change.newStock());
        }
    }

    private Product load(String productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}