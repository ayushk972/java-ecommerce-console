/**
 * 
 * @author daksh
 * 
 * 
 */


package com.flipkart.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.flipkart.exception.ValidationException;
import com.flipkart.model.RegistrationRequest;

class CustomerValidatorTest {

    private RegistrationRequest request(
            String name,
            String mobile,
            String password) {

        return new RegistrationRequest(name, mobile, password);
    }

    @Test
    void shouldAcceptValidRegistration() {
        assertDoesNotThrow(() ->
            CustomerValidator.validateRegistration(
                request("Daksh Arya", "9876543210", "password123")
            )
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "A",
        "A.",
        "1A",
        "Daksh123",
        "Daksh@Arya",
        "Daksh/Arya"
    })
    void shouldRejectInvalidNames(String name) {
        assertThrows(ValidationException.class, () ->
            CustomerValidator.validateRegistration(
                request(name, "9876543210", "password123")
            )
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "Al",
        "A B",
        "Daksh Arya",
        "Mary-Jane",
        "O'Neil",
        "Dr. Smith"
    })
    void shouldAcceptValidNames(String name) {
        assertDoesNotThrow(() ->
            CustomerValidator.validateRegistration(
                request(name, "9876543210", "password123")
            )
        );
    }

    @Test
    void shouldAcceptNameAtMaximumLength() {
        String name = "A".repeat(50);

        assertDoesNotThrow(() ->
            CustomerValidator.validateRegistration(
                request(name, "9876543210", "password123")
            )
        );
    }

    @Test
    void shouldRejectNameAboveMaximumLength() {
        String name = "A".repeat(51);

        assertThrows(ValidationException.class, () ->
            CustomerValidator.validateRegistration(
                request(name, "9876543210", "password123")
            )
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "123456789",
        "12345678901",
        "5876543210",
        "987654321A",
        "98765 3210",
        " 9876543210"
    })
    void shouldRejectInvalidMobile(String mobile) {
        assertThrows(ValidationException.class, () ->
            CustomerValidator.validateRegistration(
                request("Daksh Arya", mobile, "password123")
            )
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "6123456789",
        "7123456789",
        "8123456789",
        "9123456789"
    })
    void shouldAcceptMobileStartingFromSixToNine(String mobile) {
        assertDoesNotThrow(() ->
            CustomerValidator.validateRegistration(
                request("Daksh Arya", mobile, "password123")
            )
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "1234567"
        })
    void shouldRejectPasswordBelowEightCharacters(String password) {
        assertThrows(ValidationException.class, () ->
            CustomerValidator.validateRegistration(
                request("Daksh Arya", "9876543210", password)
            )
        );
    }

    @Test
    void shouldAcceptPasswordAtMinimumLength() {
        assertDoesNotThrow(() ->
            CustomerValidator.validateRegistration(
                request("Daksh Arya", "9876543210", "12345678")
            )
        );
    }

    @Test
    void shouldAcceptPasswordAtMaximumLength() {
        assertDoesNotThrow(() ->
            CustomerValidator.validateRegistration(
                request("Daksh Arya", "9876543210", "A".repeat(64))
            )
        );
    }

    @Test
    void shouldRejectPasswordAboveMaximumLength() {
        assertThrows(ValidationException.class, () ->
            CustomerValidator.validateRegistration(
                request("Daksh Arya", "9876543210", "A".repeat(65))
            )
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "",
        " ",
        "       "
    })
    void shouldRejectBlankPassword(String password) {
        assertThrows(ValidationException.class, () ->
            CustomerValidator.validateRegistration(
                request("Daksh Arya", "9876543210", password)
            )
        );
    }

    @Test
    void shouldCollectAllErrorsTogether() {
        ValidationException exception = assertThrows(
            ValidationException.class,
            () -> CustomerValidator.validateRegistration(
                request("A1", "123", "123")
            )
        );

        assertEquals(4, exception.getErrors().size());
    }

    @Test
    void shouldRejectNullRequest() {
        assertThrows(ValidationException.class,
                () -> CustomerValidator.validateRegistration(null));
    }

    @Test
    void shouldNormalizeWhitespace() {
        assertEquals(
            "Daksh Arya",
            CustomerValidator.normalizeName("  Daksh    Arya  ")
        );
    }

    @Test
    void shouldNormalizeTabsAndNewlines() {
        assertEquals(
            "Daksh Arya",
            CustomerValidator.normalizeName("Daksh\t\n Arya")
        );
    }

    @Test
    void shouldReturnEmptyStringForNullName() {
        assertEquals("", CustomerValidator.normalizeName(null));
    }
}
