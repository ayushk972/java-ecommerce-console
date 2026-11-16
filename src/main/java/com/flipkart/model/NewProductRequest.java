package com.flipkart.model;

import java.math.BigDecimal;

public record NewProductRequest(
        String name,
        String description,
        String category,
        BigDecimal price,
        int initialStock
) {
}