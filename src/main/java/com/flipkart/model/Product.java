package com.flipkart.model;

import com.flipkart.exception.ValidationException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Immutable catalogue product. "Changes" return a new instance (design guideline G3).
 *
 * <p>Range validation (BR-P02) is the job of {@code ProductValidator}; this class guards
 * against nulls, normalises the price and enforces the status rules (BR-P04).
 */
public final class Product {

    private static final int PRICE_SCALE = 2;
    private static final RoundingMode PRICE_ROUNDING = RoundingMode.HALF_UP;

    private final String id;
    private final String name;
    private final String description;
    private final String category;
    private final BigDecimal price;
    private final int stockQuantity;
    private final ProductStatus status;
    private final Instant createdAt;
    private final Instant updatedAt;

    /** Baseline constructor, kept public so services can build copies (see Doc 1, D1). */
    public Product(String id, String name, String description, String category,
                   BigDecimal price, int stockQuantity, ProductStatus status,
                   Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Creates a new ACTIVE product (BR-P01, BR-P04, BR-P05).
     *
     * <p>{@code createdAt} and {@code updatedAt} both equal {@code now}; the price is
     * normalised to scale 2 with HALF_UP (10 becomes 10.00). Name and category get
     * leading/trailing whitespace trimmed and internal whitespace collapsed.
     *
     * @throws NullPointerException if id, name, category, price or now is null
     */
    public static Product create(String id, String name, String description, String category,
                                 BigDecimal price, int stockQuantity, Instant now) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(now, "now");
        return new Product(id, cleanText(name, "name"), cleanDescription(description),
                cleanText(category, "category"), normalizePrice(price), stockQuantity,
                ProductStatus.ACTIVE, now, now);
    }

    /**
     * Returns a copy with new name, description, category and price (BR-P06). Stock, status,
     * id and createdAt are preserved; updatedAt becomes {@code now}.
     */
    public Product withDetails(String name, String description, String category,
                               BigDecimal price, Instant now) {
        Objects.requireNonNull(now, "now");
        return new Product(id, cleanText(name, "name"), cleanDescription(description),
                cleanText(category, "category"), normalizePrice(price), stockQuantity,
                status, createdAt, now);
    }

    /** Returns a copy with the given stock; updatedAt becomes {@code now}. */
    public Product withStock(int stockQuantity, Instant now) {
        Objects.requireNonNull(now, "now");
        return new Product(id, name, description, category, price, stockQuantity,
                status, createdAt, now);
    }

    /**
     * Returns a copy with the given status; updatedAt becomes {@code now}.
     *
     * @throws ValidationException if the transition is not allowed by BR-P04, including a
     *         change to the same status or any change away from DISCONTINUED
     */
    public Product withStatus(ProductStatus status, Instant now) {
        Objects.requireNonNull(now, "now");
        if (status == null) {
            throw new ValidationException(List.of("Status is required"));
        }
        if (!this.status.canTransitionTo(status)) {
            String message = (this.status == status)
                    ? "Product is already " + status
                    : "Cannot change product status from " + this.status + " to " + status;
            throw new ValidationException(List.of(message));
        }
        return new Product(id, name, description, category, price, stockQuantity,
                status, createdAt, now);
    }

    /** Trimmed, whitespace-collapsed, lower-cased (Locale.ROOT) name for BR-P03 / BR-P07. */
    public String normalizedName() {
        // TODO(S1-03): replace with StringNormalizer.normalize(name) once S1-03 merges.
        return name == null ? "" : name.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public BigDecimal getPrice() { return price; }
    public int getStockQuantity() { return stockQuantity; }
    public ProductStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof Product other && Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Product{id=" + id + ", name=" + name + ", status=" + status + "}";
    }

    private static BigDecimal normalizePrice(BigDecimal price) {
        Objects.requireNonNull(price, "price");
        return price.setScale(PRICE_SCALE, PRICE_ROUNDING);
    }

    private static String cleanText(String value, String field) {
        Objects.requireNonNull(value, field);
        return value.trim().replaceAll("\\s+", " ");
    }

    private static String cleanDescription(String description) {
        return description == null ? "" : description.trim();
    }
}