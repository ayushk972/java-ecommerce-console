package com.flipkart.model;

import com.flipkart.exception.ValidationException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * One immutable entry in the audit trail (BR-AU01, BR-AU02).
 *
 * <p>All fields are final and there are no setters. Every field except
 * details must be non-null. Details that are null become the empty string;
 * details longer than {@value #MAX_DETAILS_LENGTH} characters are cut so that
 * the stored text is exactly {@value #MAX_DETAILS_LENGTH} characters long and
 * ends with "...".
 */
/**
 * @author nitin singh 
 */
public final class AuditRecord {

    /** Maximum stored length of the details text (BR-AU02). */
    public static final int MAX_DETAILS_LENGTH = 500;

    /** Marker appended to details that had to be shortened. */
    public static final String ELLIPSIS = "...";

    private final String auditId;
    private final Instant timestamp;
    private final Actor actor;
    private final AuditAction action;
    private final AuditEntityType entityType;
    private final String entityId;
    private final AuditOutcome outcome;
    private final String details;

    /**
     * @throws ValidationException listing every null field (details excepted)
     */
    public AuditRecord(String auditId, Instant timestamp, Actor actor, AuditAction action,
                       AuditEntityType entityType, String entityId, AuditOutcome outcome,
                       String details) {
        List<String> errors = new ArrayList<>();
        if (auditId == null) errors.add("Audit id is required");
        if (timestamp == null) errors.add("Audit timestamp is required");
        if (actor == null) errors.add("Audit actor is required");
        if (action == null) errors.add("Audit action is required");
        if (entityType == null) errors.add("Audit entity type is required");
        if (entityId == null) errors.add("Audit entity id is required");
        if (outcome == null) errors.add("Audit outcome is required");
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
        this.auditId = auditId;
        this.timestamp = timestamp;
        this.actor = actor;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.outcome = outcome;
        this.details = normalizeDetails(details);
    }

    private static String normalizeDetails(String details) {
        if (details == null) {
            return "";
        }
        if (details.length() <= MAX_DETAILS_LENGTH) {
            return details;
        }
        int cut = MAX_DETAILS_LENGTH - ELLIPSIS.length();
        // do not leave half of a surrogate pair at the cut
        if (Character.isHighSurrogate(details.charAt(cut - 1))) {
            cut--;
        }
        return details.substring(0, cut) + ELLIPSIS;
    }

    public String getAuditId() { return auditId; }
    public Instant getTimestamp() { return timestamp; }
    public Actor getActor() { return actor; }
    public AuditAction getAction() { return action; }
    public AuditEntityType getEntityType() { return entityType; }
    public String getEntityId() { return entityId; }
    public AuditOutcome getOutcome() { return outcome; }
    public String getDetails() { return details; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AuditRecord that)) return false;
        return auditId.equals(that.auditId)
                && timestamp.equals(that.timestamp)
                && actor.equals(that.actor)
                && action == that.action
                && entityType == that.entityType
                && entityId.equals(that.entityId)
                && outcome == that.outcome
                && details.equals(that.details);
    }

    @Override
    public int hashCode() {
        return Objects.hash(auditId, timestamp, actor, action, entityType, entityId, outcome, details);
    }

    @Override
    public String toString() {
        return "AuditRecord[" + auditId + ", " + timestamp + ", " + actor + ", " + action
                + ", " + entityType + ":" + entityId + ", " + outcome + ", details=" + details + "]";
    }
}
