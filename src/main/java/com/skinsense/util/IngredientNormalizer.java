package com.skinsense.util;

import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Component
public class IngredientNormalizer {

    private static final Map<String, String> ALIASES = Map.ofEntries(
            Map.entry("vitamin b3", "niacinamide"),
            Map.entry("nicotinamide", "niacinamide"),
            Map.entry("zinc pca", "zinc pca"),
            Map.entry("bha", "salicylic acid"),
            Map.entry("beta hydroxy acid", "salicylic acid"),
            Map.entry("aha", "gentle exfoliating acids"),
            Map.entry("glycolic acid", "gentle exfoliating acids"),
            Map.entry("lactic acid", "gentle exfoliating acids"),
            Map.entry("alpha arbutin", "alpha arbutin"),
            Map.entry("txa", "tranexamic acid"),
            Map.entry("tranexamic", "tranexamic acid"),
            Map.entry("retinoids", "retinol"),
            Map.entry("retinoid", "retinol"),
            Map.entry("retinal", "retinol"),
            Map.entry("hyalouronic acid", "hyaluronic acid"),
            Map.entry("sodium hyaluronate", "hyaluronic acid"),
            Map.entry("cica", "centella asiatica"),
            Map.entry("centella", "centella asiatica"),
            Map.entry("snail secretion filtrate", "snail mucin"),
            Map.entry("snail mucin", "snail mucin"),
            Map.entry("peptide", "peptides"),
            Map.entry("peptides", "peptides"),
            Map.entry("ceramide", "ceramides"),
            Map.entry("ceramides", "ceramides"),
            Map.entry("parfum", "fragrance"),
            Map.entry("perfume", "fragrance"),
            Map.entry("artificial perfume", "fragrance")
    );

    public String normalize(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }

        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", " ")
                .trim()
                .replaceAll("\\s+", " ");

        return ALIASES.getOrDefault(normalized, normalized);
    }

    public boolean matches(String expected, String candidate) {
        String normalizedExpected = normalize(expected);
        String normalizedCandidate = normalize(candidate);

        if (normalizedExpected.isBlank() || normalizedCandidate.isBlank()) {
            return false;
        }

        return normalizedExpected.equals(normalizedCandidate)
                || ingredientFamily(normalizedExpected).contains(normalizedCandidate)
                || ingredientFamily(normalizedCandidate).contains(normalizedExpected);
    }

    private Set<String> ingredientFamily(String ingredient) {
        return switch (ingredient) {
            case "gentle exfoliating acids" -> Set.of("glycolic acid", "lactic acid", "mandelic acid", "aha");
            case "retinol" -> Set.of("retinal", "retinoid", "retinoids");
            case "hyaluronic acid" -> Set.of("sodium hyaluronate", "hydrolyzed hyaluronic acid");
            case "centella asiatica" -> Set.of("centella", "cica", "madecassoside", "asiaticoside");
            case "ceramides" -> Set.of("ceramide np", "ceramide ap", "ceramide eop");
            default -> Set.of();
        };
    }
}
