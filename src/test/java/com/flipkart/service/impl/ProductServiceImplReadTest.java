package com.flipkart.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import com.flipkart.exception.AuthorizationException;
import com.flipkart.exception.ResourceNotFoundException;
import com.flipkart.exception.ValidationException;
import com.flipkart.model.Actor;
import com.flipkart.model.Product;
import com.flipkart.model.ProductStatus;
import com.flipkart.repository.ProductRepository;

public class ProductServiceImplReadTest {

	private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

	private FakeProductRepository repository;
	private ProductServiceImpl service;

	private final Actor admin = Actor.admin("admin");

	@BeforeEach
	void setUp() {
		repository = new FakeProductRepository();
		service = new ProductServiceImpl(repository);

		// Deliberately inserted out of order to prove sorting.
		add("PRD-1003", "laptop Pro 15", "Powerful laptop for work", "Electronics", 5, ProductStatus.ACTIVE);
		add("PRD-1001", "Apple Watch", "Smart watch", "Electronics", 0, ProductStatus.ACTIVE);
		add("PRD-1002", "Java Handbook", "Learn OOP with examples", "Books", 10, ProductStatus.ACTIVE);
		add("PRD-1004", "Secret Gadget", "Hidden laptop accessory", "Electronics", 3, ProductStatus.HIDDEN);
		add("PRD-1005", "Old Laptop Bag", "Discontinued bag", "Fashion", 2, ProductStatus.DISCONTINUED);
	}

	/* ===== AC1 browse ===== */
	@Test
	void browseActiveProducts_mixedStatuses_returnsOnlyActive() {
		List<Product> result = service.browseActiveProducts();

		assertEquals(List.of("PRD-1001", "PRD-1002", "PRD-1003"), ids(result));
	}

	@Test
	void browseActiveProducts_stockZero_isIncluded() {
		List<Product> result = service.browseActiveProducts();

		assertTrue(result.stream().anyMatch(p -> p.getStockQuantity() == 0));
	}

	@Test
	void browseActiveProducts_sortsByNameCaseInsensitively() {
		// "laptop Pro 15" starts with a lower-case letter but must sort after "Java
		// Handbook".
		List<Product> result = service.browseActiveProducts();

		assertEquals(List.of("Apple Watch", "Java Handbook", "laptop Pro 15"), names(result));
	}

	@Test
	void browseActiveProducts_sameName_sortsById() {
		add("PRD-1010", "Twin", "a", "Misc", 1, ProductStatus.ACTIVE);
		add("PRD-1009", "twin", "b", "Misc", 1, ProductStatus.ACTIVE);

		List<Product> result = service.browseActiveProducts();

		List<String> twins = result.stream().map(Product::getId)
				.filter(id -> id.equals("PRD-1009") || id.equals("PRD-1010")).toList();
		assertEquals(List.of("PRD-1009", "PRD-1010"), twins);
	}

	@Test
	void browseActiveProducts_emptyRepository_returnsEmptyList() {
		ProductServiceImpl empty = new ProductServiceImpl(new FakeProductRepository());

		assertTrue(empty.browseActiveProducts().isEmpty());
	}

	/* ===== AC2 search ===== */

	@ParameterizedTest
	@ValueSource(strings = { "LAPTOP", "laptop", " laptop ", "lap", "  LaP  ", "pro 15", "Pro   15" })
	void searchActiveProducts_anyCaseOrSpacing_findsLaptopPro(String keyword) {
		List<Product> result = service.searchActiveProducts(keyword);

		assertEquals(List.of("PRD-1003"), ids(result));
	}

	@Test
	void searchActiveProducts_matchesCategory() {
		List<Product> result = service.searchActiveProducts("books");

		assertEquals(List.of("PRD-1002"), ids(result));
	}

	@Test
	void searchActiveProducts_matchesDescription() {
		List<Product> result = service.searchActiveProducts("OOP");

		assertEquals(List.of("PRD-1002"), ids(result));
	}

