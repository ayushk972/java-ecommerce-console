/**
 * @author Aaditya Kumar
 * 
 * Orders follow strict rules: PLACED → CONFIRMED → SHIPPED → DELIVERED, one step at a time.
 * Cancellation allowed until DELIVERED 
 * 
 * If Admin 
 * 	- skip a step such as PLACED → SHIPPED (or)
 * 	- going backward SHIPPED → CONFIRMED (or)
 * 	- cancelling a DELIVERED order
 * in these scenario, this exception will be used
 * 
 */

package com.flipkart.exception;

import com.flipkart.model.OrderStatus;

public class InvalidOrderTransitionException extends ECommerceException {
	private final OrderStatus from;
	private final OrderStatus to;

	public InvalidOrderTransitionException(OrderStatus from, OrderStatus to) {
		super("Cannot change order status from " + from + " to " + to);
		this.from = from;
		this.to = to;
	}

	public OrderStatus getFrom() {
		return from;
	}

	public OrderStatus getTo() {
		return to;
	}
}
