package com.flipkart.model;

/**
 * Lifecycle status of a product (BR-P04).
 *
 * <p>Allowed transitions: {@code ACTIVE <-> HIDDEN}, {@code ACTIVE -> DISCONTINUED},
 * {@code HIDDEN -> DISCONTINUED}. {@code DISCONTINUED} is terminal.
 */
public enum ProductStatus {
    ACTIVE,
    HIDDEN,
    DISCONTINUED;

    /**
     * Whether Guests and Customers may see (and buy) products in this status.
     *
     * @return {@code true} only for {@link #ACTIVE}
     */
    public boolean isVisibleToShoppers() {
        return this == ACTIVE;
    }

    /**
     * Whether a product in this status may move to {@code target} (BR-P04).
     * A move to the same status is not a transition and returns {@code false}.
     *
     * @param target the desired status; {@code null} returns {@code false}
     * @return {@code true} if the change is allowed
     */
    public boolean canTransitionTo(ProductStatus target) {
        if (target == null || target == this) {
            return false;
        }
        return switch (this) {
            case ACTIVE -> target == HIDDEN || target == DISCONTINUED;
            case HIDDEN -> target == ACTIVE || target == DISCONTINUED;
            case DISCONTINUED -> false;
        };
    }
}
