
package com.flipkart.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.flipkart.exception.AuthorizationException;
import com.flipkart.exception.ValidationException;
import com.flipkart.model.Actor;
import com.flipkart.model.AuditAction;
import com.flipkart.model.AuditEntityType;
import com.flipkart.model.AuditOutcome;
import com.flipkart.model.AuditRecord;
import com.flipkart.repository.AuditRepository;
import com.flipkart.util.IdGenerator;

class AuditServiceImplTest {

	private static final Instant NOW = Instant.parse("2026-11-20T10:00:00Z");

	private FakeAuditRepository fakeRepository;
	private FakeIdGenerator fakeIdGenerator;
	private AuditServiceImpl auditService;

	@BeforeEach
	void setUp() {
		fakeRepository = new FakeAuditRepository();
		fakeIdGenerator = new FakeIdGenerator();

		Clock fixedClock = Clock.fixed(NOW, ZoneOffset.UTC);

		auditService = new AuditServiceImpl(fakeRepository, fakeIdGenerator, fixedClock);
	}

	private static final class FakeAuditRepository implements AuditRepository {

		private final List<AuditRecord> store = new ArrayList<>();

		private boolean throwOnAppend;

		@Override
		public AuditRecord append(AuditRecord record) {
			if (throwOnAppend) {
				throw new IllegalStateException("Simulated repository failure");
			}

			if (record == null) {
				throw new ValidationException(List.of("Audit record is required"));
			}

			store.add(record);
			return record;
		}

		void setThrowOnAppend(boolean throwOnAppend) {
			this.throwOnAppend = throwOnAppend;
		}

		@Override
		public List<AuditRecord> findAll() {
			return List.copyOf(store);
		}

		@Override
		public List<AuditRecord> findByEntity(AuditEntityType type, String entityId) {

			return store.stream().filter(r -> r.getEntityType() == type).filter(r -> r.getEntityId().equals(entityId))
					.collect(Collectors.toUnmodifiableList());
		}

		@Override
		public List<AuditRecord> findByActor(Actor actor) {
			return store.stream().filter(r -> r.getActor().equals(actor)).collect(Collectors.toUnmodifiableList());
		}

		@Override
		public List<AuditRecord> findByAction(AuditAction action) {
			return store.stream().filter(r -> r.getAction() == action).collect(Collectors.toUnmodifiableList());
		}

		@Override
		public long count() {
			return store.size();
		}
	}

	private static final class FakeIdGenerator implements IdGenerator {

		private long nextId = 1;

		@Override
		public String nextId() {
			return String.format("AUD-%06d", nextId++);
		}
	}

	@Test
	void record_validArguments_storesAuditRecord() {
		Actor actor = Actor.admin("admin-01");

		auditService.record(actor, AuditAction.PRODUCT_CREATED, AuditEntityType.PRODUCT, "PROD-101",
				AuditOutcome.SUCCESS, "Product created successfully");

		List<AuditRecord> records = fakeRepository.findAll();

		assertEquals(1, records.size());

		AuditRecord record = records.get(0);

		assertEquals("AUD-000001", record.getAuditId());
		assertEquals(NOW, record.getTimestamp());
		assertEquals(actor, record.getActor());
		assertEquals(AuditAction.PRODUCT_CREATED, record.getAction());
		assertEquals(AuditEntityType.PRODUCT, record.getEntityType());
		assertEquals("PROD-101", record.getEntityId());
		assertEquals(AuditOutcome.SUCCESS, record.getOutcome());
		assertEquals("Product created successfully", record.getDetails());
	}

	@Test
	void record_nullRequiredArguments_throwsValidationException() {

		assertThrows(ValidationException.class, () -> auditService.record(null, AuditAction.PRODUCT_CREATED,
				AuditEntityType.PRODUCT, "PROD-101", AuditOutcome.SUCCESS, "Details"));

		assertThrows(ValidationException.class, () -> auditService.record(Actor.admin("admin-01"), null,
				AuditEntityType.PRODUCT, "PROD-101", AuditOutcome.SUCCESS, "Details"));

		assertThrows(ValidationException.class, () -> auditService.record(Actor.admin("admin-01"),
				AuditAction.PRODUCT_CREATED, null, "PROD-101", AuditOutcome.SUCCESS, "Details"));

		assertThrows(ValidationException.class, () -> auditService.record(Actor.admin("admin-01"),
				AuditAction.PRODUCT_CREATED, AuditEntityType.PRODUCT, null, AuditOutcome.SUCCESS, "Details"));

		assertThrows(ValidationException.class, () -> auditService.record(Actor.admin("admin-01"),
				AuditAction.PRODUCT_CREATED, AuditEntityType.PRODUCT, "PROD-101", null, "Details"));
	}

