/**
 * @author Aaditya Kumar
 */

package com.flipkart.exception;

public class ResourceNotFoundException extends ECommerceException {

	public ResourceNotFoundException(String entity, String id) {
		super(entity + " not found: " + id);
	}
}
