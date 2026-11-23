
package com.flipkart.service;

import com.flipkart.model.Actor;
import com.flipkart.model.AuditAction;
import com.flipkart.model.AuditEntityType;
import com.flipkart.model.AuditOutcome;
import com.flipkart.model.AuditRecord;

import java.util.List;

public interface AuditService {

    void record(
            Actor actor,
            AuditAction action,
            AuditEntityType entityType,
            String entityId,
            AuditOutcome outcome,
            String details
    );

    List<AuditRecord> recent(Actor admin, int limit);

    List<AuditRecord> forEntity(
            Actor admin,
            AuditEntityType entityType,
            String entityId
    );

    List<AuditRecord> forActor(Actor admin, Actor targetActor);

    List<AuditRecord> forAction(Actor admin, AuditAction action);
}
