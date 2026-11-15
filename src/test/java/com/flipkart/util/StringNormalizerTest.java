package com.flipkart.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Locale;

import org.junit.jupiter.api.Test;

class StringNormalizerTest {

    @Test
    void normalize_null_returnsEmptyString() {
        assertEquals("", StringNormalizer.normalize(null));
    }

    @Test
    void normalize_emptyString_returnsEmptyString() {
        assertEquals("", StringNormalizer.normalize(""));
    }

    @Test
    void normalize_blankString_returnsEmptyString() {
        assertEquals("", StringNormalizer.normalize("   "));
    }

    @Test
    void normalize_multipleSpaces_returnsSingleSpace() {
        assertEquals("hello world",
                StringNormalizer.normalize("  Hello   WORLD "));
    }

    @Test
    void normalize_tabsAndNewLines_collapsesWhitespace() {
        assertEquals("hello world",
                StringNormalizer.normalize("Hello\t\nWorld"));
    }

    @Test
    void normalize_turkishLocale_usesRootLocale() {
        Locale originalLocale = Locale.getDefault();

        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));

            assertEquals("title", StringNormalizer.normalize("TITLE"));
        } finally {
            Locale.setDefault(originalLocale);
        }
    }

    @Test
    void collapseWhitespace_preservesCase() {
        assertEquals("Hello WORLD",
                StringNormalizer.collapseWhitespace("  Hello   WORLD "));
    }

    @Test
    void containsNormalized_matchingText_returnsTrue() {
        assertTrue(StringNormalizer.containsNormalized(
                "Laptop Pro 15", " LAPTOP "));
    }

    @Test
    void containsNormalized_nonMatchingText_returnsFalse() {
        assertFalse(StringNormalizer.containsNormalized(
                "Laptop Pro 15", "Mobile"));
    }
    @Test
    void collapseWhitespace_null_returnsEmptyString() {
        assertEquals("", StringNormalizer.collapseWhitespace(null));
    }

    @Test
    void containsNormalized_nullHaystack_returnsFalse() {
        assertFalse(StringNormalizer.containsNormalized(null, "Laptop"));
    }

    @Test
    void containsNormalized_nullNeedle_returnsFalse() {
        assertFalse(StringNormalizer.containsNormalized("Laptop Pro", null));
    }
}