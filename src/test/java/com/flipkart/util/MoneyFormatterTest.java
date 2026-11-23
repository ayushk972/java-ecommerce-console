package com.flipkart.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MoneyFormatterTest {

    @Test
    void shouldFormatZeroAndSmallAmounts() {
        assertEquals("Rs. 0.00", MoneyFormatter.format(BigDecimal.ZERO));
        assertEquals("Rs. 99.50", MoneyFormatter.format(new BigDecimal("99.5")));
        assertEquals("Rs. 999.99", MoneyFormatter.format(new BigDecimal("999.99")));
    }

    @Test
    void shouldFormatIndianCommaGrouping() {
        assertEquals("Rs. 1,250.00", MoneyFormatter.format(new BigDecimal("1250")));
        assertEquals("Rs. 12,345.60", MoneyFormatter.format(new BigDecimal("12345.6")));
        assertEquals("Rs. 1,23,456.78", MoneyFormatter.format(new BigDecimal("123456.78")));
        assertEquals("Rs. 12,34,56,789.00", MoneyFormatter.format(new BigDecimal("123456789")));
    }

    @Test
    void shouldRoundHalfUpToTwoDecimalPlaces() {
        assertEquals("Rs. 10.13", MoneyFormatter.format(new BigDecimal("10.125")));
        assertEquals("Rs. 10.12", MoneyFormatter.format(new BigDecimal("10.124")));
    }

    @Test
    void shouldFormatNegativeAmountsAndDoubleOverload() {
        assertEquals("-Rs. 2,500.00", MoneyFormatter.format(new BigDecimal("-2500")));
        assertEquals("Rs. 1,499.00", MoneyFormatter.format(1499.0));
    }

    @Test
    void shouldRejectNullOrNonFiniteAmounts() {
        assertThrows(IllegalArgumentException.class, () -> MoneyFormatter.format(null));
        assertThrows(IllegalArgumentException.class, () -> MoneyFormatter.format(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> MoneyFormatter.format(Double.POSITIVE_INFINITY));
    }
}