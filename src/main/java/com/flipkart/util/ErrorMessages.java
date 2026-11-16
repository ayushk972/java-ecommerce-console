/**
 * @author Aaditya Kumar
 * 
 * Usecase:
 * 	1. Invalid Login ID or password (customer)
 * 	2. Invalid Admin ID or password
 * 	3. Order not found
 * 	4. Access denied
 */
package com.flipkart.util;

public class ErrorMessages {
	public static final String INVALID_LOGIN = "Invalid Login ID or password";   // BR-A06
    public static final String INVALID_ADMIN = "Invalid Admin ID or password";   // BR-A08
    public static final String ORDER_NOT_FOUND = "Order not found";              // BR-O09
    public static final String ACCESS_DENIED = "Access denied";                  // BR-A09

    private ErrorMessages() { }   // utility class, never instantiated
}
