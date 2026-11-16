/**
 * @author Aaditya Kumar
 * 
 * Usecase:
 * 	- Stock is 5 unit and admin tries to decreases it to 8. so it throws and changes nothing
 * 	- Stock is 4, the user asks for 10, so the menu prints "Only 4 available"
 * 	
 * 
 * 	means, if the person requests for more than we have then we can use `InsufficientStockException`
 */

package com.flipkart.exception;

public class InsufficientStockException extends ECommerceException {
	private final String productId;
	private final int requested;
	private final int available;
	
	public InsufficientStockException(String productId, int requested, int available) {
		super (
			"Insufficient stock for " + productId
			+ ": requested " + requested
			+ ", available " + available
		);
		this.productId = productId;
		this.requested = requested;
		this.available = available;
	}
	
	public String getProductId() {
		return productId;
	}
	
	public int getRequested() {
		return requested;
	}

	public int getAvailable() {
		return available;
	}
	
}
