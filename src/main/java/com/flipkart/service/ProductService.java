/**
 * @author Aaditya Kumar
 */
package com.flipkart.service;

import java.util.List;

import com.flipkart.model.Actor;
import com.flipkart.model.Product;
import com.flipkart.model.ProductStatus;

public interface ProductService {
	
	List<Product> browseActiveProducts();
	List<Product> searchActiveProducts(String keyword);
	Product getProduct(String productId);
	Product getActiveProduct(String productId);
	List<Product> listAllProducts(Actor actor, ProductStatus statusFilter);
	
	default List<Product> listAllProducts(Actor actor) {
        return listAllProducts(actor, null);
    }
	
	List<Product> searchAllProducts(Actor actor, String keyword, ProductStatus statusFilter);
	
	default List<Product> searchAllProducts(Actor actor, String keyword) {
        return searchAllProducts(actor, keyword, null);
    }
}
