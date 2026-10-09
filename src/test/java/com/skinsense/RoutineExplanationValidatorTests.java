package com.skinsense;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skinsense.dto.CatalogProduct;
import com.skinsense.dto.ProductMatch;
import com.skinsense.dto.RoutineExplanation;
import com.skinsense.dto.RoutineProductStep;
import com.skinsense.dto.SkincareRoutine;
import com.skinsense.util.RoutineExplanationValidator;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RoutineExplanationValidatorTests {

    private final RoutineExplanationValidator validator = new RoutineExplanationValidator(new ObjectMapper());

    @Test
    void filtersUnknownProductIdsFromGeminiExplanation() {
        String json = """
                {
                  "summary": "This routine may help support the selected concerns.",
                  "morningRoutine": [
                    {"step": 1, "productId": "known-cleanser", "instruction": "Use gently in the morning."},
                    {"step": 2, "productId": "invented-product", "instruction": "Do not allow this."}
                  ],
                  "eveningRoutine": [],
                  "productReasons": [
                    {"productId": "known-cleanser", "reason": "Chosen because it aligns with the profile."},
                    {"productId": "invented-product", "reason": "This should be filtered."}
                  ],
                  "generalTips": ["Introduce products gradually."],
                  "cautions": ["Patch test first."]
                }
                """;

        RoutineExplanation explanation = validator.parseAndValidate(json, routineWith("known-cleanser"));

        assertThat(explanation.getMorningRoutine()).hasSize(1);
        assertThat(explanation.getMorningRoutine().getFirst().getProductId()).isEqualTo("known-cleanser");
        assertThat(explanation.getProductReasons()).hasSize(1);
        assertThat(explanation.getProductReasons().getFirst().getProductId()).isEqualTo("known-cleanser");
    }

    private SkincareRoutine routineWith(String productId) {
        SkincareRoutine routine = new SkincareRoutine();
        routine.setMorning(List.of(step(productId)));
        return routine;
    }

    private RoutineProductStep step(String productId) {
        CatalogProduct product = new CatalogProduct();
        product.setId(productId);
        product.setName("Known Cleanser");
        product.setBrand("Known Brand");

        ProductMatch match = new ProductMatch();
        match.setProduct(product);

        RoutineProductStep step = new RoutineProductStep();
        step.setOrder(1);
        step.setProductMatch(match);
        return step;
    }
}