	@Test
	void searchActiveProducts_hiddenAndDiscontinuedNeverReturned() {
		// "laptop" also appears in a HIDDEN description and a DISCONTINUED name.
		List<Product> result = service.searchActiveProducts("laptop");

		assertEquals(List.of("PRD-1003"), ids(result));
	}

	@Test
	void searchActiveProducts_noMatch_returnsEmptyList() {
		assertTrue(service.searchActiveProducts("zzz-nothing").isEmpty());
	}

	@Test
	void searchActiveProducts_multipleMatches_sortedByName() {
		List<Product> result = service.searchActiveProducts("electronics");

		assertEquals(List.of("PRD-1001", "PRD-1003"), ids(result));
	}

	/* ===== AC3 blank keyword ===== */
	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = { " ", "   ", "\t", "\n" })
	void searchActiveProducts_blankKeyword_throwsValidationException(String keyword) {
		assertThrows(ValidationException.class, () -> service.searchActiveProducts(keyword));
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = { " ", "\t" })
	void searchAllProducts_blankKeyword_throwsValidationException(String keyword) {
		assertThrows(ValidationException.class, () -> service.searchAllProducts(admin, keyword));
	}

	/* ===== AC3 get product ===== */
	@Test
	void getProduct_anyStatus_returnsProduct() {
		assertEquals("PRD-1004", service.getProduct("PRD-1004").getId());
		assertEquals("PRD-1005", service.getProduct("PRD-1005").getId());
		assertEquals("PRD-1003", service.getProduct("PRD-1003").getId());
	}

	@Test
	void getProduct_unknownId_throwsResourceNotFound() {
		ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
				() -> service.getProduct("PRD-9999"));

		assertEquals("Product not found: PRD-9999", ex.getMessage());
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = { "  " })
	void getProduct_blankId_throwsValidationException(String id) {
		assertThrows(ValidationException.class, () -> service.getProduct(id));
	}

	@Test
	void getActiveProduct_activeProduct_returnsIt() {
		assertEquals("PRD-1001", service.getActiveProduct("PRD-1001").getId());
	}

	@Test
	void getActiveProduct_hiddenProduct_throwsResourceNotFound() {
		assertThrows(ResourceNotFoundException.class, () -> service.getActiveProduct("PRD-1004"));
	}

	@Test
	void getActiveProduct_discontinuedProduct_throwsResourceNotFound() {
		assertThrows(ResourceNotFoundException.class, () -> service.getActiveProduct("PRD-1005"));
	}

	@Test
	void getActiveProduct_unknownId_throwsResourceNotFound() {
		assertThrows(ResourceNotFoundException.class, () -> service.getActiveProduct("PRD-9999"));
	}

	/* ===== AC5 admin: authorization ===== */

	@Test
	void listAllProducts_adminActor_seesEveryStatus() {
		List<Product> result = service.listAllProducts(admin);

		assertEquals(5, result.size());
		assertTrue(result.stream().anyMatch(p -> p.getStatus() == ProductStatus.HIDDEN));
		assertTrue(result.stream().anyMatch(p -> p.getStatus() == ProductStatus.DISCONTINUED));
	}

	@Test
	void listAllProducts_adminActor_sortedByNameThenId() {
		List<Product> result = service.listAllProducts(admin);

		assertEquals(List.of("Apple Watch", "Java Handbook", "laptop Pro 15", "Old Laptop Bag", "Secret Gadget"),
				names(result));
	}

	@Test
	void listAllProducts_statusFilter_returnsOnlyThatStatus() {
		assertEquals(List.of("PRD-1004"), ids(service.listAllProducts(admin, ProductStatus.HIDDEN)));
		assertEquals(List.of("PRD-1005"), ids(service.listAllProducts(admin, ProductStatus.DISCONTINUED)));
		assertEquals(3, service.listAllProducts(admin, ProductStatus.ACTIVE).size());
	}

	@Test
	void listAllProducts_nonAdminActors_throwAuthorizationException() {
		assertThrows(AuthorizationException.class, () -> service.listAllProducts(Actor.guest()));
		assertThrows(AuthorizationException.class, () -> service.listAllProducts(Actor.customer(UUID.randomUUID())));
		assertThrows(AuthorizationException.class, () -> service.listAllProducts(null));
	}

	@Test
	void searchAllProducts_adminActor_findsHiddenAndDiscontinued() {
		List<Product> result = service.searchAllProducts(admin, "laptop");

		assertEquals(List.of("PRD-1003", "PRD-1005", "PRD-1004"), ids(result));
	}

	@Test
	void searchAllProducts_statusFilter_narrowsResults() {
		List<Product> result = service.searchAllProducts(admin, "laptop", ProductStatus.HIDDEN);

		assertEquals(List.of("PRD-1004"), ids(result));
	}

	@Test
	void searchAllProducts_nonAdminActors_throwAuthorizationException() {
		assertThrows(AuthorizationException.class, () -> service.searchAllProducts(Actor.guest(), "laptop"));
		assertThrows(AuthorizationException.class,
				() -> service.searchAllProducts(Actor.customer(UUID.randomUUID()), "laptop"));
		assertThrows(AuthorizationException.class, () -> service.searchAllProducts(null, "laptop"));
	}

	@Test
	void searchAllProducts_nonAdminWithBlankKeyword_failsAuthorizationFirst() {
		assertThrows(AuthorizationException.class, () -> service.searchAllProducts(Actor.guest(), " "));
	}

	/* ===== AC6 unmodifiable lists ===== */

	@Test
	void returnedLists_areUnmodifiable() {
		Product extra = product("PRD-2000", "Extra", "x", "x", 1, ProductStatus.ACTIVE);

		assertThrows(UnsupportedOperationException.class, () -> service.browseActiveProducts().add(extra));
		assertThrows(UnsupportedOperationException.class, () -> service.searchActiveProducts("lap").add(extra));
		assertThrows(UnsupportedOperationException.class, () -> service.listAllProducts(admin).add(extra));
		assertThrows(UnsupportedOperationException.class, () -> service.searchAllProducts(admin, "lap").add(extra));
	}

	@Test
	void returnedLists_doNotAffectRepository() {
		long before = repository.count();

		try {
			service.listAllProducts(admin).clear();
		} catch (UnsupportedOperationException expected) {
			// fine: the list is unmodifiable
		}

		assertEquals(before, repository.count());
	}

	@Test
	void constructor_nullRepository_throwsNullPointerException() {
		assertThrows(NullPointerException.class, () -> new ProductServiceImpl(null));
	}

	/* ===== helpers ===== */
	private void add(String id, String name, String description, String category, int stock, ProductStatus status) {
		repository.save(product(id, name, description, category, stock, status));
	}

	private static Product product(String id, String name, String description, String category, int stock,
			ProductStatus status) {
		return new Product(id, name, description, category, new BigDecimal("100.00"), stock, status, NOW, NOW);
	}

	private static List<String> ids(List<Product> products) {
		return products.stream().map(Product::getId).toList();
	}

	private static List<String> names(List<Product> products) {
		return products.stream().map(Product::getName).toList();
	}

	
	/** Nested fake: just enough of a repository to feed the service. */
    private static final class FakeProductRepository implements ProductRepository {

        private final Map<String, Product> store = new LinkedHashMap<>();

        @Override
        public Product save(Product p) {
            Objects.requireNonNull(p, "product");
            store.put(p.getId(), p);
            return p;
        }

        @Override
        public Optional<Product> findById(String id) {
            Objects.requireNonNull(id, "id");
            return Optional.ofNullable(store.get(id));
        }

        @Override
        public List<Product> findAll() {
            return new ArrayList<>(store.values());
        }

        @Override
        public List<Product> findAllByNormalizedName(String normalizedName) {
            return List.of();
        }

        @Override
        public long count() {
            return store.size();
        }
    }
}
