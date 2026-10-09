package com.skinsense;

import com.skinsense.dto.AssessmentRequest;
import com.skinsense.dto.BudgetRange;
import com.skinsense.dto.CatalogProduct;
import com.skinsense.dto.ProductCatalog;
import com.skinsense.dto.RoutineProductStep;
import com.skinsense.dto.RoutineNeeds;
import com.skinsense.dto.SkinAnalysis;
import com.skinsense.dto.SkincareRoutine;
import com.skinsense.service.ProductMatcher;
import com.skinsense.service.RoutineOptimizer;
import com.skinsense.util.IngredientNormalizer;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RoutineOptimizerTests {

    @Test
    void oilyAcneCanFitUnderTwoThousand() {
        RoutineOptimizer optimizer = optimizer(catalog(
                product("cleanser-budget", "Budget Acne Cleanser", "cleanser", 500).skin("oily").concerns("acne").ingredients("salicylic acid").build(),
                product("treatment-budget", "Budget Acne Treatment", "serum", 600).skin("oily").concerns("acne").ingredients("niacinamide").build(),
                product("moisturizer-budget", "Budget Gel Moisturizer", "moisturizer", 400).skin("oily").concerns("acne").ingredients("glycerin").build(),
                product("sunscreen-budget", "Budget Sunscreen", "sunscreen", 450).skin("oily").concerns("acne").ingredients("niacinamide").build(),
                product("treatment-expensive", "Expensive Acne Serum", "serum", 1800).skin("oily").concerns("acne").ingredients("salicylic acid", "niacinamide").build()
        ));

        SkincareRoutine routine = optimizer.optimize(analysis("oily", "moderate", true, true, "acne"), assessment("under-2000"));

        assertThat(routine.isExactBudgetFit()).isTrue();
        assertThat(routine.getBudgetRange()).isEqualTo(BudgetRange.UNDER_2000);
        assertThat(routine.getTotalCost()).isLessThan(BigDecimal.valueOf(2000));
        assertThat(productIds(routine)).contains("cleanser-budget", "sunscreen-budget");
    }

    @Test
    void dryHyperpigmentationFitsUnderFourThousand() {
        RoutineOptimizer optimizer = optimizer(catalog(
                product("dry-cleanser", "Dry Cleanser", "cleanser", 700).skin("dry").concerns("redness").ingredients("glycerin").build(),
                product("pigment-treatment", "Pigment Treatment", "serum", 1100).skin("dry").concerns("hyperpigmentation").ingredients("alpha arbutin").build(),
                product("dry-moisturizer", "Dry Moisturizer", "moisturizer", 900).skin("dry").concerns("redness").ingredients("ceramides").build(),
                product("dry-sunscreen", "Dry Sunscreen", "sunscreen", 900).skin("dry").concerns("hyperpigmentation").ingredients("niacinamide").build()
        ));

        SkincareRoutine routine = optimizer.optimize(analysis("dry", "low", true, true, "hyperpigmentation"), assessment("under-4000"));

        assertThat(routine.isExactBudgetFit()).isTrue();
        assertThat(routine.getTotalCost()).isLessThan(BigDecimal.valueOf(4000));
    }

    @Test
    void combinationDarkSpotsFitsUnderSixThousand() {
        RoutineOptimizer optimizer = optimizer(catalog(
                product("combo-cleanser", "Combo Cleanser", "cleanser", 1200).skin("combination").concerns("large_pores").ingredients("glycerin").build(),
                product("dark-treatment", "Dark Spot Treatment", "serum", 1600).skin("combination").concerns("dark_spots").ingredients("tranexamic acid").build(),
                product("combo-moisturizer", "Combo Moisturizer", "moisturizer", 1200).skin("combination").concerns("redness").ingredients("panthenol").build(),
                product("combo-sunscreen", "Combo Sunscreen", "sunscreen", 1200).skin("combination").concerns("dark_spots").ingredients("niacinamide").build()
        ));

        SkincareRoutine routine = optimizer.optimize(analysis("combination", "moderate", true, true, "dark_spots"), assessment("under-6000"));

        assertThat(routine.isExactBudgetFit()).isTrue();
        assertThat(routine.getTotalCost()).isLessThan(BigDecimal.valueOf(6000));
    }

    @Test
    void sensitiveRednessFitsTwoToFourThousandAndAvoidsIrritatingProducts() {
        RoutineOptimizer optimizer = optimizer(catalog(
                product("gentle-cleanser", "Gentle Cleanser", "cleanser", 600).skin("sensitive").concerns("redness").ingredients("glycerin").sensitivity("low_risk").build(),
                product("calming-treatment", "Calming Treatment", "serum", 800).skin("sensitive").concerns("redness").ingredients("centella asiatica").sensitivity("low_risk").build(),
                product("barrier-cream", "Barrier Cream", "moisturizer", 900).skin("sensitive").concerns("redness").ingredients("ceramides").sensitivity("low_risk").build(),
                product("sensitive-sunscreen", "Sensitive Sunscreen", "sunscreen", 900).skin("sensitive").concerns("redness").ingredients("zinc oxide").sensitivity("low_risk").build(),
                product("acid-treatment", "Acid Treatment", "serum", 700).skin("sensitive").concerns("redness").ingredients("glycolic acid").sensitivity("potentially_irritating").build()
        ));

        SkincareRoutine routine = optimizer.optimize(analysis("sensitive", "high", true, true, "redness"), assessment("under-4000"));

        assertThat(routine.isExactBudgetFit()).isTrue();
        assertThat(productIds(routine)).doesNotContain("acid-treatment");
    }

    @Test
    void highBudgetRoutineUsesSixThousandPlusBand() {
        RoutineOptimizer optimizer = optimizer(catalog(
                product("lux-cleanser", "Luxury Cleanser", "cleanser", 1500).skin("normal").concerns("dull_skin").ingredients("glycerin").build(),
                product("lux-treatment", "Luxury Treatment", "serum", 2200).skin("normal").concerns("fine_lines_wrinkles").ingredients("peptides").build(),
                product("lux-moisturizer", "Luxury Moisturizer", "moisturizer", 1600).skin("normal").concerns("fine_lines_wrinkles").ingredients("ceramides").build(),
                product("lux-sunscreen", "Luxury Sunscreen", "sunscreen", 1200).skin("normal").concerns("dull_skin").ingredients("niacinamide").build()
        ));

        SkincareRoutine routine = optimizer.optimize(analysis("normal", "low", true, true, "fine_lines_wrinkles"), assessment("above-6000"));

        assertThat(routine.isExactBudgetFit()).isTrue();
        assertThat(routine.getTotalCost()).isGreaterThanOrEqualTo(BigDecimal.valueOf(6000));
    }

    @Test
    void noExactBudgetFitDoesNotPretendToFit() {
        RoutineOptimizer optimizer = optimizer(catalog(
                product("costly-cleanser", "Costly Cleanser", "cleanser", 900).skin("oily").concerns("acne").ingredients("glycerin").build(),
                product("costly-moisturizer", "Costly Moisturizer", "moisturizer", 900).skin("oily").concerns("acne").ingredients("glycerin").build(),
                product("costly-sunscreen", "Costly Sunscreen", "sunscreen", 900).skin("oily").concerns("acne").ingredients("niacinamide").build()
        ));

        SkincareRoutine routine = optimizer.optimize(analysis("oily", "moderate", false, true, "acne"), assessment("under-2000"));

        assertThat(routine.isExactBudgetFit()).isFalse();
        assertThat(routine.getTotalCost()).isGreaterThanOrEqualTo(BigDecimal.valueOf(2000));
        assertThat(routine.getFallbackMessage()).contains("No complete routine satisfies");
    }

    @Test
    void sameCleanserUsedMorningAndEveningIsCountedOnce() {
        RoutineOptimizer optimizer = optimizer(catalog(
                product("shared-cleanser", "Shared Cleanser", "cleanser", 500).skin("normal").concerns("dull_skin").ingredients("glycerin").build(),
                product("basic-moisturizer", "Basic Moisturizer", "moisturizer", 600).skin("normal").concerns("dull_skin").ingredients("glycerin").build(),
                product("basic-sunscreen", "Basic Sunscreen", "sunscreen", 700).skin("normal").concerns("dull_skin").ingredients("niacinamide").build()
        ));

        SkincareRoutine routine = optimizer.optimize(analysis("normal", "low", false, true, "dull_skin"), assessment("under-2000"));

        assertThat(routine.getMorning()).extracting(step -> step.getProductMatch().getProduct().getId()).contains("shared-cleanser");
        assertThat(routine.getEvening()).extracting(step -> step.getProductMatch().getProduct().getId()).contains("shared-cleanser");
        assertThat(routine.getTotalCost()).isEqualByComparingTo(BigDecimal.valueOf(1800));
    }

    @Test
    void noTreatmentNecessaryDoesNotForceSerum() {
        RoutineOptimizer optimizer = optimizer(catalog(
                product("simple-cleanser", "Simple Cleanser", "cleanser", 500).skin("normal").concerns("dull_skin").ingredients("glycerin").build(),
                product("unneeded-serum", "Unneeded Serum", "serum", 500).skin("normal").concerns("dull_skin").ingredients("niacinamide").build(),
                product("simple-moisturizer", "Simple Moisturizer", "moisturizer", 500).skin("normal").concerns("dull_skin").ingredients("glycerin").build(),
                product("simple-sunscreen", "Simple Sunscreen", "sunscreen", 500).skin("normal").concerns("dull_skin").ingredients("niacinamide").build()
        ));

        SkincareRoutine routine = optimizer.optimize(analysis("normal", "low", false, true, "dull_skin"), assessment("under-4000"));

        assertThat(productIds(routine)).doesNotContain("unneeded-serum");
        assertThat(routine.getUniqueProductCount()).isEqualTo(3);
    }

    @Test
    void geminiTreatmentFlagCannotAddTreatmentWithoutAnExplicitUserConcern() {
        RoutineOptimizer optimizer = optimizer(catalog(
                product("cleanser", "Cleanser", "cleanser", 500).skin("normal").build(),
                product("moisturizer", "Moisturizer", "moisturizer", 500).skin("normal").build(),
                product("sunscreen", "Sunscreen", "sunscreen", 700).skin("normal").build(),
                product("serum", "Serum", "serum", 500).skin("normal").concerns("dull_skin").build()
        ));
        AssessmentRequest request = assessment("under-4000");
        request.setConcerns(List.of());

        SkincareRoutine routine = optimizer.optimize(analysis("normal", "low", true, true, "dull_skin"), request);

        assertThat(productIds(routine)).doesNotContain("serum");
        assertThat(routine.getUniqueProductCount()).isEqualTo(3);
    }

    @Test
    void expensiveSerumCannotDisplaceEssentialSunscreenUnderBudget() {
        RoutineOptimizer optimizer = optimizer(catalog(
                product("cleanser", "Cleanser", "cleanser", 500).skin("oily").concerns("acne").build(),
                product("moisturizer", "Moisturizer", "moisturizer", 500).skin("oily").concerns("acne").build(),
                product("sunscreen", "Sunscreen", "sunscreen", 700).skin("oily").concerns("acne").build(),
                product("serum", "High Score Serum", "serum", 1800).skin("oily").concerns("acne").ingredients("niacinamide", "salicylic acid").build()
        ));

        SkincareRoutine routine = optimizer.optimize(analysis("oily", "low", true, true, "acne"), assessment("under-2000"));

        assertThat(routine.isExactBudgetFit()).isTrue();
        assertThat(productIds(routine)).contains("cleanser", "moisturizer", "sunscreen").doesNotContain("serum");
    }

    @Test
    void advancedPreferenceDoesNotCreateRedundantFillerProducts() {
        RoutineOptimizer optimizer = optimizer(catalog(
                product("cleanser", "Cleanser", "cleanser", 500).skin("normal").build(),
                product("moisturizer", "Moisturizer", "moisturizer", 500).skin("normal").build(),
                product("sunscreen", "Sunscreen", "sunscreen", 700).skin("normal").build(),
                product("toner", "Unneeded Toner", "toner", 500).skin("normal").build()
        ));
        AssessmentRequest request = assessment("under-4000");
        request.setRoutinePreference("advanced");

        SkincareRoutine routine = optimizer.optimize(analysis("normal", "low", false, true), request);

        assertThat(productIds(routine)).containsExactlyInAnyOrder("cleanser", "moisturizer", "sunscreen");
        assertThat(routine.getExplanationMetadata()).anyMatch(note -> note.contains("filler products"));
    }

    @Test
    void tenMlSunscreenIsRejectedInFavorOfVerifiedFullSize() {
        RoutineOptimizer optimizer = optimizer(catalog(
                product("cleanser", "Cleanser", "cleanser", 500).skin("normal").size(150, "ml").build(),
                product("moisturizer", "Moisturizer", "moisturizer", 500).skin("normal").size(50, "ml").build(),
                product("mini-sun", "Mini Sunscreen", "sunscreen", 200).skin("normal").size(10, "ml").ingredients("niacinamide").build(),
                product("full-sun", "Full Sunscreen", "sunscreen", 900).skin("normal").size(50, "ml").build()
        ));

        SkincareRoutine routine = optimizer.optimize(analysis("normal", "low", false, true), assessment("under-4000"));

        assertThat(productIds(routine)).contains("full-sun").doesNotContain("mini-sun");
    }

    @Test
    void unknownSizeSunscreenCannotBeTreatedAsFullSize() {
        RoutineOptimizer optimizer = optimizer(catalog(
                product("cleanser", "Cleanser", "cleanser", 500).skin("normal").build(),
                product("moisturizer", "Moisturizer", "moisturizer", 500).skin("normal").build(),
                product("unknown-sun", "Unknown Sunscreen", "sunscreen", 500).skin("normal").sizeUnknown().build()
        ));

        SkincareRoutine routine = optimizer.optimize(analysis("normal", "low", false, true), assessment("under-4000"));

        assertThat(routine.isComplete()).isFalse();
        assertThat(routine.getFallbackMessage()).contains("full-size sunscreen");
    }

    @Test
    void regularSunscreenUseCanAddOneCatalogFirstCleanserAndCountsItsCost() {
        RoutineOptimizer optimizer = optimizer(catalog(
                product("cleanser", "Cleanser", "cleanser", 500).skin("normal").build(),
                product("moisturizer", "Moisturizer", "moisturizer", 500).skin("normal").build(),
                product("sunscreen", "Sunscreen", "sunscreen", 700).skin("normal").build(),
                product("cleansing-oil", "Cleansing Oil", "cleansing_oil", 800).skin("normal").size(200, "ml").build()
        ));
        AssessmentRequest request = assessment("under-4000");
        request.setSunscreenUsage("always");

        SkincareRoutine routine = optimizer.optimize(analysis("normal", "low", false, true), request);

        assertThat(routine.getEvening()).extracting(RoutineProductStep::getStepName).contains("First Cleanse");
        assertThat(productIds(routine)).contains("cleansing-oil");
        assertThat(routine.getTotalCost()).isEqualByComparingTo("2500");
    }

    @Test
    void doubleCleanseIsNotForcedWhenSunscreenUseIsNotRegular() {
        RoutineOptimizer optimizer = optimizer(catalog(
                product("cleanser", "Cleanser", "cleanser", 500).skin("normal").build(),
                product("moisturizer", "Moisturizer", "moisturizer", 500).skin("normal").build(),
                product("sunscreen", "Sunscreen", "sunscreen", 700).skin("normal").build(),
                product("cleansing-oil", "Cleansing Oil", "cleansing_oil", 800).skin("normal").size(200, "ml").build()
        ));

        SkincareRoutine routine = optimizer.optimize(analysis("normal", "low", false, true), assessment("under-4000"));

        assertThat(routine.getEvening()).extracting(RoutineProductStep::getStepName).doesNotContain("First Cleanse");
    }

    @Test
    void exactSixThousandBelongsToFinalTierButNotUnderSixThousand() {
        assertThat(BudgetRange.UNDER_6000.contains(BigDecimal.valueOf(6000))).isFalse();
        assertThat(BudgetRange.ABOVE_6000.contains(BigDecimal.valueOf(6000))).isTrue();
        assertThat(BudgetRange.UNDER_2000.contains(BigDecimal.valueOf(1999))).isTrue();
        assertThat(BudgetRange.UNDER_4000.contains(BigDecimal.valueOf(3999))).isTrue();
    }

    private RoutineOptimizer optimizer(ProductCatalog catalog) {
        return new RoutineOptimizer(new ProductMatcher(catalog, new IngredientNormalizer()));
    }

    private ProductCatalog catalog(CatalogProduct... products) {
        ProductCatalog catalog = new ProductCatalog();
        catalog.setProducts(List.of(products));
        return catalog;
    }

    private AssessmentRequest assessment(String budget) {
        AssessmentRequest request = new AssessmentRequest();
        request.setBudget(budget);
        request.setFragranceFree("no");
        request.setCrueltyFree("no");
        request.setConcerns(List.of("acne", "hyperpigmentation", "dark-spots", "redness", "fine-lines-wrinkles", "dull-skin"));
        request.setRoutinePreference("minimal");
        request.setSunscreenUsage("rarely");
        return request;
    }

    private SkinAnalysis analysis(String skinType, String sensitivity, boolean treatment, boolean moisturizer, String... concerns) {
        SkinAnalysis analysis = new SkinAnalysis();
        analysis.setSkinType(skinType);
        analysis.setSensitivity(sensitivity);
        analysis.setConcerns(List.of(concerns));
        analysis.setRecommendedIngredients(List.of("niacinamide", "glycerin", "salicylic acid", "alpha arbutin", "centella asiatica", "ceramides", "peptides"));
        analysis.setAvoidIngredients(List.of());

        RoutineNeeds needs = new RoutineNeeds();
        needs.setCleanser(true);
        needs.setTreatment(treatment);
        needs.setMoisturizer(moisturizer);
        needs.setSunscreen(true);
        analysis.setRoutineNeeds(needs);
        return analysis;
    }

    private List<String> productIds(SkincareRoutine routine) {
        return java.util.stream.Stream.concat(routine.getMorning().stream(), routine.getEvening().stream())
                .map(RoutineProductStep::getProductMatch)
                .map(match -> match.getProduct().getId())
                .distinct()
                .toList();
    }

    private ProductBuilder product(String id, String name, String category, int price) {
        return new ProductBuilder(id, name, category, price);
    }

    private static class ProductBuilder {

        private final CatalogProduct product = new CatalogProduct();

        private ProductBuilder(String id, String name, String category, int price) {
            product.setId(id);
            product.setName(name);
            product.setBrand("Test Brand");
            product.setCategory(category);
            product.setPrice(BigDecimal.valueOf(price));
            product.setCurrency("BDT");
            product.setSizeValue(BigDecimal.valueOf(defaultSize(category)));
            product.setSizeUnit("ml");
            product.setRetailer("Test");
            product.setProductUrl("https://example.com/" + id);
            product.setSourceProductUrl("https://example.com/" + id);
            product.setImageUrl("https://example.com/" + id + ".jpg");
            product.setSensitivitySuitability("moderate");
            product.setPriceLastVerified("2026-10-09");
            product.setVerified(true);
            product.setImageVerified(true);
            product.setIngredientsVerified(true);
            product.setReadyForRecommendation(true);
        }

        private static int defaultSize(String category) {
            return switch (category) {
                case "cleanser", "cleansing_oil", "cleansing_balm" -> 150;
                case "moisturizer", "sunscreen" -> 50;
                default -> 30;
            };
        }

        private ProductBuilder size(int value, String unit) {
            product.setSizeValue(BigDecimal.valueOf(value));
            product.setSizeUnit(unit);
            return this;
        }

        private ProductBuilder sizeUnknown() {
            product.setSizeValue(null);
            product.setSizeUnit(null);
            return this;
        }

        private ProductBuilder skin(String... skinTypes) {
            product.setSkinTypes(List.of(skinTypes));
            return this;
        }

        private ProductBuilder concerns(String... concerns) {
            product.setConcerns(List.of(concerns));
            return this;
        }

        private ProductBuilder ingredients(String... ingredients) {
            product.setKeyIngredients(List.of(ingredients));
            product.setIngredients(List.of(ingredients));
            return this;
        }

        private ProductBuilder sensitivity(String sensitivitySuitability) {
            product.setSensitivitySuitability(sensitivitySuitability);
            return this;
        }

        private CatalogProduct build() {
            return product;
        }
    }
}
