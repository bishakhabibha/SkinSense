package com.skinsense.dto;

import java.util.Locale;

/**
 * User-facing routine length targets. The number represents distinct,
 * meaningful skincare functions, not AM/PM placements or duplicate purchases.
 */
public enum RoutineLengthPreference {
    MINIMAL("minimal", 3, "Minimal"),
    STANDARD("standard", 5, "Standard"),
    ADVANCED("advanced", 7, "Advanced");

    private final String value;
    private final int targetStepCount;
    private final String label;

    RoutineLengthPreference(String value, int targetStepCount, String label) {
        this.value = value;
        this.targetStepCount = targetStepCount;
        this.label = label;
    }

    public String value() {
        return value;
    }

    public int targetStepCount() {
        return targetStepCount;
    }

    public String label() {
        return label;
    }

    public static RoutineLengthPreference from(String value) {
        String normalized = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
        for (RoutineLengthPreference preference : values()) {
            if (preference.value.equals(normalized)) {
                return preference;
            }
        }
        return MINIMAL;
    }
}
