/**
 * @author Aaditya Kumar
 * 
 * Root of all application exceptions.
 * Unchecked, so service layer classes will not need to throws clause.
 */

package com.flipkart.exception;

public class ECommerceException extends RuntimeException{

	public ECommerceException(String message) {
		super(message);
	}

	public ECommerceException(String message, Throwable cause) {
		super(message, cause);
	}
	
	
}
