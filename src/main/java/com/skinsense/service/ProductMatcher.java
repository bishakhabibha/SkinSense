package com.skinsense.service;

import com.skinsense.dto.AssessmentRequest;
import com.skinsense.dto.CatalogProduct;
import com.skinsense.dto.ProductCatalog;
import com.skinsense.dto.ProductMatch;
import com.skinsense.dto.SkinAnalysis;
import com.skinsense.util.IngredientNormalizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class ProductMatcher {

    private static final int SKIN_TYPE_POINTS = 25;
    private static final int CONCERN_POINTS = 25;
    private static final int INGREDIENT_POINTS = 20;
    private static final int SENSITIVITY_POINTS = 15;
    private static final int PREFERENCE_POINTS = 10;
    private static final int OTHER_POINTS = 5;
    private static final int AVOID_INGREDIENT_PENALTY = 20;

    private final ProductCatalog productCatalog;
    private final IngredientNormalizer ingredientNormalizer;

    @Autowired
    public ProductMatcher(ProductCatalogService productCatalogService, IngredientNormalizer ingredientNormalizer) {
        this(productCatalogService.getProductCatalog(), ingredientNormalizer);
    }

    public ProductMatcher(ProductCatalog productCatalog, IngredientNormalizer ingredientNormalizer) {
        this.productCatalog = productCatalog;
        this.ingredientNormalizer = ingredientNormalizer;
    }

    public List<ProductMatch> rankProducts(String category, SkinAnalysis analysis, AssessmentRequest preferences) {
        return productCatalog.getProducts().stream()
                .map(product -> matchProduct(category, product, analysis, preferences))
                .filter(MatchAttempt::included)
                .map(MatchAttempt::match)
                .sorted(Comparator.comparingInt(ProductMatch::getCompatibilityScore).reversed()
                        .thenComparing(match -> match.getProduct().getPrice())
                        .thenComparing(match -> match.getProduct().getName()))
                .toList();
    }

    private MatchAttempt matchProduct(String requestedCategory, CatalogProduct product, SkinAnalysis analysis, AssessmentRequest preferences) {
        ProductMatch match = new ProductMatch();
        match.setProduct(product);

        List<String> filterNotes = new ArrayList<>();

        if (!product.isReadyForRecommendation()) {
            return MatchAttempt.excluded("Product is not marked ready for recommendation.");
        }

        if (!categoryMatches(requestedCategory, product.getCategory())) {
            return MatchAttempt.excluded("Product category does not match requested category.");
        }

        if (isSensitiveProfile(analysis) && "potentially_irritating".equals(normalize(product.getSensitivitySuitability()))) {
            return MatchAttempt.excluded("Potentially irritating products are excluded for sensitive profiles.");
        }

        if (preferences != null && requiresYes(preferences.getFragranceFree()) && Boolean.FALSE.equals(product.getFragranceFree())) {
            return MatchAttempt.excluded("Product is verified as not fragrance-free.");
        }

        if (preferences != null && requiresYes(preferences.getCrueltyFree()) && Boolean.FALSE.equals(product.getCrueltyFree())) {
            return MatchAttempt.excluded("Product is verified as not cruelty-free.");
        }

        int score = 0;
        score += scoreSkinType(product, analysis, match, filterNotes);
        score += scoreConcerns(product, analysis, match, filterNotes);
        score += scoreIngredients(product, analysis, match, filterNotes);
        score += scoreSensitivity(product, analysis, match, filterNotes);
        score += scorePreferences(product, preferences, match, filterNotes);
        score += scoreOther(product, match);
        score -= scoreAvoidIngredients(product, analysis, match);

        match.setCompatibilityScore(Math.max(score, 0));
        match.setFilterNotes(filterNotes);
        return MatchAttempt.included(match);
    }

    private int scoreSkinType(CatalogProduct product, SkinAnalysis analysis, ProductMatch match, List<String> filterNotes) {
        String userSkinType = normalize(analysis.getSkinType());
        List<String> productSkinTypes = normalizedList(product.getSkinTypes());

        if (productSkinTypes.isEmpty()) {
            filterNotes.add("Skin-type metadata is unknown, so no skin-type points were awarded.");
            return 0;
        }

        if (productSkinTypes.contains(userSkinType)) {
            match.getMatchedSkinTypes().add(userSkinType);
            match.getReasons().add("Matched skin type: " + userSkinType);
            return SKIN_TYPE_POINTS;
        }

        if ("oily".equals(userSkinType) && productSkinTypes.contains("combination")
                || "dry".equals(userSkinType) && productSkinTypes.contains("normal")
                || "combination".equals(userSkinType) && (productSkinTypes.contains("oily") || productSkinTypes.contains("normal"))) {
            match.getReasons().add("Related skin-type compatibility from catalog metadata.");
            return SKIN_TYPE_POINTS / 2;
        }

        return 0;
    }

    private int scoreConcerns(CatalogProduct product, SkinAnalysis analysis, ProductMatch match, List<String> filterNotes) {
        List<String> userConcerns = normalizedList(analysis.getConcerns());
        List<String> productConcerns = normalizedList(product.getConcerns());

        if (productConcerns.isEmpty() || userConcerns.isEmpty()) {
            filterNotes.add("Concern metadata is incomplete, so no concern points were awarded.");
            return 0;
        }

        List<String> matchedConcerns = userConcerns.stream()
                .filter(productConcerns::contains)
                .toList();
        match.setMatchedConcerns(matchedConcerns);

        if (matchedConcerns.isEmpty()) {
            return 0;
        }

        match.getReasons().add("Matched concerns: " + String.join(", ", matchedConcerns));
        return Math.min(CONCERN_POINTS, matchedConcerns.size() * 10);
    }

    private int scoreIngredients(CatalogProduct product, SkinAnalysis analysis, ProductMatch match, List<String> filterNotes) {
        Set<String> productIngredients = normalizedIngredients(product);
        if (productIngredients.isEmpty()) {
            filterNotes.add("Ingredient metadata is unknown, so no ingredient points were awarded.");
            return 0;
        }

        Set<String> matchedIngredients = new HashSet<>();
        for (String requestedIngredient : analysis.getRecommendedIngredients()) {
            for (String productIngredient : productIngredients) {
                if (ingredientNormalizer.matches(requestedIngredient, productIngredient)) {
                    matchedIngredients.add(ingredientNormalizer.normalize(requestedIngredient));
                }
            }
        }

        match.setMatchedIngredients(matchedIngredients.stream().sorted().toList());
        if (matchedIngredients.isEmpty()) {
            return 0;
        }

        match.getReasons().add("Relevant ingredients: " + String.join(", ", match.getMatchedIngredients()));
        return Math.min(INGREDIENT_POINTS, matchedIngredients.size() * 7);
    }

    private int scoreSensitivity(CatalogProduct product, SkinAnalysis analysis, ProductMatch match, List<String> filterNotes) {
        String suitability = normalize(product.getSensitivitySuitability());
        if (suitability.isBlank()) {
            filterNotes.add("Sensitivity suitability is unknown, so no sensitivity points were awarded.");
            return 0;
        }

        if (isSensitiveProfile(analysis)) {
            if ("low_risk".equals(suitability)) {
                match.getReasons().add("Sensitivity suitability: low risk.");
                return SENSITIVITY_POINTS;
            }
            if ("moderate".equals(suitability)) {
                match.getReasons().add("Sensitivity suitability: moderate.");
                return SENSITIVITY_POINTS / 2;
            }
            return 0;
        }

        if ("low_risk".equals(suitability) || "moderate".equals(suitability)) {
            return SENSITIVITY_POINTS / 2;
        }

        return 3;
    }

    private int scorePreferences(CatalogProduct product, AssessmentRequest preferences, ProductMatch match, List<String> filterNotes) {
        if (preferences == null) {
            return 0;
        }

        int score = 0;

        if (requiresYes(preferences.getFragranceFree())) {
            if (Boolean.TRUE.equals(product.getFragranceFree())) {
                score += PREFERENCE_POINTS / 2;
                match.getReasons().add("Preference match: verified fragrance-free.");
            } else {
                filterNotes.add("Fragrance-free status is unknown, so no fragrance-free preference points were awarded.");
            }
        }

        if (requiresYes(preferences.getCrueltyFree())) {
            if (Boolean.TRUE.equals(product.getCrueltyFree())) {
                score += PREFERENCE_POINTS / 2;
                match.getReasons().add("Preference match: verified cruelty-free.");
            } else {
                filterNotes.add("Cruelty-free status is unknown, so no cruelty-free preference points were awarded.");
            }
        }

        return score;
    }

    private int scoreOther(CatalogProduct product, ProductMatch match) {
        int score = 0;

        if (product.isIngredientsVerified()) {
            score += 3;
            match.getReasons().add("Ingredient list is verified.");
        }

        if (product.isImageVerified()) {
            score += 2;
        }

        return Math.min(score, OTHER_POINTS);
    }

    private int scoreAvoidIngredients(CatalogProduct product, SkinAnalysis analysis, ProductMatch match) {
        Set<String> productIngredients = normalizedIngredients(product);
        if (productIngredients.isEmpty() || analysis.getAvoidIngredients().isEmpty()) {
            return 0;
        }

        Set<String> avoidMatches = new HashSet<>();
        for (String avoidIngredient : analysis.getAvoidIngredients()) {
            for (String productIngredient : productIngredients) {
                if (ingredientNormalizer.matches(avoidIngredient, productIngredient)) {
                    avoidMatches.add(ingredientNormalizer.normalize(avoidIngredient));
                }
            }
        }

        match.setAvoidIngredientMatches(avoidMatches.stream().sorted().toList());
        if (avoidMatches.isEmpty()) {
            return 0;
        }

        match.getReasons().add("Contains an ingredient category marked for caution: " + String.join(", ", match.getAvoidIngredientMatches()));
        return Math.min(AVOID_INGREDIENT_PENALTY, avoidMatches.size() * 10);
    }

    private Set<String> normalizedIngredients(CatalogProduct product) {
        Set<String> ingredients = new HashSet<>();
        for (String ingredient : product.getKeyIngredients()) {
            String normalized = ingredientNormalizer.normalize(ingredient);
            if (!normalized.isBlank()) {
                ingredients.add(normalized);
            }
        }
        for (String ingredient : product.getIngredients()) {
            String normalized = ingredientNormalizer.normalize(ingredient);
            if (!normalized.isBlank()) {
                ingredients.add(normalized);
            }
        }
        return ingredients;
    }

    private boolean categoryMatches(String requestedCategory, String productCategory) {
        String requested = normalize(requestedCategory);
        String product = normalize(productCategory);

        if ("treatment".equals(requested)) {
            return Set.of("serum", "spot_treatment", "exfoliant").contains(product);
        }

        return requested.equals(product);
    }

    private boolean isSensitiveProfile(SkinAnalysis analysis) {
        return "sensitive".equals(normalize(analysis.getSkinType()))
                || "high".equals(normalize(analysis.getSensitivity()));
    }

    private boolean requiresYes(String value) {
        return "yes".equals(normalize(value)) || "true".equals(normalize(value));
    }

    private List<String> normalizedList(List<String> values) {
        if (values == null) {
            return List.of();
        }

        return values.stream()
                .map(this::normalize)
                .filter(value -> !value.isBlank())
                .toList();
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT).replace("-", "_").replace(" ", "_");
    }

    private record MatchAttempt(boolean included, ProductMatch match, String reason) {

        private static MatchAttempt included(ProductMatch match) {
            return new MatchAttempt(true, match, null);
        }

        private static MatchAttempt excluded(String reason) {
            return new MatchAttempt(false, null, reason);
        }
    }
}
