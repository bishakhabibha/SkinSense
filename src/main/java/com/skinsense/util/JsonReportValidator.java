package com.skinsense.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skinsense.dto.SkincareReport;
import com.skinsense.exception.GeminiException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class JsonReportValidator {

    private static final List<String> REQUIRED_FIELDS = List.of(
            "skinSummary",
            "morningRoutine",
            "nightRoutine",
            "ingredientsToUse",
            "ingredientsToAvoid",
            "productRecommendations",
            "safetyWarnings",
            "disclaimer"
    );

    private final ObjectMapper objectMapper;

    public JsonReportValidator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public SkincareReport parseAndValidate(String responseText) {
        try {
            JsonNode root = objectMapper.readTree(responseText);

            if (!root.isObject()) {
                throw new GeminiException("Gemini returned an unexpected response format. Please try again.");
            }

            for (String field : REQUIRED_FIELDS) {
                if (!root.has(field)) {
                    throw new GeminiException("Gemini returned an incomplete report. Please try again.");
                }
            }

            JsonNode recommendations = root.get("productRecommendations");
            if (recommendations == null
                    || !recommendations.has("affordable")
                    || !recommendations.has("midRange")
                    || !recommendations.has("premium")) {
                throw new GeminiException("Gemini returned incomplete product recommendations. Please try again.");
            }

            return objectMapper.treeToValue(root, SkincareReport.class);
        } catch (JsonProcessingException exception) {
            throw new GeminiException("Gemini did not return valid JSON. Please try again.", exception);
        }
    }
}
