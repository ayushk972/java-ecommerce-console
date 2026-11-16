package com.flipkart.model;

/** Request/result contract from Overall Project Workflow Section 6.3. */
public record CartView(java.util.List<CartLineView> lines, java.math.BigDecimal total, boolean hasIssues) {}
