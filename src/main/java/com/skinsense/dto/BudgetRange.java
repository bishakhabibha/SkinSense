package com.skinsense.dto;

import java.math.BigDecimal;
import java.util.Locale;

public enum BudgetRange {
    UNDER_2000("under-2000", "Under ৳2,000", null, BigDecimal.valueOf(2000), false, false),
    UNDER_4000("under-4000", "Under ৳4,000", null, BigDecimal.valueOf(4000), false, false),
    UNDER_6000("under-6000", "Under ৳6,000", null, BigDecimal.valueOf(6000), false, false),
    ABOVE_6000("above-6000", "Above ৳6,000 (৳6,000 and above)", BigDecimal.valueOf(6000), null, true, false);

    private final String value;
    private final String label;
    private final BigDecimal minimum;
    private final BigDecimal maximum;
    private final boolean minimumInclusive;
    private final boolean maximumInclusive;

    BudgetRange(String value, String label, BigDecimal minimum, BigDecimal maximum, boolean minimumInclusive, boolean maximumInclusive) {
        this.value = value;
        this.label = label;
        this.minimum = minimum;
        this.maximum = maximum;
        this.minimumInclusive = minimumInclusive;
        this.maximumInclusive = maximumInclusive;
    }

    public String getValue() {
        return value;
    }

    public String getLabel() {
        return label;
    }

    public boolean contains(BigDecimal amount) {
        if (amount == null) {
            return false;
        }

        if (minimum != null) {
            int compareMinimum = amount.compareTo(minimum);
            if (compareMinimum < 0 || (!minimumInclusive && compareMinimum == 0)) {
                return false;
            }
        }

        if (maximum != null) {
            int compareMaximum = amount.compareTo(maximum);
            return compareMaximum < 0 || (maximumInclusive && compareMaximum == 0);
        }

        return true;
    }

    public boolean isOverBudget(BigDecimal amount) {
        if (maximum == null || amount == null) {
            return false;
        }
        int comparison = amount.compareTo(maximum);
        return comparison > 0 || (comparison == 0 && !maximumInclusive);
    }

    public BigDecimal distanceFromRange(BigDecimal amount) {
        if (amount == null || contains(amount)) {
            return BigDecimal.ZERO;
        }

        if (maximum != null && amount.compareTo(maximum) > 0) {
            return amount.subtract(maximum);
        }

        if (minimum != null && amount.compareTo(minimum) < 0) {
            return minimum.subtract(amount);
        }

        return BigDecimal.ZERO;
    }

    public static BudgetRange from(String value) {
        if (value == null || value.isBlank()) {
            return UNDER_4000;
        }

        String normalized = value.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "under-2000", "under_2000" -> UNDER_2000;
            case "under-4000", "under_4000" -> UNDER_4000;
            case "under-6000", "under_6000" -> UNDER_6000;
            case "above-6000", "above_6000" -> ABOVE_6000;
            default -> UNDER_4000;
        };
    }
}
