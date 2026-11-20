
package com.flipkart.seed;

import java.math.BigDecimal;
import java.util.List;

/**
 * Contains predefined product information used to initialize the e-commerce
 * product catalogue.
 */
public final class ProductSeedData {

	private ProductSeedData() {
		// Prevent object creation.
	}

	/**
	 * Represents the initial information for one product.
	 */
	public record SeedProduct(String name, String description, String category, BigDecimal price, int stockQuantity,
			boolean hidden) {
	}

	/**
	 * Returns the predefined list of 15 products.
	 */
	public static List<SeedProduct> getProducts() {

		return List.of(
				new SeedProduct("Laptop Pro 15", "High performance laptop for programming and office work.",
						"Electronics", new BigDecimal("55000.00"), 10, false),
				
				new SeedProduct("Smartphone X", "Smartphone with advanced camera and display.", "Electronics",
						new BigDecimal("22000.00"), 5, false),
				
				new SeedProduct("Wireless Headphones", "Wireless headphones with noise cancellation.", "Electronics",
						new BigDecimal("2500.00"), 0, false),
				
				new SeedProduct("Bluetooth Speaker", "Portable speaker with clear sound.", "Electronics",
						new BigDecimal("1800.00"), 3, false),
				
				new SeedProduct("Mechanical Keyboard", "Mechanical keyboard for daily computer use.", "Electronics",
						new BigDecimal("3200.00"), 8, false),
				
				new SeedProduct("Java Programming Book", "A practical guide to Java programming.", "Books",
						new BigDecimal("650.00"), 20, false),
				
				new SeedProduct("Data Structures Book", "Learn data structures and algorithms.", "Books",
						new BigDecimal("750.00"), 0, false),
				
				new SeedProduct("Database Management Book", "Introduction to database management systems.", "Books",
						new BigDecimal("550.00"), 12, false),
				
				new SeedProduct("Men Cotton T-Shirt", "Comfortable cotton casual T-shirt.", "Fashion",
						new BigDecimal("499.00"), 15, false),
				
				new SeedProduct("Running Shoes", "Lightweight shoes for everyday running.", "Fashion",
						new BigDecimal("1999.00"), 4, false),
				
				new SeedProduct("Casual Backpack", "Durable backpack for college and travel.", "Fashion",
						new BigDecimal("899.00"), 7, false),
				
				new SeedProduct("Wrist Watch", "Classic wrist watch for everyday use.", "Fashion",
						new BigDecimal("1499.00"), 10, false),
				
				new SeedProduct("Electric Kettle", "Electric kettle for quickly boiling water.", "Home and Kitchen",
						new BigDecimal("999.00"), 6, false),
				
				new SeedProduct("Mixer Grinder", "Kitchen mixer grinder for daily cooking.", "Home and Kitchen",
						new BigDecimal("2499.00"), 9, false),
				
				new SeedProduct("Old Model Speaker", "Demonstration product with hidden status.", "Home and Kitchen",
						new BigDecimal("1200.00"), 5, true));
	}
}
