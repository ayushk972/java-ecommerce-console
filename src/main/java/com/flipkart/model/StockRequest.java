/**
 * @author Aaditya Kumar
 */
package com.flipkart.model;

/** One line of a stock reservation or release. */
public record StockRequest(String productId, int quantity) {
}