package com.flipkart.util;

import java.util.ArrayList;
import java.util.List;

/** 
 * 
 * @author daksh
 * 
 */


import com.flipkart.exception.ValidationException;
import com.flipkart.model.RegistrationRequest;

/**
 * Validates customer registration input.
 *
 * All registration errors are collected and reported together.
 */
public final class CustomerValidator {

    private CustomerValidator() {
        // Utility class
    }

    /**
     * Validates registration details.
     *
     * @throws ValidationException when one or more fields are invalid
     */
    public static void validateRegistration(
            RegistrationRequest request) {

        List<String> errors = new ArrayList<>();

        if (request == null) {
            errors.add("Registration request must not be null");
            throw new ValidationException(errors);
        }

        String name = request.name();
        String mobile = request.mobile();
        String password = request.password();

        validateName(name, errors);
        validateMobile(mobile, errors);
        validatePassword(password, errors);

        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }

    /**
     * Trims the name and replaces every sequence of whitespace
     * characters with a single ordinary space.
     */
    public static String normalizeName(String name) {
        if (name == null) {
            return "";
        }

        return name.trim().replaceAll("\\s+", " ");
    }

    private static void validateName(
            String name,
            List<String> errors) {

        if (name == null || name.isBlank()) {
            errors.add("Name must not be blank");
            return;
        }

        String normalized = normalizeName(name);

        if (normalized.length() < 2 || normalized.length() > 50) {
            errors.add("Name must contain 2 to 50 characters");
        }

        if (!normalized.matches("[A-Za-z .'-]+")) {
            errors.add(
                    "Name may contain only English letters, spaces, " +
                    "hyphens, apostrophes and dots");
        }

        long letterCount = normalized.chars()
                .filter(Character::isLetter)
                .count();

        if (letterCount < 2) {
            errors.add("Name must contain at least 2 letters");
        }
    }

    private static void validateMobile(
            String mobile,
            List<String> errors) {

        if (mobile == null || !mobile.matches("[6-9]\\d{9}")) {
            errors.add(
                    "Mobile must contain exactly 10 digits " +
                    "and start with 6, 7, 8 or 9");
        }
    }

    private static void validatePassword(
            String password,
            List<String> errors) {

        if (password == null || password.isBlank()) {
            errors.add("Password must not be blank");
            return;
        }

        if (password.length() < 8 || password.length() > 64) {
            errors.add("Password must contain 8 to 64 characters");
        }
    }
}
