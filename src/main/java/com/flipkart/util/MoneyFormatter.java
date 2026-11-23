package com.flipkart.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public final class MoneyFormatter {

    public static final String CURRENCY_PREFIX = "Rs. ";

    private MoneyFormatter() {
    }

    public static String format(BigDecimal amount) {
        if (amount == null) {
            throw new IllegalArgumentException("Amount must not be null");
        }
        BigDecimal scaled = amount.setScale(2, RoundingMode.HALF_UP);
        boolean negative = scaled.signum() < 0;
        BigDecimal abs = scaled.abs();

        String plain = abs.toPlainString();
        int dotIndex = plain.indexOf('.');
        String integerPart = plain.substring(0, dotIndex);
        String fractionPart = plain.substring(dotIndex);

        String groupedInteger = groupIndianDigits(integerPart);
        String prefix = negative ? "-" + CURRENCY_PREFIX : CURRENCY_PREFIX;
        return prefix + groupedInteger + fractionPart;
    }

    public static String format(double amount) {
        if (Double.isNaN(amount) || Double.isInfinite(amount)) {
            throw new IllegalArgumentException("Amount must be a finite number");
        }
        return format(BigDecimal.valueOf(amount));
    }

    private static String groupIndianDigits(String digits) {
        int len = digits.length();
        if (len <= 3) {
            return digits;
        }
        String lastThree = digits.substring(len - 3);
        String remaining = digits.substring(0, len - 3);

        StringBuilder sb = new StringBuilder();
        int remLen = remaining.length();
        int firstGroupLen = remLen % 2 == 0 ? 2 : 1;
        sb.append(remaining, 0, firstGroupLen);

        for (int i = firstGroupLen; i < remLen; i += 2) {
            sb.append(',').append(remaining, i, i + 2);
        }
        sb.append(',').append(lastThree);
        return sb.toString();
    }
}