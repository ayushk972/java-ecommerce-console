package com.flipkart.model;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.flipkart.exception.ValidationException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Locale;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ProductTest {

    private static final Instant T0 = Instant.parse("2026-11-10T09:00:00Z");
    private static final Instant T1 = Instant.parse("2026-11-11T09:00:00Z");

    private static Product newProduct() {
        return Product.create("PRD-1001", "Blue Mug", "Ceramic", "Kitchen",
                new BigDecimal("10"), 5, T0);
    }

    @Nested
    class Create {
        @Test
        @DisplayName("AC3: sets ACTIVE and createdAt = updatedAt = now")
        void create_validInput_setsActiveAndTimestamps() {
            Product p = newProduct();

            assertAll(
                    () -> assertEquals(ProductStatus.ACTIVE, p.getStatus()),
                    () -> assertEquals(T0, p.getCreatedAt()),
                    () -> assertEquals(T0, p.getUpdatedAt()),
                    () -> assertEquals("PRD-1001", p.getId()),
                    () -> assertEquals(5, p.getStockQuantity()));
        }

        @ParameterizedTest(name = "{0} becomes {1}")
        @CsvSource({"10,10.00", "10.5,10.50", "0.01,0.01", "10000000,10000000.00",
                "1.005,1.01", "1.004,1.00"})
        @DisplayName("AC3: price normalised to scale 2 HALF_UP")
        void create_price_isScaleTwoHalfUp(String input, String expected) {
            Product p = Product.create("PRD-1", "Ab", "", "Cd", new BigDecimal(input), 0, T0);

            assertEquals(new BigDecimal(expected), p.getPrice());
            assertEquals(2, p.getPrice().scale());
        }

        @Test
        @DisplayName("BR-P02: name and category are trimmed and whitespace-collapsed")
        void create_messyNameAndCategory_areCleaned() {
            Product p = Product.create("PRD-1", "  Blue   Mug ", null, " Home \t Kitchen ",
                    BigDecimal.ONE, 0, T0);

            assertAll(
                    () -> assertEquals("Blue Mug", p.getName()),
                    () -> assertEquals("Home Kitchen", p.getCategory()),
                    () -> assertEquals("", p.getDescription()));
        }

        @Test
        void create_nullRequiredArguments_throwNullPointerException() {
            BigDecimal one = BigDecimal.ONE;
            assertAll(
                    () -> assertThrows(NullPointerException.class,
                            () -> Product.create(null, "Ab", "", "Cd", one, 0, T0)),
                    () -> assertThrows(NullPointerException.class,
                            () -> Product.create("PRD-1", null, "", "Cd", one, 0, T0)),
                    () -> assertThrows(NullPointerException.class,
                            () -> Product.create("PRD-1", "Ab", "", null, one, 0, T0)),
                    () -> assertThrows(NullPointerException.class,
                            () -> Product.create("PRD-1", "Ab", "", "Cd", null, 0, T0)),
                    () -> assertThrows(NullPointerException.class,
                            () -> Product.create("PRD-1", "Ab", "", "Cd", one, 0, null)));
        }
    }

    @Nested
    class WithMethods {
        @Test
        @DisplayName("AC4: withDetails returns a new instance, original unchanged")
        void withDetails_returnsNewInstanceAndKeepsOriginal() {
            Product original = newProduct();

            Product changed = original.withDetails("Red Mug", "Glass", "Home",
                    new BigDecimal("12.5"), T1);

            assertAll(
                    () -> assertNotSame(original, changed),
                    () -> assertEquals("Red Mug", changed.getName()),
                    () -> assertEquals("Glass", changed.getDescription()),
                    () -> assertEquals("Home", changed.getCategory()),
                    () -> assertEquals(new BigDecimal("12.50"), changed.getPrice()),
                    () -> assertEquals(T1, changed.getUpdatedAt()),
                    () -> assertEquals(T0, changed.getCreatedAt()),
                    () -> assertEquals(original.getStockQuantity(), changed.getStockQuantity()),
                    () -> assertEquals(original.getStatus(), changed.getStatus()),
                    () -> assertEquals("Blue Mug", original.getName()),
                    () -> assertEquals(new BigDecimal("10.00"), original.getPrice()),
                    () -> assertEquals(T0, original.getUpdatedAt()));
        }

        @Test
        @DisplayName("AC4: withStock returns a new instance, original unchanged")
        void withStock_returnsNewInstanceAndKeepsOriginal() {
            Product original = newProduct();

            Product changed = original.withStock(42, T1);

            assertAll(
                    () -> assertNotSame(original, changed),
                    () -> assertEquals(42, changed.getStockQuantity()),
                    () -> assertEquals(T1, changed.getUpdatedAt()),
                    () -> assertEquals(T0, changed.getCreatedAt()),
                    () -> assertEquals(5, original.getStockQuantity()),
                    () -> assertEquals(T0, original.getUpdatedAt()));
        }

        @Test
        @DisplayName("AC4: withStatus returns a new instance, original unchanged")
        void withStatus_allowedTransition_returnsNewInstanceAndKeepsOriginal() {
            Product original = newProduct();

            Product changed = original.withStatus(ProductStatus.HIDDEN, T1);

            assertAll(
                    () -> assertNotSame(original, changed),
                    () -> assertEquals(ProductStatus.HIDDEN, changed.getStatus()),
                    () -> assertEquals(T1, changed.getUpdatedAt()),
                    () -> assertEquals(T0, changed.getCreatedAt()),
                    () -> assertEquals(ProductStatus.ACTIVE, original.getStatus()),
                    () -> assertEquals(T0, original.getUpdatedAt()));
        }
    }

    @Nested
    class StatusRules {
        private Product inStatus(ProductStatus status) {
            return new Product("PRD-1", "Ab", "", "Cd", new BigDecimal("1.00"), 0, status, T0, T0);
        }

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
        @DisplayName("AC5 / BR-P04: withStatus enforces the transition table")
        void withStatus_allPairs_allowedOrValidationException(ProductStatus from,
                                                              ProductStatus to,
                                                              boolean allowed) {
            Product p = inStatus(from);

            if (allowed) {
                assertEquals(to, p.withStatus(to, T1).getStatus());
            } else {
                ValidationException ex = assertThrows(ValidationException.class,
                        () -> p.withStatus(to, T1));
                assertEquals(1, ex.getErrors().size());
            }
        }

        @Test
        void withStatus_nullStatus_throwsValidationException() {
            assertThrows(ValidationException.class, () -> newProduct().withStatus(null, T1));
        }
    }

    @Nested
    class Identity {
        @Test
        @DisplayName("AC6: normalizedName is trimmed, collapsed and lower-cased")
        void normalizedName_mixedCase_isNormalised() {
            Product p = Product.create("PRD-1", "  BLUE   Mug ", "", "Cd", BigDecimal.ONE, 0, T0);

            assertEquals("blue mug", p.normalizedName());
        }

        @Test
        @DisplayName("AC6: normalizedName uses Locale.ROOT, not the default locale")
        void normalizedName_turkishDefaultLocale_stillAscii() {
            Locale previous = Locale.getDefault();
            try {
                Locale.setDefault(Locale.forLanguageTag("tr-TR"));
                Product p = Product.create("PRD-1", "TITLE", "", "Cd", BigDecimal.ONE, 0, T0);

                assertEquals("title", p.normalizedName());
            } finally {
                Locale.setDefault(previous);
            }
        }

        @Test
        @DisplayName("AC7: equals and hashCode use id only")
        void equalsAndHashCode_sameIdDifferentFields_areEqual() {
            Product a = newProduct();
            Product b = a.withDetails("Other", "x", "Other", new BigDecimal("99"), T1)
                    .withStock(1, T1);
            Product differentId = Product.create("PRD-1002", "Blue Mug", "Ceramic", "Kitchen",
                    new BigDecimal("10"), 5, T0);

            assertAll(
                    () -> assertEquals(a, b),
                    () -> assertEquals(a.hashCode(), b.hashCode()),
                    () -> assertNotEquals(a, differentId),
                    () -> assertNotEquals(a, null),
                    () -> assertNotEquals(a, "PRD-1001"),
                    () -> assertTrue(a.equals(a)));
        }

        @Test
        @DisplayName("AC7: toString lists id, name and status only")
        void toString_containsIdNameStatus() {
            String s = newProduct().toString();

            assertAll(
                    () -> assertTrue(s.contains("PRD-1001")),
                    () -> assertTrue(s.contains("Blue Mug")),
                    () -> assertTrue(s.contains("ACTIVE")),
                    () -> assertFalse(s.contains("Ceramic")),
                    () -> assertFalse(s.contains("10.00")));
        }
    }
}
