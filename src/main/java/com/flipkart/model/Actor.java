package com.flipkart.model;

import com.flipkart.exception.ValidationException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Who performs an operation: a guest, a customer, an admin or the system.
 *
 * <p>Immutable value type. The type must not be null and the id must not be
 * blank; the id is stored trimmed. Prefer the factory methods, which fix the
 * type and the id format.
 *
 * @param type the kind of actor
 * @param id   the actor identifier (never blank, trimmed)
 * @throws ValidationException (from the canonical constructor) listing every
 *                             invalid field
 */
/**
 * @author nitin singh
 */
public record Actor(ActorType type, String id) {

    /** Id used for every guest actor. */
    public static final String GUEST_ID = "guest";

    /** Id used for the system actor. */
    public static final String SYSTEM_ID = "system";

    public Actor {
        List<String> errors = new ArrayList<>();
        if (type == null) {
            errors.add("Actor type is required");
        }
        if (id == null || id.isBlank()) {
            errors.add("Actor id is required");
        } else {
            id = id.trim();
        }
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }

    /** @return the guest actor (id "guest") */
    public static Actor guest() {
        return new Actor(ActorType.GUEST, GUEST_ID);
    }

    /** @return the system actor (id "system"), used for seeding and bootstrap work */
    public static Actor system() {
        return new Actor(ActorType.SYSTEM, SYSTEM_ID);
    }

    /**
     * @param adminId the admin login id, for example "admin"
     * @return an ADMIN actor with that id
     * @throws ValidationException if the id is null or blank
     */
    public static Actor admin(String adminId) {
        return new Actor(ActorType.ADMIN, adminId);
    }

    /**
     * @param customerId the internal customer UUID (not the 7-digit login id)
     * @return a CUSTOMER actor whose id is the UUID text
     * @throws ValidationException if the UUID is null
     */
    public static Actor customer(UUID customerId) {
        if (customerId == null) {
            throw new ValidationException(List.of("Customer id is required"));
        }
        return new Actor(ActorType.CUSTOMER, customerId.toString());
    }
}
