/**
 * @author Aaditya Kumar
 * 
 * in Ecommerce, there is two kind of timeline:
 * 	1. Timeline-1 → Add a product into Cart
 * 	2. Timeline-2 → Place order from Cart
 * 
 * Usecase:
 * 	1. during Timeline-1 product has sufficient no. of stock which current customer needs
 *  	but may be during placing order, stock quantity got reduced → throw this exception
 *  2. At Timeline-2 product is hidden or discontinued → throw this exception
 *  3. The product no longer exists (PRODUCT_NOT_FOUND)
 */
package com.flipkart.exception;

import java.util.List;

import com.flipkart.model.CartIssue;

public class CheckoutValidationException extends ECommerceException {

	private final List<CartIssue> issues;

	public CheckoutValidationException(List<CartIssue> issues) {
		super("Checkout failed with " + (issues == null ? 0 : issues.size()) + " issues(s)");
		this.issues = List.copyOf(issues);
	}
	public List<CartIssue> getIssues() {
		return issues;
	}

}
