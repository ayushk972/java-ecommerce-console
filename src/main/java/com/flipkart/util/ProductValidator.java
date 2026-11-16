package com.flipkart.util;

import com.flipkart.exception.ValidationException;
import com.flipkart.model.NewProductRequest;
import com.flipkart.model.UpdateProductRequest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Validates product input against BR-P02. Every problem is collected and reported together in
 * one {@link ValidationException} (AC2). Stateless; all methods are static.
 *
 * <p>Name and category are measured after trimming and collapsing internal whitespace;
 * description is measured after trimming and may be null (treated as empty).
 * Name uniqueness (BR-P03) needs the repository and is checked in the service, not here.
 */
public final class ProductValidator {

    public static final int NAME_MIN = 2;
    public static final int NAME_MAX = 100;
    public static final int DESCRIPTION_MAX = 500;
    public static final int CATEGORY_MIN = 2;
    public static final int CATEGORY_MAX = 50;
    public static final int PRICE_MAX_DECIMALS = 2;
    public static final int STOCK_MIN = 0;

    private ProductValidator() {
    }

    /**
     * Validates a request to add a product (BR-P02, BR-P05).
     *
     * @throws ValidationException listing all field errors, or if the request is null
     */
    public static void validateNew(NewProductRequest request) {
        List<String> errors = new ArrayList<>();
        if (request == null) {
            errors.add("Product request is required");
        } else {
            checkName(request.name(), errors);
            checkDescription(request.description(), errors);
            checkCategory(request.category(), errors);
            checkPrice(request.price(), errors);
            checkStock(request.initialStock(), errors);
        }
        throwIfAny(errors);
    }

    /**
     * Validates a request to update a product (BR-P02, BR-P06). Stock is not part of an update.
     *
     * @throws ValidationException listing all field errors, or if the request is null
     */
    public static void validateUpdate(UpdateProductRequest request) {
        List<String> errors = new ArrayList<>();
        if (request == null) {
            errors.add("Product request is required");
        } else {
            checkName(request.name(), errors);
            checkDescription(request.description(), errors);
            checkCategory(request.category(), errors);
            checkPrice(request.price(), errors);
        }
        throwIfAny(errors);
    }

    private static void checkName(String name, List<String> errors) {
        if (isBlank(name)) {
            errors.add("Name is required");
            return;
        }
        int length = collapse(name).length();
        if (length < NAME_MIN || length > NAME_MAX) {
            errors.add("Name must be " + NAME_MIN + "-" + NAME_MAX + " characters");
        }
    }

    private static void checkDescription(String description, List<String> errors) {
        if (description != null && description.trim().length() > DESCRIPTION_MAX) {
            errors.add("Description must be at most " + DESCRIPTION_MAX + " characters");
        }
    }

    private static void checkCategory(String category, List<String> errors) {
        if (isBlank(category)) {
            errors.add("Category is required");
            return;
        }
        int length = collapse(category).length();
        if (length < CATEGORY_MIN || length > CATEGORY_MAX) {
            errors.add("Category must be " + CATEGORY_MIN + "-" + CATEGORY_MAX + " characters");
        }
    }

    private static void checkPrice(BigDecimal price, List<String> errors) {
        if (price == null) {
            errors.add("Price is required");
            return;
        }
        if (price.signum() <= 0) {
            errors.add("Price must be greater than 0");
            return;
        }
        if (price.stripTrailingZeros().scale() > PRICE_MAX_DECIMALS) {
            errors.add("Price must have at most " + PRICE_MAX_DECIMALS + " decimal places");
        }
        if (price.compareTo(AppConstants.MAX_PRICE) > 0) {
            errors.add("Price must not exceed " + AppConstants.MAX_PRICE.toPlainString());
        }
    }

    private static void checkStock(int stock, List<String> errors) {
        if (stock < STOCK_MIN || stock > AppConstants.MAX_STOCK) {
            errors.add("Stock must be between " + STOCK_MIN + " and " + AppConstants.MAX_STOCK);
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private static String collapse(String s) {
        // TODO(S1-03): replace with StringNormalizer.collapseWhitespace(s) once S1-03 merges.
        return s.trim().replaceAll("\\s+", " ");
    }

    private static void throwIfAny(List<String> errors) {
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }
}
