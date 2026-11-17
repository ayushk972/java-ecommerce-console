package com.flipkart.repository.impl;

import com.flipkart.exception.ValidationException;
import com.flipkart.model.Actor;
import com.flipkart.model.AuditAction;
import com.flipkart.model.AuditEntityType;
import com.flipkart.model.AuditRecord;
import com.flipkart.repository.AuditRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Append-only, in-memory audit store (BR-AU01).
 *
 * <p>Records are kept in insertion order (oldest first). The class deliberately
 * has no update, delete, clear or replace operation, and it never hands out its
 * internal list: every query returns an unmodifiable copy. Records themselves
 * are immutable. It contains no business rules (G9).
 */
/**
 * @author nitin
 */
public class InMemoryAuditRepository implements AuditRepository {

    private final List<AuditRecord> records = new ArrayList<>();

    /**
     * Stores the record at the end of the log.
     *
     * @param record the record to store
     * @return the stored record (the same instance)
     * @throws ValidationException if the record is null
     */
    @Override
    public AuditRecord append(AuditRecord record) {
        if (record == null) {
            throw new ValidationException(List.of("Audit record is required"));
        }
        records.add(record);
        return record;
    }

    /** @return an unmodifiable snapshot of all records, oldest first */
    @Override
    public List<AuditRecord> findAll() {
        return List.copyOf(records);
    }

    /**
     * @return unmodifiable snapshot of records about the given entity, oldest first
     * @throws ValidationException if either argument is null
     */
    @Override
    public List<AuditRecord> findByEntity(AuditEntityType type, String entityId) {
        if (type == null || entityId == null) {
            throw new ValidationException(List.of("Entity type and entity id are required"));
        }
        return records.stream()
                .filter(r -> r.getEntityType() == type && r.getEntityId().equals(entityId))
                .toList();
    }

    /**
     * @return unmodifiable snapshot of records written by the given actor, oldest first
     * @throws ValidationException if the actor is null
     */
    @Override
    public List<AuditRecord> findByActor(Actor actor) {
        if (actor == null) {
            throw new ValidationException(List.of("Actor is required"));
        }
        return records.stream()
                .filter(r -> r.getActor().equals(actor))
                .toList();
    }

    /**
     * @return unmodifiable snapshot of records with the given action, oldest first
     * @throws ValidationException if the action is null
     */
    @Override
    public List<AuditRecord> findByAction(AuditAction action) {
        if (action == null) {
            throw new ValidationException(List.of("Audit action is required"));
        }
        return records.stream()
                .filter(r -> r.getAction() == action)
                .toList();
    }

    /** @return the number of stored records */
    @Override
    public long count() {
        return records.size();
    }
}