	@Test
	void record_nullDetails_storesRecordSuccessfully() {
		auditService.record(Actor.guest(), AuditAction.PRODUCT_CREATED, AuditEntityType.PRODUCT, "PROD-101",
				AuditOutcome.SUCCESS, null);

		assertEquals(1, fakeRepository.count());
		assertEquals("", fakeRepository.findAll().get(0).getDetails());
	}

	@Test
	void queryMethods_nonAdminActor_throwsAuthorizationException() {

		Actor customer = Actor.customer(java.util.UUID.randomUUID());
		Actor guest = Actor.guest();

		assertThrows(AuthorizationException.class, () -> auditService.recent(customer, 10));

		assertThrows(AuthorizationException.class,
				() -> auditService.forEntity(customer, AuditEntityType.PRODUCT, "PROD-101"));

		assertThrows(AuthorizationException.class, () -> auditService.forActor(customer, Actor.guest()));

		assertThrows(AuthorizationException.class, () -> auditService.forAction(customer, AuditAction.PRODUCT_CREATED));

		assertThrows(AuthorizationException.class, () -> auditService.recent(guest, 10));
	}

	@Test
	void queryMethods_nullAdmin_throwsValidationException() {

		assertThrows(ValidationException.class, () -> auditService.recent(null, 10));

		assertThrows(ValidationException.class,
				() -> auditService.forEntity(null, AuditEntityType.PRODUCT, "PROD-101"));

		assertThrows(ValidationException.class, () -> auditService.forActor(null, Actor.guest()));

		assertThrows(ValidationException.class, () -> auditService.forAction(null, AuditAction.PRODUCT_CREATED));
	}

	@Test
	void forEntity_matchingEntity_returnsMatchingRecords() {
		Actor admin = Actor.admin("admin-01");

		auditService.record(admin, AuditAction.PRODUCT_CREATED, AuditEntityType.PRODUCT, "PROD-101",
				AuditOutcome.SUCCESS, "Product created");

		auditService.record(admin, AuditAction.PRODUCT_UPDATED, AuditEntityType.PRODUCT, "PROD-102",
				AuditOutcome.SUCCESS, "Product updated");

		auditService.record(admin, AuditAction.ORDER_PLACED, AuditEntityType.ORDER, "ORD-101", AuditOutcome.SUCCESS,
				"Order placed");

		List<AuditRecord> result = auditService.forEntity(admin, AuditEntityType.PRODUCT, "PROD-101");

		assertEquals(1, result.size());
		assertEquals("PROD-101", result.get(0).getEntityId());
	}

	@Test
	void forActor_matchingActor_returnsOnlyTheirRecords() {
		Actor admin = Actor.admin("admin-01");
		Actor customer = Actor.customer(java.util.UUID.randomUUID());
		Actor anotherCustomer = Actor.customer(java.util.UUID.randomUUID());

		auditService.record(customer, AuditAction.ORDER_PLACED, AuditEntityType.ORDER, "ORD-101", AuditOutcome.SUCCESS,
				"Order placed");

		auditService.record(anotherCustomer, AuditAction.ORDER_PLACED, AuditEntityType.ORDER, "ORD-102",
				AuditOutcome.SUCCESS, "Another order");

		List<AuditRecord> result = auditService.forActor(admin, customer);

		assertEquals(1, result.size());
		assertEquals(customer, result.get(0).getActor());
	}

	@Test
	void forAction_matchingAction_returnsOnlyMatchingRecords() {
		Actor admin = Actor.admin("admin-01");

		auditService.record(admin, AuditAction.PRODUCT_CREATED, AuditEntityType.PRODUCT, "PROD-101",
				AuditOutcome.SUCCESS, "Created");

		auditService.record(admin, AuditAction.PRODUCT_UPDATED, AuditEntityType.PRODUCT, "PROD-101",
				AuditOutcome.SUCCESS, "Updated");

		auditService.record(admin, AuditAction.PRODUCT_CREATED, AuditEntityType.PRODUCT, "PROD-102",
				AuditOutcome.SUCCESS, "Created another");

		List<AuditRecord> result = auditService.forAction(admin, AuditAction.PRODUCT_CREATED);

		assertEquals(2, result.size());
		assertTrue(result.stream().allMatch(r -> r.getAction() == AuditAction.PRODUCT_CREATED));
	}

