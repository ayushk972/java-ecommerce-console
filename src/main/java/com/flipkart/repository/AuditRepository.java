package com.flipkart.repository;

import com.flipkart.exception.ValidationException;
import com.flipkart.model.Actor;
import com.flipkart.model.AuditAction;
import com.flipkart.model.AuditEntityType;
import com.flipkart.model.AuditRecord;

import java.util.List;

/**
 * Append-only store for audit records (BR-AU01).
 *
 * <p>This contract deliberately has no update, delete, clear or replace
 * operation. Implementations must return unmodifiable snapshots and never
 * expose their internal collections. All lists are ordered oldest first.
 */
public interface AuditRepository {

    /**
     * Stores the record at the end of the log.
     *
     * @param r the record to store
     * @return the stored record
     * @throws ValidationException if the record is null
     */
    AuditRecord append(AuditRecord r);

    /** @return an unmodifiable snapshot of all records, oldest first */
    List<AuditRecord> findAll();

    /**
     * @param t        the entity type
     * @param entityId the entity id
     * @return unmodifiable snapshot of records about that entity, oldest first
     * @throws ValidationException if either argument is null
     */
    List<AuditRecord> findByEntity(AuditEntityType t, String entityId);

    /**
     * @param a the actor who performed the actions
     * @return unmodifiable snapshot of that actor's records, oldest first
     * @throws ValidationException if the actor is null
     */
    List<AuditRecord> findByActor(Actor a);

    /**
     * @param action the audit action
     * @return unmodifiable snapshot of records with that action, oldest first
     * @throws ValidationException if the action is null
     */
    List<AuditRecord> findByAction(AuditAction action);

    /** @return the number of stored records */
    long count();
}
