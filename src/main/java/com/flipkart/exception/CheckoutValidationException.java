package com.flipkart.exception;

import java.util.List;

import com.flipkart.model.CartIssue;

public class CheckoutValidationException extends ECommerceException {

	private final List<CartIssue> issues;

	public CheckoutValidationException(List<CartIssue> issues) {
		super("Checkout failed with " + (issues == null ? 0 : issues.size()) + " issues(s)");
		this.issues = List.copyOf(issues);
	}
	public List<CartIssue> getIssue() {
		return issues;
	}

}
