package com.flipkart.model;

/**
 * 
 * @author daksh
 * 
 */




import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Immutable customer entity.
 *
 * A Customer has an internal UUID and a 7-digit String login ID.
 * Passwords are stored only as hashes.
 */
public final class Customer {

    private final UUID id;
    private final String loginId;
    private final String name;
    private final String mobile;
    private final String passwordHash;
    private final Instant registeredAt;

    private Customer(
            UUID id,
            String loginId,
            String name,
            String mobile,
            String passwordHash,
            Instant registeredAt) {

        this.id = id;
        this.loginId = loginId;
        this.name = name;
        this.mobile = mobile;
        this.passwordHash = passwordHash;
        this.registeredAt = registeredAt;
    }

    /**
     * Creates a Customer after validating required fields.
     *
     * @throws IllegalArgumentException if a required field is null
     *         or loginId is not exactly seven digits
     */
    public static Customer create(
            UUID id,
            String loginId,
            String name,
            String mobile,
            String passwordHash,
            Instant registeredAt) {

        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(loginId, "loginId must not be null");
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(mobile, "mobile must not be null");
        Objects.requireNonNull(passwordHash, "passwordHash must not be null");
        Objects.requireNonNull(registeredAt, "registeredAt must not be null");

        if (!loginId.matches("\\d{7}")) {
            throw new IllegalArgumentException(
                    "loginId must contain exactly 7 digits");
        }

        return new Customer(
                id, loginId, name, mobile, passwordHash, registeredAt);
    }

    public UUID getId() {
        return id;
    }

    public String getLoginId() {
        return loginId;
    }

    public String getName() {
        return name;
    }

    public String getMobile() {
        return mobile;
    }

    /**
     * Internal accessor used by authentication logic.
     * Never print or log this value.
     */
    public String getPasswordHash() {
        return passwordHash;
    }

    public Instant getRegisteredAt() {
        return registeredAt;
    }

    /**
     * Hides all mobile digits except the last four.
     */
    public String maskedMobile() {
        return "******" + mobile.substring(mobile.length() - 4);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Customer other)) {
            return false;
        }

        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    /**
     * Deliberately excludes passwordHash and full mobile number.
     */
    @Override
    public String toString() {
        return "Customer{" +
                "id=" + id +
                ", loginId='" + loginId + '\'' +
                ", name='" + name + '\'' +
                ", mobile='" + maskedMobile() + '\'' +
                ", registeredAt=" + registeredAt +
                '}';
    }
}
