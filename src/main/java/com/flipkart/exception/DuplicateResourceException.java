/**
 * @author Aaditya Kumar
 * 
 * Usecase:
 * 	- Mobile Number is already used by someone else
 * 	- Product Name is already existed 
 */
package com.flipkart.exception;

public class DuplicateResourceException extends ECommerceException {

	public DuplicateResourceException(String message) {
		super(message);
	}
}
