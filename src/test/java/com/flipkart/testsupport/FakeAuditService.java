
package com.flipkart.testsupport;

import com.flipkart.exception.AuthorizationException;
import com.flipkart.exception.ValidationException;
import com.flipkart.model.Actor;
import com.flipkart.model.ActorType;
import com.flipkart.model.AuditAction;
import com.flipkart.model.AuditEntityType;
import com.flipkart.model.AuditOutcome;
import com.flipkart.model.AuditRecord;
import com.flipkart.service.AuditService;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class FakeAuditService implements AuditService {

	private final List<AuditRecord> records = new ArrayList<>();
	private long nextId = 1;

	@Override
	public void record(Actor actor, AuditAction action, AuditEntityType entityType, String entityId,
			AuditOutcome outcome, String details) {

		if (actor == null || action == null || entityType == null || entityId == null || outcome == null) {
			throw new ValidationException(List.of("Required audit arguments cannot be null"));
		}

		AuditRecord record = new AuditRecord(String.format("AUD-%06d", nextId++), Instant.now(), actor, action,
				entityType, entityId, outcome, details);

		records.add(record);
	}

	@Override
	public List<AuditRecord> recent(Actor admin, int limit) {
		validateAdmin(admin);

		if (limit < 1 || limit > 500) {
			throw new ValidationException(List.of("Limit must be between 1 and 500"));
		}

		return records.stream().sorted(Comparator.comparing(AuditRecord::getTimestamp).reversed()).limit(limit)
				.toList();
	}

	@Override
	public List<AuditRecord> forEntity(Actor admin, AuditEntityType entityType, String entityId) {
		validateAdmin(admin);

		if (entityType == null || entityId == null) {
			throw new ValidationException(List.of("Entity type and entity id are required"));
		}

		return records.stream().filter(r -> r.getEntityType() == entityType)
				.filter(r -> r.getEntityId().equals(entityId)).toList();
	}

	@Override
	public List<AuditRecord> forActor(Actor admin, Actor targetActor) {
		validateAdmin(admin);

		if (targetActor == null) {
			throw new ValidationException(List.of("Target actor is required"));
		}

		return records.stream().filter(r -> r.getActor().equals(targetActor)).toList();
	}

	@Override
	public List<AuditRecord> forAction(Actor admin, AuditAction action) {
		validateAdmin(admin);

		if (action == null) {
			throw new ValidationException(List.of("Audit action is required"));
		}

		return records.stream().filter(r -> r.getAction() == action).toList();
	}

	public List<AuditRecord> getRecords() {
		return List.copyOf(records);
	}

	private void validateAdmin(Actor admin) {
		if (admin == null) {
			throw new ValidationException(List.of("Admin actor is required"));
		}

		if (admin.type() != ActorType.ADMIN) {
			throw new AuthorizationException("Only ADMIN can access audit records");
		}
	}
}
