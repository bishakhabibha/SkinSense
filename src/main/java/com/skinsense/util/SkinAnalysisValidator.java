package com.skinsense.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skinsense.dto.SkinAnalysis;
import com.skinsense.exception.GeminiException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

@Component
public class SkinAnalysisValidator {

    private static final Set<String> VALID_SKIN_TYPES = Set.of("normal", "oily", "dry", "combination", "sensitive");
    private static final Set<String> VALID_SENSITIVITY = Set.of("low", "moderate", "high");
    private static final Set<String> VALID_CONCERNS = Set.of(
            "acne",
            "acne_scars",
            "hyperpigmentation",
            "dark_spots",
            "redness",
            "large_pores",
            "fine_lines_wrinkles",
            "dull_skin",
            "uneven_skin_tone"
    );
    private static final Set<String> FORBIDDEN_PRODUCT_FIELDS = Set.of(
            "product",
            "products",
            "productName",
            "productRecommendations",
            "brand",
            "price",
            "productUrl",
            "url"
    );
    private static final List<String> KNOWN_PRODUCT_BRANDS = List.of(
            "cerave",
            "cetaphil",
            "cosrx",
            "beauty of joseon",
            "la roche",
            "bioderma",
            "neutrogena",
            "simple",
            "the ordinary",
            "purito",
            "skin1004",
            "some by mi",
            "anua",
            "isntree",
            "minimalist",
            "eucerin",
            "aveeno"
    );

    private final ObjectMapper objectMapper;

    public SkinAnalysisValidator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public SkinAnalysis parseAndValidate(String responseText) {
        try {
            JsonNode root = objectMapper.readTree(responseText);

            if (!root.isObject()) {
                throw new GeminiException("Gemini returned an unexpected skin analysis format. Please try again.");
            }

            require(root, "skinType");
            require(root, "sensitivity");
            require(root, "concerns");
            require(root, "recommendedIngredients");
            require(root, "avoidIngredients");
            require(root, "routineNeeds");
            require(root, "reasoning");
            rejectProductRecommendations(root);
            validateRoutineNeeds(root.get("routineNeeds"));

            SkinAnalysis analysis = objectMapper.treeToValue(root, SkinAnalysis.class);
            validateControlledValues(analysis);
            return analysis;
        } catch (JsonProcessingException exception) {
            throw new GeminiException("Gemini did not return valid skin analysis JSON. Please try again.", exception);
        }
    }

    private void require(JsonNode root, String fieldName) {
        if (!root.has(fieldName) || root.get(fieldName).isNull()) {
            throw new GeminiException("Gemini returned an incomplete skin analysis. Please try again.");
        }
    }

    private void validateRoutineNeeds(JsonNode routineNeeds) {
        if (routineNeeds == null || !routineNeeds.isObject()) {
            throw new GeminiException("Gemini returned malformed routine needs. Please try again.");
        }

        for (String field : List.of("cleanser", "treatment", "moisturizer", "sunscreen")) {
            if (!routineNeeds.has(field) || !routineNeeds.get(field).isBoolean()) {
                throw new GeminiException("Gemini returned malformed routine needs. Please try again.");
            }
        }
    }

    private void validateControlledValues(SkinAnalysis analysis) {
        if (!VALID_SKIN_TYPES.contains(normalize(analysis.getSkinType()))) {
            throw new GeminiException("Gemini returned an unsupported skin type. Please try again.");
        }

        if (!VALID_SENSITIVITY.contains(normalize(analysis.getSensitivity()))) {
            throw new GeminiException("Gemini returned an unsupported sensitivity level. Please try again.");
        }

        for (String concern : analysis.getConcerns()) {
            if (!VALID_CONCERNS.contains(normalize(concern))) {
                throw new GeminiException("Gemini returned an unsupported skin concern. Please try again.");
            }
        }

        if (!StringUtils.hasText(analysis.getReasoning())) {
            throw new GeminiException("Gemini returned incomplete skin reasoning. Please try again.");
        }

        rejectProductNames(analysis.getRecommendedIngredients());
        rejectProductNames(analysis.getAvoidIngredients());
    }

    private void rejectProductRecommendations(JsonNode node) {
        Iterator<String> fieldNames = node.fieldNames();
        while (fieldNames.hasNext()) {
            String fieldName = fieldNames.next();
            if (FORBIDDEN_PRODUCT_FIELDS.contains(fieldName)) {
                throw new GeminiException("Gemini returned product recommendations too early. Please try again.");
            }
            rejectProductRecommendations(node.get(fieldName));
        }

        if (node.isArray()) {
            for (JsonNode child : node) {
                rejectProductRecommendations(child);
            }
        }
    }

    private void rejectProductNames(List<String> values) {
        for (String value : values) {
            String normalized = value == null ? "" : value.trim().toLowerCase();
            for (String brand : KNOWN_PRODUCT_BRANDS) {
                if (normalized.contains(brand)) {
                    throw new GeminiException("Gemini returned a product or brand instead of an ingredient category. Please try again.");
                }
            }
        }
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase().replace("-", "_").replace(" ", "_");
    }
}
