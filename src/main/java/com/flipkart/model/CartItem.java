package com.flipkart.model;



/**
 * Baseline entity shell: fields, constructor and getters only.
 * Feature stories add validation, factories and behaviour.
 */
public class CartItem {
    private final String productId;
    private final int quantity;

    public CartItem(String productId, int quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    public String getProductId() { return productId; }
    public int getQuantity() { return quantity; }
}
