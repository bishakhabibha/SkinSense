package com.skinsense;

import com.skinsense.dto.AssessmentRequest;
import com.skinsense.dto.BudgetRange;
import com.skinsense.dto.CatalogProduct;
import com.skinsense.dto.IngredientRecommendation;
import com.skinsense.dto.ProductMatch;
import com.skinsense.dto.RoutineProductStep;
import com.skinsense.dto.RoutineStep;
import com.skinsense.dto.SafetyWarning;
import com.skinsense.dto.SkincareRoutine;
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
        report.setSkincareRoutine(skincareRoutine());
        report.setIngredientsToUse(List.of(ingredient("Niacinamide")));
        report.setIngredientsToAvoid(List.of(ingredient("Harsh fragrance")));
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

    private SafetyWarning warning(String topic) {
        SafetyWarning warning = new SafetyWarning();
        warning.setTopic(topic);
        warning.setGuidance("Test new products on a small area before full use.");
        return warning;
    }

    private SkincareRoutine skincareRoutine() {
        SkincareRoutine routine = new SkincareRoutine();
        routine.setBudgetRange(BudgetRange.BDT_2000_TO_4000);
        routine.setExactBudgetFit(true);
        routine.setComplete(true);
        routine.setTotalCost(java.math.BigDecimal.valueOf(2900));
        routine.setMorning(List.of(
                productStep("morning", "Cleanser", 1, "sample-cleanser", "CeraVe", "Hydrating Facial Cleanser", 1200),
                productStep("morning", "Sunscreen", 2, "sample-sunscreen", "Beauty of Joseon", "Relief Sun", 1700)
        ));
        routine.setEvening(List.of(
                productStep("evening", "Cleanser", 1, "sample-cleanser", "CeraVe", "Hydrating Facial Cleanser", 1200)
        ));
        return routine;
    }

    private RoutineProductStep productStep(String period, String stepName, int order, String id, String brand, String name, int price) {
        CatalogProduct product = new CatalogProduct();
        product.setId(id);
        product.setBrand(brand);
        product.setName(name);
        product.setCategory(stepName.toLowerCase());
        product.setPrice(java.math.BigDecimal.valueOf(price));
        product.setCurrency("BDT");
        product.setImageUrl("images/skincare-placeholder.svg");
        product.setKeyIngredients(List.of("glycerin", "niacinamide"));

        ProductMatch match = new ProductMatch();
        match.setProduct(product);
        match.setMatchedIngredients(List.of("glycerin"));
        match.setReasons(List.of("Matched the test skin profile."));

        RoutineProductStep step = new RoutineProductStep();
        step.setPeriod(period);
        step.setStepName(stepName);
        step.setOrder(order);
        step.setFrequency("Use as directed.");
        step.setInstruction("Use as directed.");
        step.setWhyPicked("Matched the test skin profile.");
        step.setProductMatch(match);
        return step;
    }
}
