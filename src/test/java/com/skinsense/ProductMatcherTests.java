package com.skinsense;

import com.skinsense.dto.AssessmentRequest;
import com.skinsense.dto.CatalogProduct;
import com.skinsense.dto.ProductCatalog;
import com.skinsense.dto.ProductMatch;
import com.skinsense.dto.SkinAnalysis;
import com.skinsense.service.ProductMatcher;
import com.skinsense.util.IngredientNormalizer;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ProductMatcherTests {

    private final IngredientNormalizer ingredientNormalizer = new IngredientNormalizer();

    @Test
    void ranksOilyAcneProductsHigherWhenConcernAndIngredientsMatch() {
        ProductMatcher matcher = matcher(
                product("oily-acne-cleanser", "Oily Acne Cleanser", "cleanser", 1200)
                        .skinTypes("oily", "combination")
                        .concerns("acne", "large_pores")
                        .keyIngredients("salicylic acid", "niacinamide")
                        .ingredients("Water", "Salicylic Acid", "Niacinamide")
                        .build(),
                product("dry-cleanser", "Dry Skin Cleanser", "cleanser", 1000)
                        .skinTypes("dry")
                        .concerns("redness")
                        .keyIngredients("glycerin")
                        .ingredients("Water", "Glycerin")
                        .build()
        );

        List<ProductMatch> matches = matcher.rankProducts("cleanser", analysis("oily", "moderate", List.of("acne"), List.of("salicylic acid")), preferences());

        assertThat(matches).hasSize(2);
        assertThat(matches.getFirst().getProduct().getId()).isEqualTo("oily-acne-cleanser");
        assertThat(matches.getFirst().getMatchedConcerns()).contains("acne");
        assertThat(matches.getFirst().getMatchedIngredients()).contains("salicylic acid");
    }

    @Test
    void ranksDryHyperpigmentationTreatmentByConcernAndBrighteningIngredients() {
        ProductMatcher matcher = matcher(
                product("arbutin-serum", "Alpha Arbutin Serum", "serum", 1800)
                        .skinTypes("dry", "normal")
                        .concerns("hyperpigmentation", "dark_spots")
                        .keyIngredients("alpha arbutin", "hyaluronic acid")
                        .ingredients("Water", "Alpha Arbutin", "Sodium Hyaluronate")
                        .build(),
                product("oil-serum", "Oil Control Serum", "serum", 1400)
                        .skinTypes("oily")
                        .concerns("large_pores")
                        .keyIngredients("zinc pca")
                        .ingredients("Water", "Zinc PCA")
                        .build()
        );

        List<ProductMatch> matches = matcher.rankProducts("treatment", analysis("dry", "low", List.of("hyperpigmentation"), List.of("alpha arbutin")), preferences());

        assertThat(matches.getFirst().getProduct().getId()).isEqualTo("arbutin-serum");
        assertThat(matches.getFirst().getMatchedConcerns()).contains("hyperpigmentation");
    }

    @Test
    void ranksCombinationDarkSpotProductsLogically() {
        ProductMatcher matcher = matcher(
                product("dark-spot-serum", "Dark Spot Serum", "serum", 1600)
                        .skinTypes("combination", "normal")
                        .concerns("dark_spots", "uneven_skin_tone")
                        .keyIngredients("niacinamide", "tranexamic acid")
                        .ingredients("Water", "Niacinamide", "Tranexamic Acid")
                        .build(),
                product("plain-serum", "Plain Serum", "serum", 900)
                        .skinTypes("normal")
                        .concerns("dull_skin")
                        .keyIngredients("glycerin")
                        .ingredients("Water", "Glycerin")
                        .build()
        );

        List<ProductMatch> matches = matcher.rankProducts("treatment", analysis("combination", "moderate", List.of("dark_spots"), List.of("txa", "niacinamide")), preferences());

        assertThat(matches.getFirst().getProduct().getId()).isEqualTo("dark-spot-serum");
        assertThat(matches.getFirst().getMatchedIngredients()).contains("niacinamide", "tranexamic acid");
    }

    @Test
    void excludesPotentiallyIrritatingProductsForSensitiveRednessProfile() {
        ProductMatcher matcher = matcher(
                product("calming-cream", "Calming Cream", "moisturizer", 1500)
                        .skinTypes("sensitive", "dry")
                        .concerns("redness")
                        .keyIngredients("centella asiatica", "panthenol")
                        .ingredients("Water", "Centella Asiatica Extract", "Panthenol")
                        .sensitivity("low_risk")
                        .build(),
                product("acid-cream", "Strong Acid Cream", "moisturizer", 1300)
                        .skinTypes("sensitive")
                        .concerns("redness")
                        .keyIngredients("glycolic acid")
                        .ingredients("Water", "Glycolic Acid")
                        .sensitivity("potentially_irritating")
                        .build()
        );

        List<ProductMatch> matches = matcher.rankProducts("moisturizer", analysis("sensitive", "high", List.of("redness"), List.of("centella asiatica")), preferences());

        assertThat(matches).hasSize(1);
        assertThat(matches.getFirst().getProduct().getId()).isEqualTo("calming-cream");
    }

    @Test
    void fragranceFreePreferenceExcludesVerifiedNonFragranceFreeAndDoesNotRewardUnknown() {
        AssessmentRequest preferences = preferences();
        preferences.setFragranceFree("yes");
        ProductMatcher matcher = matcher(
                product("verified-ff", "Verified Fragrance Free Cleanser", "cleanser", 1100)
                        .skinTypes("sensitive")
                        .concerns("redness")
                        .keyIngredients("glycerin")
                        .ingredients("Water", "Glycerin")
                        .fragranceFree(Boolean.TRUE)
                        .build(),
                product("unknown-ff", "Unknown Fragrance Cleanser", "cleanser", 900)
                        .skinTypes("sensitive")
                        .concerns("redness")
                        .keyIngredients("glycerin")
                        .ingredients("Water", "Glycerin")
                        .fragranceFree(null)
                        .build(),
                product("not-ff", "Fragranced Cleanser", "cleanser", 800)
                        .skinTypes("sensitive")
                        .concerns("redness")
                        .keyIngredients("glycerin")
                        .ingredients("Water", "Fragrance")
                        .fragranceFree(Boolean.FALSE)
                        .build()
        );

        List<ProductMatch> matches = matcher.rankProducts("cleanser", analysis("sensitive", "moderate", List.of("redness"), List.of("glycerin")), preferences);

        assertThat(matches).extracting(match -> match.getProduct().getId()).containsExactly("verified-ff", "unknown-ff");
        assertThat(matches.get(1).getReasons()).noneMatch(reason -> reason.contains("verified fragrance-free"));
    }

    @Test
    void crueltyFreePreferenceExcludesVerifiedFalseButKeepsUnknownNeutral() {
        AssessmentRequest preferences = preferences();
        preferences.setCrueltyFree("yes");
        ProductMatcher matcher = matcher(
                product("verified-cf", "Verified Cruelty Free Sunscreen", "sunscreen", 1400)
                        .skinTypes("normal")
                        .concerns("dull_skin")
                        .keyIngredients("niacinamide")
                        .ingredients("Water", "Niacinamide")
                        .crueltyFree(Boolean.TRUE)
                        .build(),
                product("unknown-cf", "Unknown Cruelty Sunscreen", "sunscreen", 1300)
                        .skinTypes("normal")
                        .concerns("dull_skin")
                        .keyIngredients("niacinamide")
                        .ingredients("Water", "Niacinamide")
                        .crueltyFree(null)
                        .build(),
                product("not-cf", "Not Cruelty Free Sunscreen", "sunscreen", 1200)
                        .skinTypes("normal")
                        .concerns("dull_skin")
                        .keyIngredients("niacinamide")
                        .ingredients("Water", "Niacinamide")
                        .crueltyFree(Boolean.FALSE)
                        .build()
        );

        List<ProductMatch> matches = matcher.rankProducts("sunscreen", analysis("normal", "low", List.of("dull_skin"), List.of("niacinamide")), preferences);

        assertThat(matches).extracting(match -> match.getProduct().getId()).containsExactly("verified-cf", "unknown-cf");
    }

    @Test
    void unknownMetadataIsNotTreatedAsAPositiveMatch() {
        ProductMatcher matcher = matcher(
                product("unknown", "Unknown Metadata Moisturizer", "moisturizer", 700)
                        .skinTypes()
                        .concerns()
                        .keyIngredients()
                        .ingredients()
                        .sensitivity("")
                        .ingredientsVerified(false)
                        .build()
        );

        ProductMatch match = matcher.rankProducts("moisturizer", analysis("dry", "moderate", List.of("redness"), List.of("ceramides")), preferences()).getFirst();

        assertThat(match.getMatchedSkinTypes()).isEmpty();
        assertThat(match.getMatchedConcerns()).isEmpty();
        assertThat(match.getMatchedIngredients()).isEmpty();
        assertThat(match.getCompatibilityScore()).isEqualTo(2);
    }

    @Test
    void avoidIngredientsPenalizeButDoNotAlwaysExclude() {
        SkinAnalysis analysis = analysis("oily", "moderate", List.of("acne"), List.of("niacinamide"));
        analysis.setAvoidIngredients(List.of("fragrance"));
        ProductMatcher matcher = matcher(
                product("without-fragrance", "Without Fragrance", "serum", 1000)
                        .skinTypes("oily")
                        .concerns("acne")
                        .keyIngredients("niacinamide")
                        .ingredients("Water", "Niacinamide")
                        .build(),
                product("with-fragrance", "With Fragrance", "serum", 900)
                        .skinTypes("oily")
                        .concerns("acne")
                        .keyIngredients("niacinamide")
                        .ingredients("Water", "Niacinamide", "Fragrance")
                        .build()
        );

        List<ProductMatch> matches = matcher.rankProducts("treatment", analysis, preferences());

        assertThat(matches.getFirst().getProduct().getId()).isEqualTo("without-fragrance");
        assertThat(matches.get(1).getAvoidIngredientMatches()).contains("fragrance");
    }

    private ProductMatcher matcher(CatalogProduct... products) {
        ProductCatalog catalog = new ProductCatalog();
        catalog.setProducts(List.of(products));
        return new ProductMatcher(catalog, ingredientNormalizer);
    }

    private SkinAnalysis analysis(String skinType, String sensitivity, List<String> concerns, List<String> recommendedIngredients) {
        SkinAnalysis analysis = new SkinAnalysis();
        analysis.setSkinType(skinType);
        analysis.setSensitivity(sensitivity);
        analysis.setConcerns(concerns);
        analysis.setRecommendedIngredients(recommendedIngredients);
        analysis.setAvoidIngredients(List.of());
        return analysis;
    }

    private AssessmentRequest preferences() {
        AssessmentRequest preferences = new AssessmentRequest();
        preferences.setFragranceFree("no");
        preferences.setCrueltyFree("no");
        return preferences;
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

        private ProductBuilder skinTypes(String... skinTypes) {
            product.setSkinTypes(List.of(skinTypes));
            return this;
        }

        private ProductBuilder concerns(String... concerns) {
            product.setConcerns(List.of(concerns));
            return this;
        }

        private ProductBuilder keyIngredients(String... keyIngredients) {
            product.setKeyIngredients(List.of(keyIngredients));
            return this;
        }

        private ProductBuilder ingredients(String... ingredients) {
            product.setIngredients(List.of(ingredients));
            return this;
        }

        private ProductBuilder sensitivity(String suitability) {
            product.setSensitivitySuitability(suitability);
            return this;
        }

        private ProductBuilder fragranceFree(Boolean fragranceFree) {
            product.setFragranceFree(fragranceFree);
            return this;
        }

        private ProductBuilder crueltyFree(Boolean crueltyFree) {
            product.setCrueltyFree(crueltyFree);
            return this;
        }

        private ProductBuilder ingredientsVerified(boolean ingredientsVerified) {
            product.setIngredientsVerified(ingredientsVerified);
            return this;
        }

        private CatalogProduct build() {
            return product;
        }
    }
}
