/**
 * @author Aaditya Kumar
 * 
 * Usecase:
 * 	we will use ValidationException when user/admin provides wrong input.
 * 	Example:
 * 		- Blank Keyword
 * 		- < 10 digit mobile numbers
 * 		- name is too short
 */

package com.flipkart.exception;

import java.util.List;

public class ValidationException extends ECommerceException {
	private final List<String> errors;

	public ValidationException(List<String> errors) {
		super(join(errors));
		this.errors = List.copyOf(errors);
	}
	
	public List<String> getErrors() {
		return errors;
	}
	
	/**
	 * join() is a helper method.
	 * it must be runs before super(...) and checks the list first, it is the reason of static
	 * 
	 */
	private static String join(List<String> errors) {
		if (errors == null || errors.isEmpty()) {
			throw new IllegalArgumentException("errors must not be null or empty");
		}
		return String.join(";", errors);
	}
	
}
