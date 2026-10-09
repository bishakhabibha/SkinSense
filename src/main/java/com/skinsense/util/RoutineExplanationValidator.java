package com.skinsense.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skinsense.dto.ProductReason;
import com.skinsense.dto.RoutineExplanation;
import com.skinsense.dto.RoutineInstruction;
import com.skinsense.dto.RoutineProductStep;
import com.skinsense.dto.SkincareRoutine;
import com.skinsense.exception.GeminiException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class RoutineExplanationValidator {

    private final ObjectMapper objectMapper;

    public RoutineExplanationValidator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public RoutineExplanation parseAndValidate(String responseText, SkincareRoutine routine) {
        try {
            JsonNode root = objectMapper.readTree(responseText);
            if (!root.isObject()) {
                throw new GeminiException("Gemini returned an invalid routine explanation.");
            }

            require(root, "summary");
            require(root, "morningRoutine");
            require(root, "eveningRoutine");
            require(root, "productReasons");
            require(root, "generalTips");
            require(root, "cautions");

            RoutineExplanation explanation = objectMapper.treeToValue(root, RoutineExplanation.class);
            Set<String> selectedProductIds = selectedProductIds(routine);

            explanation.setMorningRoutine(explanation.getMorningRoutine().stream()
                    .filter(instruction -> validInstruction(instruction, selectedProductIds))
                    .toList());
            explanation.setEveningRoutine(explanation.getEveningRoutine().stream()
                    .filter(instruction -> validInstruction(instruction, selectedProductIds))
                    .toList());
            explanation.setProductReasons(explanation.getProductReasons().stream()
                    .filter(reason -> validReason(reason, selectedProductIds))
                    .toList());

            return explanation;
        } catch (JsonProcessingException exception) {
            throw new GeminiException("Gemini returned invalid routine explanation JSON.", exception);
        }
    }

    private void require(JsonNode root, String field) {
        if (!root.has(field)) {
            throw new GeminiException("Gemini returned an incomplete routine explanation.");
        }
    }

    private boolean validInstruction(RoutineInstruction instruction, Set<String> selectedProductIds) {
        return instruction != null
                && selectedProductIds.contains(instruction.getProductId())
                && StringUtils.hasText(instruction.getInstruction());
    }

    private boolean validReason(ProductReason reason, Set<String> selectedProductIds) {
        return reason != null
                && selectedProductIds.contains(reason.getProductId())
                && StringUtils.hasText(reason.getReason());
    }

    private Set<String> selectedProductIds(SkincareRoutine routine) {
        return Stream.concat(routine.getMorning().stream(), routine.getEvening().stream())
                .map(RoutineProductStep::getProductId)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());
    }
}
