package com.skinsense.util;

import com.skinsense.dto.CatalogProduct;
import com.skinsense.dto.ProductCatalog;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class ProductCatalogValidator {

    private ProductCatalogValidator() {
    }

    public static ValidationResult validate(ProductCatalog catalog) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        if (catalog == null) {
            errors.add("Catalog JSON could not be parsed.");
            return new ValidationResult(errors, warnings);
        }

        Set<String> allowedCategories = new HashSet<>(catalog.getAllowedCategories());
        Set<String> allowedSkinTypes = new HashSet<>(catalog.getAllowedSkinTypes());
        Set<String> allowedConcerns = new HashSet<>(catalog.getAllowedConcerns());
        Set<String> allowedSensitivity = new HashSet<>(catalog.getAllowedSensitivitySuitability());
        Set<String> ids = new HashSet<>();
        Set<String> productUrls = new HashSet<>();

        if (catalog.getProducts().isEmpty()) {
            errors.add("Catalog has no products.");
        }

        for (CatalogProduct product : catalog.getProducts()) {
            String label = product.getId() == null ? "(missing id)" : product.getId();

            requireText(product.getId(), label, "id", errors);
            requireText(product.getName(), label, "name", errors);
            requireText(product.getBrand(), label, "brand", errors);
            requireText(product.getCategory(), label, "category", errors);
            requireText(product.getCurrency(), label, "currency", errors);
            requireText(product.getRetailer(), label, "retailer", errors);
            requireText(product.getProductUrl(), label, "productUrl", errors);
            requireText(product.getImageUrl(), label, "imageUrl", errors);
            requireText(product.getPriceLastVerified(), label, "priceLastVerified", errors);
            requireText(product.getSensitivitySuitability(), label, "sensitivitySuitability", errors);

            if (product.getId() != null && !ids.add(product.getId())) {
                errors.add("Duplicate product id: " + product.getId());
            }

            if (product.getProductUrl() != null && !productUrls.add(product.getProductUrl())) {
                errors.add("Duplicate product URL: " + product.getProductUrl());
            }

            if (product.getPrice() == null || product.getPrice().signum() <= 0) {
                errors.add(label + " has an invalid price.");
            }

            if (product.getSizeValue() == null || product.getSizeValue().signum() <= 0) {
                errors.add(label + " has missing or invalid verified sizeValue.");
            }
            if (product.getSizeUnit() == null || !Set.of("ml", "g").contains(product.getSizeUnit().toLowerCase())) {
                errors.add(label + " has missing or unsupported sizeUnit.");
            }

            if (product.getCategory() != null && !allowedCategories.contains(product.getCategory())) {
                errors.add(label + " uses unsupported category: " + product.getCategory());
            }

            if (product.getSensitivitySuitability() != null
                    && !allowedSensitivity.contains(product.getSensitivitySuitability())) {
                errors.add(label + " uses unsupported sensitivitySuitability: " + product.getSensitivitySuitability());
            }

            for (String skinType : product.getSkinTypes()) {
                if (!allowedSkinTypes.contains(skinType)) {
                    errors.add(label + " uses unsupported skin type: " + skinType);
                }
            }

            for (String concern : product.getConcerns()) {
                if (!allowedConcerns.contains(concern)) {
                    errors.add(label + " uses unsupported concern: " + concern);
                }
            }

            validateUrl(product.getProductUrl(), label, "productUrl", errors);
            validateUrl(product.getImageUrl(), label, "imageUrl", errors);

            if (!product.isIngredientsVerified() || product.getIngredients().isEmpty()) {
                warnings.add(label + " does not have a fully verified ingredient list.");
            }

            if (!product.isReadyForRecommendation()) {
                warnings.add(label + " is not marked ready for recommendation.");
            }
        }

        return new ValidationResult(errors, warnings);
    }

    private static void requireText(String value, String productId, String field, List<String> errors) {
        if (value == null || value.isBlank()) {
            errors.add(productId + " is missing " + field + ".");
        }
    }

    private static void validateUrl(String value, String productId, String field, List<String> errors) {
        if (value == null || value.isBlank()) {
            return;
        }
        try {
            URI uri = new URI(value);
            if (uri.getScheme() == null || uri.getHost() == null) {
                errors.add(productId + " has malformed " + field + ": " + value);
            }
        } catch (URISyntaxException exception) {
            errors.add(productId + " has malformed " + field + ": " + value);
        }
    }

    public record ValidationResult(List<String> errors, List<String> warnings) {

        public boolean hasErrors() {
            return !errors.isEmpty();
        }
    }
}