	@Test
	void recent_adminActor_returnsRecordsNewestFirst() {
		Actor admin = Actor.admin("admin-01");

		AuditRecord older = new AuditRecord("AUD-000001", Instant.parse("2026-11-18T10:00:00Z"), admin,
				AuditAction.PRODUCT_CREATED, AuditEntityType.PRODUCT, "PROD-101", AuditOutcome.SUCCESS, "Older record");

		AuditRecord newer = new AuditRecord("AUD-000002", Instant.parse("2026-11-20T10:00:00Z"), admin,
				AuditAction.PRODUCT_UPDATED, AuditEntityType.PRODUCT, "PROD-101", AuditOutcome.SUCCESS, "Newer record");

		fakeRepository.append(older);
		fakeRepository.append(newer);

		List<AuditRecord> result = auditService.recent(admin, 10);

		assertEquals(2, result.size());
		assertEquals("AUD-000002", result.get(0).getAuditId());
		assertEquals("AUD-000001", result.get(1).getAuditId());
	}

	@Test
	void recent_limitOne_returnsOnlyNewestRecord() {
		Actor admin = Actor.admin("admin-01");

		auditService.record(admin, AuditAction.PRODUCT_CREATED, AuditEntityType.PRODUCT, "PROD-101",
				AuditOutcome.SUCCESS, "First");

		auditService.record(admin, AuditAction.PRODUCT_UPDATED, AuditEntityType.PRODUCT, "PROD-101",
				AuditOutcome.SUCCESS, "Second");

		List<AuditRecord> result = auditService.recent(admin, 1);

		assertEquals(1, result.size());
	}

	@Test
	void recent_invalidLimit_throwsValidationException() {
		Actor admin = Actor.admin("admin-01");

		assertThrows(ValidationException.class, () -> auditService.recent(admin, 0));

		assertThrows(ValidationException.class, () -> auditService.recent(admin, 501));
	}

	@Test
	void forEntity_nullArguments_throwsValidationException() {
		Actor admin = Actor.admin("admin-01");

		assertThrows(ValidationException.class, () -> auditService.forEntity(admin, null, "PROD-101"));

		assertThrows(ValidationException.class, () -> auditService.forEntity(admin, AuditEntityType.PRODUCT, null));
	}

	@Test
	void forActor_nullTargetActor_throwsValidationException() {
		Actor admin = Actor.admin("admin-01");

		assertThrows(ValidationException.class, () -> auditService.forActor(admin, null));
	}

	@Test
	void forAction_nullAction_throwsValidationException() {
		Actor admin = Actor.admin("admin-01");

		assertThrows(ValidationException.class, () -> auditService.forAction(admin, null));
	}

	@Test
	void recent_sameTimestamp_returnsNewestAppendedRecordFirst() {
		Actor admin = Actor.admin("admin-01");
		Instant sameTime = Instant.parse("2026-11-20T10:00:00Z");

		AuditRecord first = new AuditRecord("AUD-000001", sameTime, admin, AuditAction.PRODUCT_CREATED,
				AuditEntityType.PRODUCT, "PROD-101", AuditOutcome.SUCCESS, "First");

		AuditRecord second = new AuditRecord("AUD-000002", sameTime, admin, AuditAction.PRODUCT_UPDATED,
				AuditEntityType.PRODUCT, "PROD-102", AuditOutcome.SUCCESS, "Second");

		fakeRepository.append(first);
		fakeRepository.append(second);

		List<AuditRecord> result = auditService.recent(admin, 10);

		assertEquals("AUD-000002", result.get(0).getAuditId());
		assertEquals("AUD-000001", result.get(1).getAuditId());
	}

	@Test
	void record_repositoryThrowsException_propagatesException() {
		Actor admin = Actor.admin("admin-01");

		fakeRepository.setThrowOnAppend(true);

		assertThrows(IllegalStateException.class, () -> auditService.record(admin, AuditAction.PRODUCT_CREATED,
				AuditEntityType.PRODUCT, "PROD-101", AuditOutcome.SUCCESS, "Product created"));

		assertEquals(0, fakeRepository.count());
	}

	@Test
	void queryMethods_doNotModifyOrRemoveStoredRecords() {
		Actor admin = Actor.admin("admin-01");
		Actor customer = Actor.customer(java.util.UUID.randomUUID());

		auditService.record(customer, AuditAction.ORDER_PLACED, AuditEntityType.ORDER, "ORD-101", AuditOutcome.SUCCESS,
				"Order placed");

		auditService.record(admin, AuditAction.PRODUCT_CREATED, AuditEntityType.PRODUCT, "PROD-101",
				AuditOutcome.SUCCESS, "Product created");

		List<AuditRecord> before = fakeRepository.findAll();

		auditService.recent(admin, 10);
		auditService.forEntity(admin, AuditEntityType.ORDER, "ORD-101");
		auditService.forActor(admin, customer);
		auditService.forAction(admin, AuditAction.ORDER_PLACED);

		List<AuditRecord> after = fakeRepository.findAll();

		assertEquals(2, fakeRepository.count());
		assertEquals(before, after);
	}
}
