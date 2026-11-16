package com.flipkart.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.flipkart.model.CartIssue;      // check the real package in the baseline
import com.flipkart.model.OrderStatus;    // check the real package in the baseline

class ExceptionHierarchyTest {

    @Nested
    class Root {
        @Test
        void constructor_keepsMessage() {
            ECommerceException ex = new ECommerceException("Something failed");
            assertEquals("Something failed", ex.getMessage());
            assertInstanceOf(RuntimeException.class, ex);        // unchecked (AC1)
        }

        @Test
        void constructorWithCause_keepsCause() {
            Throwable cause = new IllegalStateException("root cause");
            ECommerceException ex = new ECommerceException("Wrapped", cause);
            assertSame(cause, ex.getCause());
        }
    }

    @Nested
    class Validation {
        @Test
        void constructor_emptyList_throwsIllegalArgument() {
            assertThrows(IllegalArgumentException.class,
                    () -> new ValidationException(List.of()));
        }

        @Test
        void constructor_nullList_throwsIllegalArgument() {
            assertThrows(IllegalArgumentException.class,
                    () -> new ValidationException(null));
        }

        @Test
        void getErrors_isDefensiveCopy() {
            List<String> source = new ArrayList<>(List.of("Name is required"));
            ValidationException ex = new ValidationException(source);
            source.add("added later");
            assertEquals(1, ex.getErrors().size());
        }

        @Test
        void getErrors_isImmutable() {
            ValidationException ex = new ValidationException(List.of("A"));
            assertThrows(UnsupportedOperationException.class, () -> ex.getErrors().add("B"));
        }

        @Test
        void getMessage_joinsErrors() {
            assertEquals("A; B", new ValidationException(List.of("A", "B")).getMessage());
        }
    }

    @Nested
    class ResourceNotFound {
        @Test
        void getMessage_hasEntityAndId() {
            assertEquals("Product not found: PRD-1001",
                    new ResourceNotFoundException("Product", "PRD-1001").getMessage());
        }
    }

    @Nested
    class InsufficientStock {
        @Test
        void getters_returnConstructorValues() {
            InsufficientStockException ex = new InsufficientStockException("PRD-1001", 8, 5);
            assertEquals("PRD-1001", ex.getProductId());
            assertEquals(8, ex.getRequested());
            assertEquals(5, ex.getAvailable());
        }
    }

    @Nested
    class CheckoutValidation {
        @Test
        void getIssues_isDefensiveCopy() {
            List<CartIssue> source = new ArrayList<>();
            CheckoutValidationException ex = new CheckoutValidationException(source);
            source.add(null);
            assertTrue(ex.getIssues().isEmpty());
        }

        @Test
        void getIssues_isImmutable() {
            CheckoutValidationException ex = new CheckoutValidationException(new ArrayList<>());
            assertThrows(UnsupportedOperationException.class, () -> ex.getIssues().add(null));
        }
    }

    @Nested
    class InvalidOrderTransition {
        @Test
        void getters_andMessage() {
            InvalidOrderTransitionException ex =
                    new InvalidOrderTransitionException(OrderStatus.PLACED, OrderStatus.SHIPPED);
            assertEquals(OrderStatus.PLACED, ex.getFrom());
            assertEquals(OrderStatus.SHIPPED, ex.getTo());
            assertEquals("Cannot change order status from PLACED to SHIPPED", ex.getMessage());
        }
    }

    @Nested
    class MessageOnly {
        @Test
        void allExtendRootAndKeepMessage() {
            List<ECommerceException> all = List.of(
                    new DuplicateResourceException("Mobile already registered"),
                    new AuthenticationException("Invalid Login ID or password"),
                    new AuthorizationException("Access denied"),
                    new EmptyCartException("Your cart is empty"),
                    new IdGenerationException("No free ID"),
                    new InputClosedException("Input ended"));

            for (ECommerceException e : all) {
                assertNotNull(e.getMessage());
                assertFalse(e.getMessage().contains("Exception"));   // AC8
            }
        }
    }
}