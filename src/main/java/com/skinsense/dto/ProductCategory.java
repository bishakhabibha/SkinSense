package com.skinsense.dto;

import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/**
 * Central catalog taxonomy. Routine roles such as first cleanse and treatment
 * are defined here so matching rules do not drift across services.
 */
public enum ProductCategory {
    CLEANSER("cleanser"),
    MOISTURIZER("moisturizer"),
    SUNSCREEN("sunscreen"),
    SERUM("serum"),
    TONER("toner"),
    CLEANSING_OIL("cleansing_oil"),
    CLEANSING_BALM("cleansing_balm"),
    MICELLAR_WATER("micellar_water"),
    SPOT_TREATMENT("spot_treatment"),
    EXFOLIANT("exfoliant");

    private static final Set<ProductCategory> FIRST_CLEANSERS =
            Set.of(CLEANSING_OIL, CLEANSING_BALM, MICELLAR_WATER);
    private static final Set<ProductCategory> TREATMENTS =
            Set.of(SERUM, SPOT_TREATMENT, EXFOLIANT);

    private final String catalogValue;

    ProductCategory(String catalogValue) {
        this.catalogValue = catalogValue;
    }

    public String catalogValue() {
        return catalogValue;
    }

    public boolean isFirstCleanser() {
        return FIRST_CLEANSERS.contains(this);
    }

    public boolean isTreatment() {
        return TREATMENTS.contains(this);
    }

    public static Optional<ProductCategory> fromCatalogValue(String value) {
        String normalized = value == null ? "" : value.trim().toLowerCase(Locale.ROOT)
                .replace('-', '_').replace(' ', '_');
        for (ProductCategory category : values()) {
            if (category.catalogValue.equals(normalized)) {
                return Optional.of(category);
            }
        }
        return Optional.empty();
    }
}
