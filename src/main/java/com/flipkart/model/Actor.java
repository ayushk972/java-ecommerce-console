/**
 * @author daksh
 * Baseline
 * 
 */

package com.flipkart.model;

import java.util.UUID;

/** Identifies the principal responsible for an operation. */
public record Actor(ActorType type, String id) {
    public static Actor guest() { return new Actor(ActorType.GUEST, "guest"); }
    public static Actor system() { return new Actor(ActorType.SYSTEM, "system"); }
    public static Actor admin(String adminId) { return new Actor(ActorType.ADMIN, adminId); }
    public static Actor customer(UUID customerId) {
        return new Actor(ActorType.CUSTOMER, customerId.toString());
    }
}
