package com.flipkart.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class ProductStatusTest {

    @ParameterizedTest(name = "{0} -> {1} allowed = {2}")
    @CsvSource({
            "ACTIVE,ACTIVE,false",
            "ACTIVE,HIDDEN,true",
            "ACTIVE,DISCONTINUED,true",
            "HIDDEN,ACTIVE,true",
            "HIDDEN,HIDDEN,false",
            "HIDDEN,DISCONTINUED,true",
            "DISCONTINUED,ACTIVE,false",
            "DISCONTINUED,HIDDEN,false",
            "DISCONTINUED,DISCONTINUED,false"
    })
    @DisplayName("BR-P04: transition table")
    void canTransitionTo_allNinePairs_matchesRule(ProductStatus from, ProductStatus to,
                                                  boolean expected) {
        assertEquals(expected, from.canTransitionTo(to));
    }

    @ParameterizedTest
    @EnumSource(ProductStatus.class)
    @DisplayName("BR-P04: null target is never allowed")
    void canTransitionTo_nullTarget_returnsFalse(ProductStatus from) {
        assertFalse(from.canTransitionTo(null));
    }

    @Test
    @DisplayName("BR-P04: only ACTIVE is visible to shoppers")
    void isVisibleToShoppers_onlyActiveIsTrue() {
        assertTrue(ProductStatus.ACTIVE.isVisibleToShoppers());
        assertFalse(ProductStatus.HIDDEN.isVisibleToShoppers());
        assertFalse(ProductStatus.DISCONTINUED.isVisibleToShoppers());
    }
}
