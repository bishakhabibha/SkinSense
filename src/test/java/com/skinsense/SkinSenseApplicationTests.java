package com.skinsense;

import com.skinsense.dto.AssessmentRequest;
import com.skinsense.dto.IngredientRecommendation;
import com.skinsense.dto.ProductRecommendation;
import com.skinsense.dto.RoutineStep;
import com.skinsense.dto.SafetyWarning;
import com.skinsense.dto.SkincareReport;
import com.skinsense.service.PdfReportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SkinSenseApplicationTests {

    @Autowired
    private PdfReportService pdfReportService;

    @Test
    void contextLoads() {
    }

    @Test
    void createsPdfReport() {
        AssessmentRequest assessment = new AssessmentRequest();
        assessment.setSkinType("combination");
        assessment.setSkinSensitivity("moderate");
        assessment.setBudget("mid-range");
        assessment.setConcerns(List.of("acne", "dark-spots"));

        SkincareReport report = new SkincareReport();
        report.setSkinSummary("Combination skin with occasional breakouts and uneven tone.");
        report.setMorningRoutine(List.of(routineStep("Cleanser"), routineStep("Sunscreen")));
        report.setNightRoutine(List.of(routineStep("Cleanser"), routineStep("Moisturizer")));
        report.setIngredientsToUse(List.of(ingredient("Niacinamide")));
        report.setIngredientsToAvoid(List.of(ingredient("Harsh fragrance")));
        report.getProductRecommendations().setAffordable(List.of(product("CeraVe Hydrating Facial Cleanser", "CeraVe")));
        report.getProductRecommendations().setMidRange(List.of(product("The Ordinary Niacinamide 10% + Zinc 1%", "The Ordinary")));
        report.getProductRecommendations().setPremium(List.of(product("Paula's Choice 2% BHA Liquid Exfoliant", "Paula's Choice")));
        report.setSafetyWarnings(List.of(warning("Patch Test")));
        report.setDisclaimer("AI-generated guidance is not a substitute for professional dermatological advice.");

        byte[] pdfBytes = pdfReportService.createReportPdf(report, assessment);

        assertThat(pdfBytes).isNotEmpty();
        assertThat(new String(pdfBytes, 0, 4, StandardCharsets.US_ASCII)).isEqualTo("%PDF");
    }

    private RoutineStep routineStep(String name) {
        RoutineStep step = new RoutineStep();
        step.setStepName(name);
        step.setRecommendedIngredients("Niacinamide, Glycerin");
        step.setSuggestedProducts("Sample product");
        step.setBrand("Sample brand");
        step.setApproximatePriceCategory("Mid-range");
        step.setScientificReasoning("Supports the skin barrier and helps reduce visible irritation.");
        return step;
    }

    private IngredientRecommendation ingredient(String name) {
        IngredientRecommendation ingredient = new IngredientRecommendation();
        ingredient.setIngredient(name);
        ingredient.setExplanation("Helpful for maintaining a balanced and comfortable routine.");
        return ingredient;
    }

    private ProductRecommendation product(String name, String brand) {
        ProductRecommendation product = new ProductRecommendation();
        product.setProductName(name);
        product.setBrand(brand);
        product.setWhySuitable("It matches the user's routine preference and skin profile.");
        return product;
    }

    private SafetyWarning warning(String topic) {
        SafetyWarning warning = new SafetyWarning();
        warning.setTopic(topic);
        warning.setGuidance("Test new products on a small area before full use.");
        return warning;
    }
}
