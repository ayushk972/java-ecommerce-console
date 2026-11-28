/**
 * @author Aaditya Kumar
 */
package com.flipkart.service.impl;

import static org.junit.jupiter.api.Assertions.*;

import com.flipkart.exception.AuthorizationException;
import com.flipkart.exception.InsufficientStockException;
import com.flipkart.exception.ResourceNotFoundException;
import com.flipkart.exception.ValidationException;
import com.flipkart.model.Actor;
import com.flipkart.model.AuditAction;
import com.flipkart.model.AuditEntityType;
import com.flipkart.model.AuditOutcome;
import com.flipkart.model.Product;
import com.flipkart.model.ProductStatus;
import com.flipkart.model.StockRequest;
import com.flipkart.repository.ProductRepository;
import com.flipkart.service.AuditService;

import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class StockServiceImplTest {

	private static final Instant T0 = Instant.parse("2026-11-01T10:00:00Z");
	private static final Instant NOW = Instant.parse("2026-11-20T10:00:00Z");
	private static final Actor ADMIN = Actor.admin("admin");

	private FakeProductRepository repository;
	private RecordingAudit audit;
	private StockServiceImpl service;

	@BeforeEach
	void setUp() {
		repository = new FakeProductRepository();
		audit = new RecordingAudit();
		service = new StockServiceImpl(repository, audit.service, Clock.fixed(NOW, ZoneOffset.UTC));
	}

	private void seed(String id, int stock, ProductStatus status) {
		repository.seed(
				new Product(id, "Phone " + id, "desc", "Electronics", new BigDecimal("10.00"), stock, status, T0, T0));
	}

	private int stockOf(String id) {
		return repository.findById(id).orElseThrow().getStockQuantity();
	}

	// ---------------------------------------------------------------- increase

	@Nested
	class IncreaseStock {

		@Test
		void validAdmin_addsStock_updatesTimestamp_andAudits() {
			seed("P1", 10, ProductStatus.ACTIVE);

			service.increaseStock("P1", 5, ADMIN, "Restock");

			assertEquals(15, stockOf("P1"));
			assertEquals(NOW, repository.findById("P1").orElseThrow().getUpdatedAt());
			assertEquals(1, audit.calls.size());
			RecordingAudit.Call call = audit.calls.get(0);
			assertEquals(AuditAction.STOCK_INCREASED, call.action());
			assertEquals(AuditEntityType.PRODUCT, call.entityType());
			assertEquals("P1", call.entityId());
			assertEquals(AuditOutcome.SUCCESS, call.outcome());
			assertTrue(call.details().contains("before=10"));
			assertTrue(call.details().contains("after=15"));
			assertTrue(call.details().contains("Restock"));
		}

		@Test
		void doesNotTouchPriceNameOrStatus() {
			seed("P1", 10, ProductStatus.HIDDEN);

			service.increaseStock("P1", 1, ADMIN, "Restock");

			Product p = repository.findById("P1").orElseThrow();
			assertEquals(new BigDecimal("10.00"), p.getPrice());
			assertEquals("Phone P1", p.getName());
			assertEquals(ProductStatus.HIDDEN, p.getStatus());
		}

		@Test
		void nonAdminActors_areRejected() {
			seed("P1", 10, ProductStatus.ACTIVE);

			assertThrows(AuthorizationException.class,
					() -> service.increaseStock("P1", 1, Actor.customer(UUID.randomUUID()), "x"));
			assertThrows(AuthorizationException.class, () -> service.increaseStock("P1", 1, Actor.guest(), "x"));
			assertThrows(AuthorizationException.class, () -> service.increaseStock("P1", 1, null, "x"));
			assertEquals(10, stockOf("P1"));
			assertTrue(audit.calls.isEmpty());
		}

		@Test
		void quantityZeroOrNegative_isRejected() {
			seed("P1", 10, ProductStatus.ACTIVE);

			assertThrows(ValidationException.class, () -> service.increaseStock("P1", 0, ADMIN, "x"));
			assertThrows(ValidationException.class, () -> service.increaseStock("P1", -1, ADMIN, "x"));
			assertEquals(10, stockOf("P1"));
		}

		@Test
		void reason_blankNullOrTooLong_isRejected_andExactly100IsAccepted() {
			seed("P1", 10, ProductStatus.ACTIVE);

			assertThrows(ValidationException.class, () -> service.increaseStock("P1", 1, ADMIN, null));
			assertThrows(ValidationException.class, () -> service.increaseStock("P1", 1, ADMIN, "   "));
			assertThrows(ValidationException.class, () -> service.increaseStock("P1", 1, ADMIN, "x".repeat(101)));

			service.increaseStock("P1", 1, ADMIN, "x".repeat(100));
			assertEquals(11, stockOf("P1"));
		}

		@Test
		void validationErrors_areReportedTogether() {
			seed("P1", 10, ProductStatus.ACTIVE);

			ValidationException ex = assertThrows(ValidationException.class,
					() -> service.increaseStock("P1", 0, ADMIN, " "));

			assertEquals(2, ex.getErrors().size());
		}

		@Test
		void cap_exactly1_000_000_isAllowed_andOneMoreIsRejected() {
			seed("P1", 999_990, ProductStatus.ACTIVE);

			assertThrows(ValidationException.class, () -> service.increaseStock("P1", 11, ADMIN, "x"));
			assertEquals(999_990, stockOf("P1"));

			service.increaseStock("P1", 10, ADMIN, "x");
			assertEquals(1_000_000, stockOf("P1"));
		}

		@Test
		void integerMaxValue_isRejected_withoutOverflow() {
			seed("P1", 5, ProductStatus.ACTIVE);

			assertThrows(ValidationException.class, () -> service.increaseStock("P1", Integer.MAX_VALUE, ADMIN, "x"));
			assertEquals(5, stockOf("P1"));
		}

		@Test
		void hiddenProduct_isAllowed() {
			seed("P1", 10, ProductStatus.HIDDEN);

			service.increaseStock("P1", 2, ADMIN, "x");

			assertEquals(12, stockOf("P1"));
		}

		@Test
		void discontinuedProduct_isRejected() {
			seed("P1", 10, ProductStatus.DISCONTINUED);

			assertThrows(ValidationException.class, () -> service.increaseStock("P1", 1, ADMIN, "x"));
			assertEquals(10, stockOf("P1"));
		}

		@Test
		void unknownProduct_throwsResourceNotFound() {
			assertThrows(ResourceNotFoundException.class, () -> service.increaseStock("NOPE", 1, ADMIN, "x"));
		}

		@Test
		void nullOrBlankProductId_isValidationError() {
			assertThrows(ValidationException.class, () -> service.increaseStock(null, 1, ADMIN, "x"));
			assertThrows(ValidationException.class, () -> service.increaseStock(" ", 1, ADMIN, "x"));
		}
	}

	// ---------------------------------------------------------------- decrease

	@Nested
	class DecreaseStock {

		@Test
		void validAdmin_removesStock_andAudits() {
			seed("P1", 10, ProductStatus.ACTIVE);

			service.decreaseStock("P1", 4, ADMIN, "Damaged");

			assertEquals(6, stockOf("P1"));
			RecordingAudit.Call call = audit.calls.get(0);
			assertEquals(AuditAction.STOCK_DECREASED, call.action());
			assertTrue(call.details().contains("before=10"));
			assertTrue(call.details().contains("after=6"));
		}

		@Test
		void decreaseToExactlyZero_isAllowed() {
			seed("P1", 5, ProductStatus.ACTIVE);

			service.decreaseStock("P1", 5, ADMIN, "Sold offline");

			assertEquals(0, stockOf("P1"));
		}

		@Test
		void decreaseByMoreThanStock_throws_andChangesNothing() {
			seed("P1", 5, ProductStatus.ACTIVE);

			InsufficientStockException ex = assertThrows(InsufficientStockException.class,
					() -> service.decreaseStock("P1", 6, ADMIN, "x"));

			assertEquals(6, ex.getRequested());
			assertEquals(5, ex.getAvailable());
			assertEquals(5, stockOf("P1"));
			assertTrue(audit.calls.isEmpty());
			assertEquals(0, repository.saveCalls());
		}

		@Test
		void integerMaxValue_isInsufficientStock() {
			seed("P1", 5, ProductStatus.ACTIVE);

			assertThrows(InsufficientStockException.class,
					() -> service.decreaseStock("P1", Integer.MAX_VALUE, ADMIN, "x"));
			assertEquals(5, stockOf("P1"));
		}

		@Test
		void nonAdmin_discontinued_unknown_andBadInput_areRejected() {
			seed("P1", 5, ProductStatus.ACTIVE);
			seed("P2", 5, ProductStatus.DISCONTINUED);

			assertThrows(AuthorizationException.class, () -> service.decreaseStock("P1", 1, Actor.guest(), "x"));
			assertThrows(ValidationException.class, () -> service.decreaseStock("P2", 1, ADMIN, "x"));
			assertThrows(ResourceNotFoundException.class, () -> service.decreaseStock("NOPE", 1, ADMIN, "x"));
			assertThrows(ValidationException.class, () -> service.decreaseStock("P1", 0, ADMIN, "x"));
			assertThrows(ValidationException.class, () -> service.decreaseStock("P1", 1, ADMIN, ""));
		}
	}

	// ---------------------------------------------------------------- reserve

	@Nested
	class ReserveAll {

		@Test
		void reservesAllLines_andAuditsEachProduct() {
			seed("P1", 10, ProductStatus.ACTIVE);
			seed("P2", 4, ProductStatus.ACTIVE);

			service.reserveAll(List.of(new StockRequest("P1", 3), new StockRequest("P2", 4)), Actor.system(),
					"ORD-000001");

			assertEquals(7, stockOf("P1"));
			assertEquals(0, stockOf("P2"));
			assertEquals(2, audit.calls.size());
			assertEquals(AuditAction.STOCK_RESERVED, audit.calls.get(0).action());
			assertTrue(audit.calls.get(0).details().contains("ORD-000001"));
			assertTrue(audit.calls.get(0).details().contains("before=10"));
			assertTrue(audit.calls.get(0).details().contains("after=7"));
		}

		@Test
		void duplicateProductIds_areAggregated() {
			seed("P1", 10, ProductStatus.ACTIVE);

			service.reserveAll(List.of(new StockRequest("P1", 2), new StockRequest("P1", 3)), Actor.system(),
					"ORD-000001");

			assertEquals(5, stockOf("P1"));
			assertEquals(1, repository.saveCalls());
			assertEquals(1, audit.calls.size());
		}

		@Test
		void duplicatesThatTogetherExceedStock_areRejected() {
			seed("P1", 4, ProductStatus.ACTIVE);

			assertThrows(InsufficientStockException.class,
					() -> service.reserveAll(List.of(new StockRequest("P1", 2), new StockRequest("P1", 3)),
							Actor.system(), "ORD-000001"));
			assertEquals(4, stockOf("P1"));
		}

		@Test
		void oneInsufficientLine_changesNothing() {
			seed("P1", 10, ProductStatus.ACTIVE);
			seed("P2", 1, ProductStatus.ACTIVE);

			assertThrows(InsufficientStockException.class,
					() -> service.reserveAll(List.of(new StockRequest("P1", 3), new StockRequest("P2", 5)),
							Actor.system(), "ORD-000001"));

			assertEquals(10, stockOf("P1"));
			assertEquals(1, stockOf("P2"));
			assertEquals(0, repository.saveCalls());
			assertTrue(audit.calls.isEmpty());
		}

		@Test
		void unknownOrNonActiveProduct_changesNothing() {
			seed("P1", 10, ProductStatus.ACTIVE);
			seed("P2", 10, ProductStatus.HIDDEN);
			seed("P3", 10, ProductStatus.DISCONTINUED);

			assertThrows(ResourceNotFoundException.class,
					() -> service.reserveAll(List.of(new StockRequest("P1", 1), new StockRequest("NOPE", 1)),
							Actor.system(), "ref"));
			assertThrows(ValidationException.class, () -> service
					.reserveAll(List.of(new StockRequest("P1", 1), new StockRequest("P2", 1)), Actor.system(), "ref"));
			assertThrows(ValidationException.class, () -> service
					.reserveAll(List.of(new StockRequest("P1", 1), new StockRequest("P3", 1)), Actor.system(), "ref"));

			assertEquals(10, stockOf("P1"));
			assertEquals(0, repository.saveCalls());
		}

		@Test
		void saveFailsOnSecondProduct_firstIsCompensated() {
			seed("P1", 10, ProductStatus.ACTIVE);
			seed("P2", 10, ProductStatus.ACTIVE);
			repository.failOnSaveCall(2);

			assertThrows(IllegalStateException.class,
					() -> service.reserveAll(List.of(new StockRequest("P1", 3), new StockRequest("P2", 4)),
							Actor.system(), "ORD-000001"));

			assertEquals(10, stockOf("P1")); // put back
			assertEquals(10, stockOf("P2"));
			assertTrue(audit.calls.isEmpty());
		}

		@Test
		void badInput_isValidationError() {
			seed("P1", 10, ProductStatus.ACTIVE);

			assertThrows(ValidationException.class, () -> service.reserveAll(List.of(), Actor.system(), "ref"));
			assertThrows(ValidationException.class, () -> service.reserveAll(null, Actor.system(), "ref"));
			assertThrows(ValidationException.class,
					() -> service.reserveAll(List.of(new StockRequest("P1", 0)), Actor.system(), "ref"));
			assertThrows(ValidationException.class,
					() -> service.reserveAll(List.of(new StockRequest("P1", 1)), Actor.system(), " "));
		}

		@Test
		void nullEntryInList_isValidationError_andChangesNothing() {
			seed("P1", 10, ProductStatus.ACTIVE);
			List<StockRequest> requests = new ArrayList<>();
			requests.add(new StockRequest("P1", 1));
			requests.add(null); // List.of(...) would reject null itself

			ValidationException ex = assertThrows(ValidationException.class,
					() -> service.reserveAll(requests, Actor.system(), "ref"));

			assertTrue(ex.getErrors().contains("Stock request must not be null"));
			assertEquals(10, stockOf("P1"));
			assertEquals(0, repository.saveCalls());
		}

		@Test
		void blankProductId_isValidationError() {
			for (String badId : new String[] { null, "", "   " }) {
				ValidationException ex = assertThrows(ValidationException.class,
						() -> service.reserveAll(List.of(new StockRequest(badId, 1)), Actor.system(), "ref"));

				assertTrue(ex.getErrors().contains("Product ID is required"));
			}
			assertEquals(0, repository.saveCalls());
		}

		@Test
		void rollbackAlsoFails_keepsRollingBack_andSuppressesTheRollbackFailure() {
			seed("P1", 10, ProductStatus.ACTIVE);
			seed("P2", 10, ProductStatus.ACTIVE);
			seed("P3", 10, ProductStatus.ACTIVE);
			// save #1 P1 ok, #2 P2 ok, #3 P3 fails (the real failure),
			// rollback: #4 (restore P2) fails, #5 (restore P1) succeeds
			repository.failOnSaveCall(3);
			repository.failOnSaveCall(4);

			IllegalStateException ex = assertThrows(IllegalStateException.class,
					() -> service.reserveAll(
							List.of(new StockRequest("P1", 1), new StockRequest("P2", 2), new StockRequest("P3", 3)),
							Actor.system(), "ORD-000001"));

			assertEquals("simulated failure on save #3", ex.getMessage()); // original is rethrown
			assertEquals(1, ex.getSuppressed().length); // rollback failure attached
			assertEquals("simulated failure on save #4", ex.getSuppressed()[0].getMessage());
			assertEquals(10, stockOf("P1")); // rollback kept going and restored P1
			assertEquals(8, stockOf("P2")); // its own rollback failed, so it stays reserved
			assertEquals(10, stockOf("P3")); // never saved
			assertTrue(audit.calls.isEmpty());
		}

		@Test
		void aggregatedQuantityOverflow_isValidationError() {
			seed("P1", 10, ProductStatus.ACTIVE);

			assertThrows(ValidationException.class,
					() -> service.reserveAll(
							List.of(new StockRequest("P1", Integer.MAX_VALUE), new StockRequest("P1", 1)),
							Actor.system(), "ref"));
			assertEquals(10, stockOf("P1"));
		}
	}

	// ---------------------------------------------------------------- release

	@Nested
	class ReleaseAll {

		@Test
		void restoresStock_forAnyStatus_andAudits() {
			seed("P1", 5, ProductStatus.ACTIVE);
			seed("P2", 5, ProductStatus.HIDDEN);
			seed("P3", 5, ProductStatus.DISCONTINUED);

			service.releaseAll(List.of(new StockRequest("P1", 2), new StockRequest("P2", 3), new StockRequest("P3", 4)),
					Actor.system(), "ORD-000001");

			assertEquals(7, stockOf("P1"));
			assertEquals(8, stockOf("P2"));
			assertEquals(9, stockOf("P3"));
			assertEquals(3, audit.calls.size());
			assertEquals(AuditAction.STOCK_RELEASED, audit.calls.get(0).action());
			assertTrue(audit.calls.get(0).details().contains("ORD-000001"));
		}

		@Test
		void duplicateIds_areAggregated() {
			seed("P1", 5, ProductStatus.ACTIVE);

			service.releaseAll(List.of(new StockRequest("P1", 2), new StockRequest("P1", 3)), Actor.system(), "ref");

			assertEquals(10, stockOf("P1"));
			assertEquals(1, audit.calls.size());
		}

		@Test
		void respectsTheCap_andChangesNothingOnFailure() {
			seed("P1", 999_999, ProductStatus.ACTIVE);
			seed("P2", 5, ProductStatus.ACTIVE);

			assertThrows(ValidationException.class, () -> service
					.releaseAll(List.of(new StockRequest("P2", 1), new StockRequest("P1", 2)), Actor.system(), "ref"));

			assertEquals(999_999, stockOf("P1"));
			assertEquals(5, stockOf("P2"));
			assertEquals(0, repository.saveCalls());
		}

		@Test
		void unknownProduct_throwsResourceNotFound_andChangesNothing() {
			seed("P1", 5, ProductStatus.ACTIVE);

			assertThrows(ResourceNotFoundException.class,
					() -> service.releaseAll(List.of(new StockRequest("P1", 1), new StockRequest("NOPE", 1)),
							Actor.system(), "ref"));
			assertEquals(5, stockOf("P1"));
		}

		@Test
		void saveFailure_isCompensated() {
			seed("P1", 5, ProductStatus.ACTIVE);
			seed("P2", 5, ProductStatus.ACTIVE);
			repository.failOnSaveCall(2);

			assertThrows(IllegalStateException.class, () -> service
					.releaseAll(List.of(new StockRequest("P1", 1), new StockRequest("P2", 1)), Actor.system(), "ref"));

			assertEquals(5, stockOf("P1"));
			assertEquals(5, stockOf("P2"));
			assertTrue(audit.calls.isEmpty());
		}
	}

	// ---------------------------------------------------------------- fakes

	static class FakeProductRepository implements ProductRepository {
		private final Map<String, Product> store = new LinkedHashMap<>();
		private final java.util.Set<Integer> failingSaves = new java.util.HashSet<>();
		private int saveCalls;

		void seed(Product p) {
			store.put(p.getId(), p);
		}

		/** Makes the Nth call to save(...) throw. Can be called more than once. */
		void failOnSaveCall(int n) {
			failingSaves.add(n);
		}

		int saveCalls() {
			return saveCalls;
		}

		@Override
		public Product save(Product p) {
			saveCalls++;
			if (failingSaves.contains(saveCalls)) {
				throw new IllegalStateException("simulated failure on save #" + saveCalls);
			}
			store.put(p.getId(), p);
			return p;
		}

		@Override
		public Optional<Product> findById(String id) {
			return Optional.ofNullable(store.get(id));
		}

		@Override
		public List<Product> findAll() {
			return List.copyOf(store.values());
		}

		@Override
		public List<Product> findAllByNormalizedName(String normalizedName) {
			return store.values().stream().filter(p -> p.normalizedName().equals(normalizedName)).toList();
		}

		@Override
		public long count() {
			return store.size();
		}
	}

	/**
	 * Records every record(...) call made on an AuditService. It uses a proxy, so
	 * it keeps compiling even when AuditService has other methods (recent,
	 * forEntity, ...).
	 */
	static class RecordingAudit {
		record Call(Actor actor, AuditAction action, AuditEntityType entityType, String entityId, AuditOutcome outcome,
				String details) {
		}

		final List<Call> calls = new ArrayList<>();

		final AuditService service = (AuditService) Proxy.newProxyInstance(AuditService.class.getClassLoader(),
				new Class<?>[] { AuditService.class }, (proxy, method, args) -> {
					if (method.getDeclaringClass() == Object.class) {
						return switch (method.getName()) {
						case "hashCode" -> 0;
						case "equals" -> proxy == args[0];
						default -> "RecordingAudit";
						};
					}
					if (method.getName().equals("record")) {
						calls.add(new Call((Actor) args[0], (AuditAction) args[1], (AuditEntityType) args[2],
								(String) args[3], (AuditOutcome) args[4], (String) args[5]));
					}
					return null;
				});
	}

}