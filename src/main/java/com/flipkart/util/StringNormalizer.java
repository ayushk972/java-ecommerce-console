package com.flipkart.util;

import java.util.Locale;

public class StringNormalizer {

    public static String normalize(String value) {
        if (value == null) {
            return "";
        }

        return collapseWhitespace(value).toLowerCase(Locale.ROOT);
    }

    public static String collapseWhitespace(String value) {
        if (value == null) {
            return "";
        }

        return value.trim().replaceAll("\\s+", " ");
    }

    public static boolean containsNormalized(String haystack, String normalizedNeedle) {
        if (haystack == null || normalizedNeedle == null) {
            return false;
        }

        return normalize(haystack).contains(normalize(normalizedNeedle));
    }
}