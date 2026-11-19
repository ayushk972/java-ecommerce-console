/**
 * @author Aaditya Kumar
 */
package com.flipkart.service.impl;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import com.flipkart.exception.AuthorizationException;
import com.flipkart.exception.ResourceNotFoundException;
import com.flipkart.exception.ValidationException;
import com.flipkart.model.Actor;
import com.flipkart.model.ActorType;
import com.flipkart.model.Product;
import com.flipkart.model.ProductStatus;
import com.flipkart.repository.ProductRepository;
import com.flipkart.service.ProductService;
import com.flipkart.util.StringNormalizer;

public class ProductServiceImpl implements ProductService {

	private static final String ENTITY_PRODUCT = "Product";

	private static final Comparator<Product> BY_NAME_THEN_ID = Comparator
			.comparing((Product p) -> StringNormalizer.normalize(p.getName())).thenComparing(Product::getId);

	private final ProductRepository productRepository;

	public ProductServiceImpl(ProductRepository productRepository) {
		this.productRepository = Objects.requireNonNull(productRepository, "productRepository");
	}

	@Override
	public List<Product> browseActiveProducts() {
		return sortedMatching(isShopperVisible());
	}

	@Override
	public List<Product> searchActiveProducts(String keyword) {
		String needle = requireKeyword(keyword);
		return sortedMatching(isShopperVisible().and(matches(needle)));
	}

	@Override
	public Product getProduct(String productId) {
		String id = requireId(productId);
		return productRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(ENTITY_PRODUCT, id));
	}

	@Override
	public Product getActiveProduct(String productId) {
		Product product = getProduct(productId);
		if (!product.getStatus().isVisibleToShoppers()) {
			// Same message as an unknown id, so shoppers cannot tell hidden from missing.
			throw new ResourceNotFoundException(ENTITY_PRODUCT, product.getId());
		}
		return product;
	}

	@Override
	public List<Product> listAllProducts(Actor actor, ProductStatus statusFilter) {
		requireAdmin(actor);
		return sortedMatching(hasStatus(statusFilter));
	}

	@Override
	public List<Product> searchAllProducts(Actor actor, String keyword, ProductStatus statusFilter) {
		requireAdmin(actor);
		String needle = requireKeyword(keyword);
		return sortedMatching(hasStatus(statusFilter).and(matches(needle)));
	}

	/* ===== helpers ===== */
	private List<Product> sortedMatching(Predicate<Product> filter) {
		return productRepository.findAll().stream().filter(filter).sorted(BY_NAME_THEN_ID).toList(); // Stream.toList()
																										// is
																										// unmodifiable
	}

	private static Predicate<Product> isShopperVisible() {
		return p -> p.getStatus().isVisibleToShoppers();
	}

	private static Predicate<Product> hasStatus(ProductStatus statusFilter) {
		return p -> statusFilter == null || p.getStatus() == statusFilter;
	}

	private static Predicate<Product> matches(String normalizedNeedle) {
		return p -> StringNormalizer.containsNormalized(p.getName(), normalizedNeedle)
				|| StringNormalizer.containsNormalized(p.getCategory(), normalizedNeedle)
				|| StringNormalizer.containsNormalized(p.getDescription(), normalizedNeedle);
	}

	private static String requireKeyword(String keyword) {
		String normalized = StringNormalizer.normalize(keyword);
		if (normalized.isEmpty()) {
			throw new ValidationException(List.of("Search keyword must not be blank"));
		}
		return normalized;
	}

	private static String requireId(String productId) {
		if (productId == null || productId.isBlank()) {
			throw new ValidationException(List.of("Product id must not be blank"));
		}
		return productId.trim();
	}

	private static void requireAdmin(Actor actor) {
		if (actor == null || actor.type() != ActorType.ADMIN) {
			throw new AuthorizationException("Access denied");
		}
	}

}
