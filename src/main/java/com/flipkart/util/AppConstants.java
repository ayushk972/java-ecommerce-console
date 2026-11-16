package com.flipkart.util;

import java.math.BigDecimal;

/**
 * Shared constants contract.
 *
 * UNSPECIFIED: workflow names MAX_STOCK, MAX_LINE_QUANTITY, MAX_PRICE and
 * name/password length limits but does not provide the complete exact list
 * of identifiers/values. Values below reflect explicit business-rule limits
 * where available; confirm identifiers with Product Owner before use.
 */
public final class AppConstants {
	
    private AppConstants() {}

    public static final int PBKDF2_ITERATIONS = 600_000;
    public static final int MAX_STOCK = 1_000_000;          // A-10 / BR-S01
    public static final int MAX_LINE_QUANTITY = 99;         // A-10
    public static final BigDecimal MAX_PRICE = new BigDecimal("10000000.00"); // A-10 / BR-P02
    public static final int MIN_CUSTOMER_NAME_LENGTH = 2;   // BR-A02
    public static final int MAX_CUSTOMER_NAME_LENGTH = 50;  // BR-A02
    public static final int MIN_PASSWORD_LENGTH = 8;        // BR-A04
    public static final int MAX_PASSWORD_LENGTH = 64;       // BR-A04
    public static final int CUSTOMER_LOGIN_ID_LENGTH = 7;   // BR-A05
}
