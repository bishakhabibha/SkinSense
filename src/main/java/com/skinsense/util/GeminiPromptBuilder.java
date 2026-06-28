package com.skinsense.util;

import com.skinsense.dto.AssessmentRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

@Component
public class GeminiPromptBuilder {

    public String buildPrompt(AssessmentRequest request) {
        return """
                You are an experienced dermatologist and skincare consultant.

                Analyze the user's complete profile.

                Provide personalized skincare recommendations based on current dermatological best practices.

                The response must include:

                1. Skin Summary

                Briefly describe the user's likely skin characteristics.

                2. Morning Routine

                For each step include:

                * Step Name
                * Recommended Ingredients
                * Suggested Products
                * Brand
                * Approximate Price Category
                * Scientific Reasoning

                3. Night Routine

                For each step include:

                * Step Name
                * Recommended Ingredients
                * Suggested Products
                * Brand
                * Approximate Price Category
                * Scientific Reasoning

                4. Ingredients To Look For

                Explain why each ingredient is beneficial.

                5. Ingredients To Avoid

                Explain why each ingredient should be avoided.

                6. Product Recommendations

                Create three categories:

                * Affordable
                * Mid-range
                * Premium

                Recommend 3 to 5 products in each category.

                For every product include:

                * Product Name
                * Brand
                * Why it is suitable

                7. Safety Warnings

                Include guidance about:

                * Patch testing
                * Sunscreen
                * Active ingredients
                * When to consult a dermatologist

                Keep explanations concise.

                Each explanation should be approximately 2 to 4 sentences.

                Do not repeat information.

                Return ONLY valid JSON.

                Use exactly this JSON structure and field names:

                {
                  "skinSummary": "",
                  "morningRoutine": [
                    {
                      "stepName": "",
                      "recommendedIngredients": "",
                      "suggestedProducts": "",
                      "brand": "",
                      "approximatePriceCategory": "",
                      "scientificReasoning": ""
                    }
                  ],
                  "nightRoutine": [
                    {
                      "stepName": "",
                      "recommendedIngredients": "",
                      "suggestedProducts": "",
                      "brand": "",
                      "approximatePriceCategory": "",
                      "scientificReasoning": ""
                    }
                  ],
                  "ingredientsToUse": [
                    {
                      "ingredient": "",
                      "explanation": ""
                    }
                  ],
                  "ingredientsToAvoid": [
                    {
                      "ingredient": "",
                      "explanation": ""
                    }
                  ],
                  "productRecommendations": {
                    "affordable": [
                      {
                        "productName": "",
                        "brand": "",
                        "whySuitable": ""
                      }
                    ],
                    "midRange": [
                      {
                        "productName": "",
                        "brand": "",
                        "whySuitable": ""
                      }
                    ],
                    "premium": [
                      {
                        "productName": "",
                        "brand": "",
                        "whySuitable": ""
                      }
                    ]
                  },
                  "safetyWarnings": [
                    {
                      "topic": "",
                      "guidance": ""
                    }
                  ],
                  "disclaimer": ""
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
