package com.skinsense.util;

import com.skinsense.dto.AssessmentRequest;
import com.skinsense.dto.CatalogProduct;
import com.skinsense.dto.RoutineProductStep;
import com.skinsense.dto.SkinAnalysis;
import com.skinsense.dto.SkincareRoutine;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Stream;

@Component
public class GeminiPromptBuilder {

    public String buildPrompt(AssessmentRequest request) {
        return """
                You are an experienced dermatologist and skincare profile analysis engine.

                Analyze the user's assessment.

                Determine the user's likely skin profile, ingredient needs, ingredients or ingredient categories to avoid or use cautiously, and which routine steps are actually necessary.

                Important rules:
                - Return structured JSON only.
                - Do not recommend specific products.
                - Do not invent products.
                - Do not invent brands.
                - Do not invent prices.
                - Do not invent product availability.
                - Do not generate product URLs.
                - Do not return markdown around the JSON.
                - Keep user preferences separate from skin analysis. Preferences such as budget, cruelty-free, fragrance-free, country, outdoor exposure and sunscreen usage are constraints for application code later.
                - If the user selected fragrance-free, represent fragrance as a preference/filter or cautious avoid category only. Do not claim fragrance is medically harmful for everyone.
                - Only recommend ingredient categories when they are relevant to the user's profile and concerns.

                Controlled values:
                skinType must be one of:
                normal, oily, dry, combination, sensitive

                sensitivity must be one of:
                low, moderate, high

                concerns must use only these values:
                acne, acne_scars, hyperpigmentation, dark_spots, redness, large_pores, fine_lines_wrinkles, dull_skin, uneven_skin_tone

                routineNeeds must contain booleans for:
                cleanser, treatment, moisturizer, sunscreen

                Routine guidance:
                - AM routine usually includes sunscreen.
                - Treatment should be true only when useful for the profile.
                - Moisturizer can be false only when clearly unnecessary, such as a very minimal oily-skin routine where sunscreen/moisturizing product can cover hydration.
                - Do not add unnecessary steps.

                Use exactly this JSON structure and field names:

                {
                  "skinType": "",
                  "sensitivity": "",
                  "concerns": [],
                  "recommendedIngredients": [],
                  "avoidIngredients": [],
                  "routineNeeds": {
                    "cleanser": true,
                    "treatment": true,
                    "moisturizer": true,
                    "sunscreen": true
                  },
                  "reasoning": ""
                }

                Do not wrap the response in markdown.
                Do not add text before or after the JSON.

                User profile:
                Age: %s
                Gender: %s
                Skin Type: %s
                Skin Sensitivity: %s
                Primary Skin Concerns: %s
                Country: %s
                Average Time Outdoors: %s
                Sunscreen Usage: %s
                Budget: %s
                Routine Preference: %s
                Cruelty-Free Products Only: %s
                Fragrance-Free Products Only: %s
                """.formatted(
                valueOrNotProvided(request.getAge()),
                valueOrNotProvided(request.getGender()),
                valueOrNotProvided(request.getSkinType()),
                valueOrNotProvided(request.getSkinSensitivity()),
                listOrNotProvided(request.getConcerns()),
                valueOrNotProvided(request.getCountry()),
                valueOrNotProvided(request.getTimeOutdoors()),
                valueOrNotProvided(request.getSunscreenUsage()),
                valueOrNotProvided(request.getBudget()),
                valueOrNotProvided(request.getRoutinePreference()),
                valueOrNotProvided(request.getCrueltyFree()),
                valueOrNotProvided(request.getFragranceFree())
        );
    }

