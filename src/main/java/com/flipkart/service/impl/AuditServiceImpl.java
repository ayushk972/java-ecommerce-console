
package com.flipkart.service.impl;

import java.time.Clock;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import com.flipkart.exception.AuthorizationException;
import com.flipkart.exception.ValidationException;
import com.flipkart.model.Actor;
import com.flipkart.model.ActorType;
import com.flipkart.model.AuditAction;
import com.flipkart.model.AuditEntityType;
import com.flipkart.model.AuditOutcome;
import com.flipkart.model.AuditRecord;
import com.flipkart.repository.AuditRepository;
import com.flipkart.service.AuditService;
import com.flipkart.util.IdGenerator;

public class AuditServiceImpl implements AuditService {

	private final AuditRepository auditRepository;
	private final IdGenerator idGenerator;
	private final Clock clock;

	public AuditServiceImpl(AuditRepository auditRepository, IdGenerator idGenerator, Clock clock) {
		this.auditRepository = Objects.requireNonNull(auditRepository);
		this.idGenerator = Objects.requireNonNull(idGenerator);
		this.clock = Objects.requireNonNull(clock);
	}

	@Override
	public void record(Actor actor, AuditAction action, AuditEntityType entityType, String entityId,
			AuditOutcome outcome, String details) {
		List<String> errors = new ArrayList<>();

		if (actor == null) {
			errors.add("Actor is required");
		}
		if (action == null) {
			errors.add("Action is required");
		}
		if (entityType == null) {
			errors.add("Entity type is required");
		}
		if (entityId == null) {
			errors.add("Entity id is required");
		}
		if (outcome == null) {
			errors.add("Outcome is required");
		}

		if (!errors.isEmpty()) {
			throw new ValidationException(errors);
		}

		String auditId = idGenerator.nextId();

		AuditRecord auditRecord = new AuditRecord(auditId, clock.instant(), actor, action, entityType, entityId,
				outcome, details);

		auditRepository.append(auditRecord);
	}

	private void validateAdmin(Actor admin) {

		if (admin == null) {
			throw new ValidationException(List.of("Admin actor is required"));
		}

		if (admin.type() != ActorType.ADMIN) {
			throw new AuthorizationException("Only ADMIN can access audit records");
		}
	}

	@Override
	public List<AuditRecord> recent(Actor admin, int limit) {

		validateAdmin(admin);

		if (limit < 1 || limit > 500) {
			throw new ValidationException(List.of("Limit must be between 1 and 500"));
		}

		List<AuditRecord> result = new ArrayList<>(auditRepository.findAll());

		// First sort by timestamp: oldest to newest.
		result.sort(Comparator.comparing(AuditRecord::getTimestamp));

		// Reverse the entire list: newest timestamp and newest insertion first.
		Collections.reverse(result);

		return result.stream().limit(limit).toList();
	}

	@Override
	public List<AuditRecord> forEntity(Actor admin, AuditEntityType entityType, String entityId) {
		validateAdmin(admin);

		if (entityType == null || entityId == null) {
			throw new ValidationException(List.of("Entity type and entity id are required"));
		}

		return auditRepository.findByEntity(entityType, entityId);
	}

	@Override
	public List<AuditRecord> forActor(Actor admin, Actor targetActor) {

		validateAdmin(admin);

		if (targetActor == null) {
			throw new ValidationException(List.of("Target actor is required"));
		}

		return auditRepository.findByActor(targetActor);
	}

	@Override
	public List<AuditRecord> forAction(Actor admin, AuditAction action) {

		validateAdmin(admin);

		if (action == null) {
			throw new ValidationException(List.of("Audit action is required"));
		}

		return auditRepository.findByAction(action);
	}

}
