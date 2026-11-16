package com.flipkart.model;

/** Request/result contract from Overall Project Workflow Section 6.3. */
public record MergeLineResult(String productId, String productName, int guestQuantity, int existingQuantity, int finalQuantity, MergeLineStatus status) {}