    public String buildRoutineExplanationPrompt(AssessmentRequest request, SkinAnalysis analysis, SkincareRoutine routine) {
        return """
                You are explaining a skincare routine that has ALREADY been selected by the application.

                Strict rules:
                - You must not replace, remove, add, or invent products.
                - You must not invent product prices.
                - You must not invent product ingredients.
                - You must not invent product availability.
                - You must not recommend another product.
                - You may only explain the products and routine steps supplied in the input.
                - You must not contradict the user's selected preferences.
                - You must not make medical promises.
                - Use cautious language such as "may help with", "is suitable for", "contains ingredients associated with", and "aligns with your concern".
                - Avoid medical guarantees such as "will cure acne", "guarantees clear skin", or "removes wrinkles".
                - Return valid JSON only. Do not wrap the response in markdown.

                Use exactly this JSON structure and field names:

                {
                  "summary": "",
                  "morningRoutine": [
                    {
                      "step": 1,
                      "productId": "",
                      "instruction": ""
                    }
                  ],
                  "eveningRoutine": [
                    {
                      "step": 1,
                      "productId": "",
                      "instruction": ""
                    }
                  ],
                  "productReasons": [
                    {
                      "productId": "",
                      "reason": ""
                    }
                  ],
                  "generalTips": [],
                  "cautions": []
                }

                User profile:
                Skin Type: %s
                Sensitivity: %s
                Concerns: %s
                Fragrance-Free Products Only: %s
                Cruelty-Free Products Only: %s
                Routine Preference: %s
                Selected Budget: %s

                Gemini skin analysis:
                Recommended Ingredients: %s
                Ingredients To Use Cautiously: %s
                Summary: %s

                Selected routine:
                Total Cost: %s %s
                Budget Range: %s
                Exact Budget Fit: %s
                Routine Notes: %s

                Morning steps:
                %s

                Evening steps:
                %s

                Selected products:
                %s
                """.formatted(
                valueOrNotProvided(analysis.getSkinType()),
                valueOrNotProvided(analysis.getSensitivity()),
                listOrNotProvided(analysis.getConcerns()),
                valueOrNotProvided(request.getFragranceFree()),
                valueOrNotProvided(request.getCrueltyFree()),
                valueOrNotProvided(request.getRoutinePreference()),
                routine.getBudgetRange().getLabel(),
                listOrNotProvided(analysis.getRecommendedIngredients()),
                listOrNotProvided(analysis.getAvoidIngredients()),
                valueOrNotProvided(analysis.getReasoning()),
                valueOrNotProvided(routine.getCurrency()),
                routine.getTotalCost().toPlainString(),
                routine.getBudgetRange().getLabel(),
                String.valueOf(routine.isExactBudgetFit()),
                listOrNotProvided(routine.getExplanationMetadata()),
                routineSteps(routine.getMorning()),
                routineSteps(routine.getEvening()),
                selectedProducts(routine)
        );
    }

    private String routineSteps(List<RoutineProductStep> steps) {
        if (steps == null || steps.isEmpty()) {
            return "No steps selected.";
        }

        StringBuilder builder = new StringBuilder();
        for (RoutineProductStep step : steps) {
            CatalogProduct product = step.getProduct();
            if (product == null) {
                continue;
            }
            builder.append("- Step ")
                    .append(step.getOrder())
                    .append(": ")
                    .append(step.getStepName())
                    .append(" | productId=")
                    .append(product.getId())
                    .append(" | product=")
                    .append(product.getBrand())
                    .append(" ")
                    .append(product.getName())
                    .append(" | frequency=")
                    .append(step.getFrequency())
                    .append(System.lineSeparator());
        }
        return builder.toString();
    }

    private String selectedProducts(SkincareRoutine routine) {
        StringBuilder builder = new StringBuilder();
        Stream.concat(routine.getMorning().stream(), routine.getEvening().stream())
                .filter(step -> step.getProduct() != null)
                .map(RoutineProductStep::getProduct)
                .distinct()
                .forEach(product -> builder.append("- productId=")
                        .append(product.getId())
                        .append(" | brand=")
                        .append(product.getBrand())
                        .append(" | name=")
                        .append(product.getName())
                        .append(" | category=")
                        .append(product.getCategory())
                        .append(" | price=")
                        .append(product.getCurrency())
                        .append(" ")
                        .append(product.getPrice())
                        .append(" | key ingredients=")
                        .append(listOrNotProvided(product.getKeyIngredients()))
                        .append(System.lineSeparator()));
        return builder.toString();
    }

    private String valueOrNotProvided(Object value) {
        if (value == null) {
            return "Not provided";
        }

        String text = String.valueOf(value);
        return StringUtils.hasText(text) ? text : "Not provided";
    }

    private String listOrNotProvided(List<String> values) {
        if (values == null || values.isEmpty()) {
            return "Not provided";
        }

        return String.join(", ", values);
    }
}
