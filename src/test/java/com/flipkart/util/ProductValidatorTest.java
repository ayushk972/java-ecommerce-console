package com.flipkart.util;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.flipkart.exception.ValidationException;
import com.flipkart.model.NewProductRequest;
import com.flipkart.model.UpdateProductRequest;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class ProductValidatorTest {

    private static final String VALID_NAME = "Blue Mug";
    private static final String VALID_CATEGORY = "Kitchen";
    private static final BigDecimal VALID_PRICE = new BigDecimal("99.99");

    private static NewProductRequest newReq(String name, String description, String category,
                                            BigDecimal price, int stock) {
        return new NewProductRequest(name, description, category, price, stock);
    }

    private static UpdateProductRequest updReq(String name, String description, String category,
                                               BigDecimal price) {
        return new UpdateProductRequest(name, description, category, price);
    }

    private static NewProductRequest validNew() {
        return newReq(VALID_NAME, "desc", VALID_CATEGORY, VALID_PRICE, 10);
    }

    @Nested
    class Name {
        @ParameterizedTest(name = "name of {0} chars valid = {1}")
        @CsvSource({"1,false", "2,true", "100,true", "101,false"})
        @DisplayName("BR-P02 / AC1: name length boundaries")
        void validateNew_nameLength_boundaries(int length, boolean valid) {
            NewProductRequest r = newReq("a".repeat(length), "", VALID_CATEGORY, VALID_PRICE, 0);

            assertOutcome(valid, () -> ProductValidator.validateNew(r), "Name");
        }

        @ParameterizedTest(name = "name of {0} chars valid = {1}")
        @CsvSource({"1,false", "2,true", "100,true", "101,false"})
        @DisplayName("BR-P02 / AC1: name length boundaries on update")
        void validateUpdate_nameLength_boundaries(int length, boolean valid) {
            UpdateProductRequest r = updReq("a".repeat(length), "", VALID_CATEGORY, VALID_PRICE);

            assertOutcome(valid, () -> ProductValidator.validateUpdate(r), "Name");
        }

        @Test
        @DisplayName("BR-P02: length is measured after trim and whitespace collapse")
        void validateNew_paddedName_measuredAfterCleaning() {
            NewProductRequest tooShort = newReq("   a   ", "", VALID_CATEGORY, VALID_PRICE, 0);
            NewProductRequest ok = newReq("  a    b  ", "", VALID_CATEGORY, VALID_PRICE, 0);

            assertThrows(ValidationException.class, () -> ProductValidator.validateNew(tooShort));
            assertDoesNotThrow(() -> ProductValidator.validateNew(ok));
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   ", "\t"})
        void validateNew_blankName_isRequired(String name) {
            NewProductRequest r = newReq(name, "", VALID_CATEGORY, VALID_PRICE, 0);

            ValidationException ex = assertThrows(ValidationException.class,
                    () -> ProductValidator.validateNew(r));
            assertEquals(java.util.List.of("Name is required"), ex.getErrors());
        }

        @Test
        void validateNew_nullName_isRequired() {
            NewProductRequest r = newReq(null, "", VALID_CATEGORY, VALID_PRICE, 0);

            ValidationException ex = assertThrows(ValidationException.class,
                    () -> ProductValidator.validateNew(r));
            assertEquals(java.util.List.of("Name is required"), ex.getErrors());
        }
    }

    @Nested
    class Description {
        @ParameterizedTest(name = "description of {0} chars valid = {1}")
        @CsvSource({"0,true", "500,true", "501,false"})
        @DisplayName("BR-P02 / AC1: description length boundaries")
        void validateNew_descriptionLength_boundaries(int length, boolean valid) {
            NewProductRequest r = newReq(VALID_NAME, "d".repeat(length), VALID_CATEGORY,
                    VALID_PRICE, 0);

            assertOutcome(valid, () -> ProductValidator.validateNew(r), "Description");
        }

        @Test
        void validateNew_nullDescription_isAllowed() {
            NewProductRequest r = newReq(VALID_NAME, null, VALID_CATEGORY, VALID_PRICE, 0);

            assertDoesNotThrow(() -> ProductValidator.validateNew(r));
        }

        @ParameterizedTest(name = "description of {0} chars valid = {1}")
        @CsvSource({"500,true", "501,false"})
        void validateUpdate_descriptionLength_boundaries(int length, boolean valid) {
            UpdateProductRequest r = updReq(VALID_NAME, "d".repeat(length), VALID_CATEGORY,
                    VALID_PRICE);

            assertOutcome(valid, () -> ProductValidator.validateUpdate(r), "Description");
        }
    }

    @Nested
    class Category {
        @ParameterizedTest(name = "category of {0} chars valid = {1}")
        @CsvSource({"1,false", "2,true", "50,true", "51,false"})
        @DisplayName("BR-P02 / AC1: category length boundaries")
        void validateNew_categoryLength_boundaries(int length, boolean valid) {
            NewProductRequest r = newReq(VALID_NAME, "", "c".repeat(length), VALID_PRICE, 0);

            assertOutcome(valid, () -> ProductValidator.validateNew(r), "Category");
        }

        @ParameterizedTest(name = "category of {0} chars valid = {1}")
        @CsvSource({"1,false", "2,true", "50,true", "51,false"})
        void validateUpdate_categoryLength_boundaries(int length, boolean valid) {
            UpdateProductRequest r = updReq(VALID_NAME, "", "c".repeat(length), VALID_PRICE);

            assertOutcome(valid, () -> ProductValidator.validateUpdate(r), "Category");
        }

        @Test
        void validateNew_nullCategory_isRequired() {
            NewProductRequest r = newReq(VALID_NAME, "", null, VALID_PRICE, 0);

            ValidationException ex = assertThrows(ValidationException.class,
                    () -> ProductValidator.validateNew(r));
            assertEquals(java.util.List.of("Category is required"), ex.getErrors());
        }
    }

    @Nested
    class Price {
        @ParameterizedTest(name = "price {0} valid = {1}")
        @CsvSource({
                "0,false",
                "0.00,false",
                "-1,false",
                "0.01,true",
                "1,true",
                "1.5,true",
                "1.50,true",
                "1.500,true",
                "10000000.00,true",
                "10000000,true",
                "10000000.01,false",
                "1.001,false",
                "0.005,false",
                "99.999,false"
        })
        @DisplayName("BR-P02 / AC1: price boundaries and decimal places")
        void validateNew_price_boundaries(String price, boolean valid) {
            NewProductRequest r = newReq(VALID_NAME, "", VALID_CATEGORY, new BigDecimal(price), 0);

            assertOutcome(valid, () -> ProductValidator.validateNew(r), "Price");
        }

        @ParameterizedTest(name = "price {0} valid = {1}")
        @CsvSource({"0,false", "0.01,true", "10000000.00,true", "10000000.01,false",
                "1.001,false"})
        void validateUpdate_price_boundaries(String price, boolean valid) {
            UpdateProductRequest r = updReq(VALID_NAME, "", VALID_CATEGORY, new BigDecimal(price));

            assertOutcome(valid, () -> ProductValidator.validateUpdate(r), "Price");
        }

        @Test
        void validateNew_nullPrice_isRequired() {
            NewProductRequest r = newReq(VALID_NAME, "", VALID_CATEGORY, null, 0);

            ValidationException ex = assertThrows(ValidationException.class,
                    () -> ProductValidator.validateNew(r));
            assertEquals(java.util.List.of("Price is required"), ex.getErrors());
        }
    }

    @Nested
    class Stock {
        @ParameterizedTest(name = "stock {0} valid = {1}")
        @CsvSource({"-1,false", "0,true", "1,true", "1000000,true", "1000001,false",
                "2147483647,false", "-2147483648,false"})
        @DisplayName("BR-P02 / AC1: stock boundaries")
        void validateNew_stock_boundaries(int stock, boolean valid) {
            NewProductRequest r = newReq(VALID_NAME, "", VALID_CATEGORY, VALID_PRICE, stock);

            assertOutcome(valid, () -> ProductValidator.validateNew(r), "Stock");
        }
    }

    @Nested
    class AllErrorsTogether {
        @Test
        @DisplayName("AC2: validateNew reports every field error in one exception")
        void validateNew_everythingInvalid_reportsAllErrors() {
            NewProductRequest r = newReq("a", "d".repeat(501), "c", new BigDecimal("0"), -1);

            ValidationException ex = assertThrows(ValidationException.class,
                    () -> ProductValidator.validateNew(r));

            assertEquals(5, ex.getErrors().size());
            assertAll(
                    () -> assertTrue(containsError(ex, "Name")),
                    () -> assertTrue(containsError(ex, "Description")),
                    () -> assertTrue(containsError(ex, "Category")),
                    () -> assertTrue(containsError(ex, "Price")),
                    () -> assertTrue(containsError(ex, "Stock")));
        }

        @Test
        @DisplayName("AC2: validateUpdate reports every field error and ignores stock")
        void validateUpdate_everythingInvalid_reportsFourErrors() {
            UpdateProductRequest r = updReq(null, "d".repeat(501), null, null);

            ValidationException ex = assertThrows(ValidationException.class,
                    () -> ProductValidator.validateUpdate(r));

            assertEquals(4, ex.getErrors().size());
        }

        @Test
        void validateNew_validRequest_doesNotThrow() {
            assertDoesNotThrow(() -> ProductValidator.validateNew(validNew()));
        }

        @Test
        void validateUpdate_validRequest_doesNotThrow() {
            assertDoesNotThrow(() -> ProductValidator.validateUpdate(
                    updReq(VALID_NAME, "desc", VALID_CATEGORY, VALID_PRICE)));
        }

        @Test
        void validateNew_nullRequest_throwsValidationException() {
            assertThrows(ValidationException.class, () -> ProductValidator.validateNew(null));
        }

        @Test
        void validateUpdate_nullRequest_throwsValidationException() {
            assertThrows(ValidationException.class, () -> ProductValidator.validateUpdate(null));
        }
    }

    /** Valid -> no exception. Invalid -> exactly one error, starting with the field name. */
    private static void assertOutcome(boolean valid, Runnable action, String field) {
        if (valid) {
            assertDoesNotThrow(action::run);
        } else {
            ValidationException ex = assertThrows(ValidationException.class, action::run);
            assertEquals(1, ex.getErrors().stream().filter(e -> e.startsWith(field)).count(),
                    () -> "Expected one " + field + " error but got " + ex.getErrors());
        }
    }

    private static boolean containsError(ValidationException ex, String field) {
        return ex.getErrors().stream().anyMatch(e -> e.startsWith(field));
    }
}
