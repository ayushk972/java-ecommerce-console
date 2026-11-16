package com.flipkart.model;

/** Request/result contract from Overall Project Workflow Section 6.3. */
public record CartLineView(String productId, String productName, java.math.BigDecimal unitPrice, int quantity, java.math.BigDecimal lineTotal, CartIssue issue) {}
