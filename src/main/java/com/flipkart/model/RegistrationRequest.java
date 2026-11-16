
/**
 * 
 * @author daksh
 * 
 */

package com.flipkart.model;

/**
 * Represents the details submitted by a customer during registration.
 *
 * @param name     customer's name
 * @param mobile   customer's mobile number
 * @param password customer's raw password (must be hashed before storage)
 */
public record RegistrationRequest(
        String name,
        String mobile,
        String password
) {
}
