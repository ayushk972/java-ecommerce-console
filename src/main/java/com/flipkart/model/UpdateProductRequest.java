package com.flipkart.model;

import java.math.BigDecimal;

public record UpdateProductRequest(
        String name,
        String description,
        String category,
        BigDecimal price
) {
}