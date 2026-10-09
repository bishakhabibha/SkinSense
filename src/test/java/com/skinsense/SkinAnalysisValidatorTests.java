package com.skinsense;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skinsense.dto.SkinAnalysis;
import com.skinsense.exception.GeminiException;
import com.skinsense.util.SkinAnalysisValidator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SkinAnalysisValidatorTests {

    private final SkinAnalysisValidator validator = new SkinAnalysisValidator(new ObjectMapper());

    @Test
    void parsesNormalSkinProfile() {
        SkinAnalysis analysis = validator.parseAndValidate("""
                {
                  "skinType": "normal",
                  "sensitivity": "low",
                  "concerns": ["dull_skin"],
                  "recommendedIngredients": ["glycerin", "niacinamide"],
                  "avoidIngredients": [],
                  "routineNeeds": {
                    "cleanser": true,
                    "treatment": false,
                    "moisturizer": true,
                    "sunscreen": true
                  },
                  "reasoning": "Normal skin with mild dullness can benefit from hydration, barrier support and consistent sunscreen."
                }
                """);

        assertThat(analysis.getSkinType()).isEqualTo("normal");
        assertThat(analysis.getRoutineNeeds().isTreatment()).isFalse();
    }

    @Test
    void parsesOilyAcneProfile() {
        SkinAnalysis analysis = validator.parseAndValidate("""
                {
                  "skinType": "oily",
                  "sensitivity": "moderate",
                  "concerns": ["acne", "large_pores"],
                  "recommendedIngredients": ["salicylic acid", "niacinamide", "zinc pca"],
                  "avoidIngredients": ["heavy occlusive textures"],
                  "routineNeeds": {
                    "cleanser": true,
                    "treatment": true,
                    "moisturizer": true,
                    "sunscreen": true
                  },
                  "reasoning": "Oily acne-prone skin usually benefits from oil regulation, gentle pore care and non-heavy hydration."
                }
                """);

        assertThat(analysis.getConcerns()).contains("acne");
        assertThat(analysis.getRecommendedIngredients()).contains("salicylic acid");
    }

    @Test
    void parsesDryHyperpigmentationProfile() {
        SkinAnalysis analysis = validator.parseAndValidate("""
                {
                  "skinType": "dry",
                  "sensitivity": "low",
                  "concerns": ["hyperpigmentation", "dark_spots", "dull_skin"],
                  "recommendedIngredients": ["alpha arbutin", "tranexamic acid", "ceramides", "glycerin"],
                  "avoidIngredients": ["overuse of strong exfoliating acids"],
                  "routineNeeds": {
                    "cleanser": true,
                    "treatment": true,
                    "moisturizer": true,
                    "sunscreen": true
                  },
                  "reasoning": "Dry skin with pigmentation needs barrier-supportive hydration plus tone-evening ingredients and daily sunscreen."
                }
                """);

        assertThat(analysis.getSkinType()).isEqualTo("dry");
        assertThat(analysis.getConcerns()).contains("hyperpigmentation");
    }

    @Test
    void parsesSensitiveRednessProfile() {
        SkinAnalysis analysis = validator.parseAndValidate("""
                {
                  "skinType": "sensitive",
                  "sensitivity": "high",
                  "concerns": ["redness"],
                  "recommendedIngredients": ["centella asiatica", "panthenol", "ceramides"],
                  "avoidIngredients": ["fragrance", "strong exfoliating acids"],
                  "routineNeeds": {
                    "cleanser": true,
                    "treatment": true,
                    "moisturizer": true,
                    "sunscreen": true
                  },
                  "reasoning": "Sensitive skin with redness should prioritize calming, barrier-supportive ingredients and avoid aggressive routines."
                }
                """);

        assertThat(analysis.getSensitivity()).isEqualTo("high");
        assertThat(analysis.getAvoidIngredients()).contains("fragrance");
    }

    @Test
    void parsesCombinationAcneDarkSpotsProfile() {
        SkinAnalysis analysis = validator.parseAndValidate("""
                {
                  "skinType": "combination",
                  "sensitivity": "moderate",
                  "concerns": ["acne", "dark_spots", "uneven_skin_tone"],
                  "recommendedIngredients": ["niacinamide", "azelaic acid", "salicylic acid"],
                  "avoidIngredients": ["layering multiple strong actives at once"],
                  "routineNeeds": {
                    "cleanser": true,
                    "treatment": true,
                    "moisturizer": true,
                    "sunscreen": true
                  },
                  "reasoning": "Combination skin with acne and dark spots needs balanced oil control, spot-supportive ingredients and sunscreen."
                }
                """);

        assertThat(analysis.getSkinType()).isEqualTo("combination");
        assertThat(analysis.getConcerns()).contains("acne", "dark_spots");
    }

    @Test
    void rejectsProductRecommendationsFromGemini() {
        assertThatThrownBy(() -> validator.parseAndValidate("""
                {
                  "skinType": "oily",
                  "sensitivity": "moderate",
                  "concerns": ["acne"],
                  "recommendedIngredients": ["CeraVe Acne Control Cleanser"],
                  "avoidIngredients": [],
                  "routineNeeds": {
                    "cleanser": true,
                    "treatment": true,
                    "moisturizer": true,
                    "sunscreen": true
                  },
                  "reasoning": "This includes a product name."
                }
                """))
                .isInstanceOf(GeminiException.class);
    }
}
