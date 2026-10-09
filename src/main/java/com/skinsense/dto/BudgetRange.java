package com.skinsense.dto;

import java.math.BigDecimal;
import java.util.Locale;

public enum BudgetRange {
    UNDER_2000("under-2000", "Under BDT 2,000", null, BigDecimal.valueOf(2000), false, true),
    BDT_2000_TO_4000("2000-4000", "BDT 2,000-BDT 4,000", BigDecimal.valueOf(2000), BigDecimal.valueOf(4000), true, true),
    BDT_4000_TO_6000("4000-6000", "BDT 4,000-BDT 6,000", BigDecimal.valueOf(4000), BigDecimal.valueOf(6000), true, true),
    BDT_6000_PLUS("6000-plus", "Around BDT 6,000+", BigDecimal.valueOf(6000), null, true, false);

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
        return maximum != null && amount != null && amount.compareTo(maximum) > 0;
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
            return BDT_2000_TO_4000;
        }

        String normalized = value.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "under-2000", "under_2000", "affordable" -> UNDER_2000;
            case "2000-4000", "2000_to_4000", "mid-range", "mid_range" -> BDT_2000_TO_4000;
            case "4000-6000", "4000_to_6000" -> BDT_4000_TO_6000;
            case "6000-plus", "6000_plus", "premium" -> BDT_6000_PLUS;
            default -> BDT_2000_TO_4000;
        };
    }
}
