/**
 * @author daksh
 * baseline
 */

package com.flipkart.model;

import java.time.Instant;

/**
 * Baseline entity shell: fields, constructor and getters only.
 * Feature stories add validation, factories and behaviour.
 */
public class AuditRecord {
    private final String auditId;
    private final java.time.Instant timestamp;
    private final Actor actor;
    private final AuditAction action;
    private final AuditEntityType entityType;
    private final String entityId;
    private final AuditOutcome outcome;
    private final String details;

    public AuditRecord(String auditId, java.time.Instant timestamp, Actor actor, AuditAction action, AuditEntityType entityType, String entityId, AuditOutcome outcome, String details) {
        this.auditId = auditId;
        this.timestamp = timestamp;
        this.actor = actor;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.outcome = outcome;
        this.details = details;
    }

    public String getAuditId() { return auditId; }
    public java.time.Instant getTimestamp() { return timestamp; }
    public Actor getActor() { return actor; }
    public AuditAction getAction() { return action; }
    public AuditEntityType getEntityType() { return entityType; }
    public String getEntityId() { return entityId; }
    public AuditOutcome getOutcome() { return outcome; }
    public String getDetails() { return details; }
}
