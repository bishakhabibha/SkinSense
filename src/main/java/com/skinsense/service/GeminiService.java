package com.skinsense.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skinsense.dto.AssessmentRequest;
import com.skinsense.dto.ProductReason;
import com.skinsense.dto.RoutineExplanation;
import com.skinsense.dto.RoutineInstruction;
import com.skinsense.dto.GeminiGenerateRequest;
import com.skinsense.dto.GeminiGenerateResponse;
import com.skinsense.dto.GeminiPart;
import com.skinsense.dto.IngredientRecommendation;
import com.skinsense.dto.RoutineStep;
import com.skinsense.dto.RoutineProductStep;
import com.skinsense.dto.SafetyWarning;
import com.skinsense.dto.SkinAnalysis;
import com.skinsense.dto.SkincareRoutine;
import com.skinsense.dto.SkincareReport;
import com.skinsense.exception.GeminiException;
import com.skinsense.util.GeminiPromptBuilder;
import com.skinsense.util.RoutineExplanationValidator;
import com.skinsense.util.SkinAnalysisValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class GeminiService {

    private final String apiKey;
    private final String model;
    private final String apiUrl;
    private final int timeoutSeconds;
    private final ObjectMapper objectMapper;
    private final GeminiPromptBuilder promptBuilder;
    private final SkinAnalysisValidator skinAnalysisValidator;
    private final RoutineExplanationValidator routineExplanationValidator;
    private final RoutineOptimizer routineOptimizer;
    private final HttpClient httpClient;

    public GeminiService(
            @Value("${gemini.api.key:}") String apiKey,
            @Value("${gemini.model}") String model,
            @Value("${gemini.api.url}") String apiUrl,
            @Value("${gemini.timeout.seconds}") int timeoutSeconds,
            ObjectMapper objectMapper,
            GeminiPromptBuilder promptBuilder,
            SkinAnalysisValidator skinAnalysisValidator,
            RoutineExplanationValidator routineExplanationValidator,
            RoutineOptimizer routineOptimizer
    ) {
        this.apiKey = apiKey;
        this.model = model;
        this.apiUrl = apiUrl;
        this.timeoutSeconds = timeoutSeconds;
        this.objectMapper = objectMapper;
        this.promptBuilder = promptBuilder;
        this.skinAnalysisValidator = skinAnalysisValidator;
        this.routineExplanationValidator = routineExplanationValidator;
        this.routineOptimizer = routineOptimizer;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(timeoutSeconds))
                .build();
    }

    public SkincareReport generateReport(AssessmentRequest assessmentRequest) {
        SkinAnalysis analysis = analyzeSkinProfile(assessmentRequest);
        SkincareRoutine routine = routineOptimizer.optimize(analysis, assessmentRequest);
        applyRoutineExplanation(assessmentRequest, analysis, routine);
        return buildFinalReport(analysis, routine);
    }

    public SkinAnalysis analyzeSkinProfile(AssessmentRequest assessmentRequest) {
        if (!StringUtils.hasText(apiKey)) {
            throw new GeminiException("The Gemini API key is missing. Add GEMINI_API_KEY to your .env file and restart the app.");
        }

        String prompt = promptBuilder.buildPrompt(assessmentRequest);
        String analysisJson = sendGeminiPrompt(prompt);
        return skinAnalysisValidator.parseAndValidate(analysisJson);
    }

    private void applyRoutineExplanation(AssessmentRequest assessmentRequest, SkinAnalysis analysis, SkincareRoutine routine) {
        if (!routine.isComplete()) {
            applyDeterministicExplanation(routine, analysis);
            return;
        }

        try {
            String prompt = promptBuilder.buildRoutineExplanationPrompt(assessmentRequest, analysis, routine);
            String explanationJson = sendGeminiPrompt(prompt);
            RoutineExplanation explanation = routineExplanationValidator.parseAndValidate(explanationJson, routine);
            applyGeminiExplanation(routine, explanation);
        } catch (GeminiException exception) {
            applyDeterministicExplanation(routine, analysis);
        }
    }

    private SkincareReport buildFinalReport(SkinAnalysis analysis, SkincareRoutine routine) {
        SkincareReport report = new SkincareReport();
        report.setSkincareRoutine(routine);
        report.setSkinSummary(buildSkinSummary(analysis, routine));
        report.setMorningRoutine(buildRoutineSteps(routine.getMorning(), analysis));
        report.setNightRoutine(buildRoutineSteps(routine.getEvening(), analysis));
        report.setIngredientsToUse(analysis.getRecommendedIngredients().stream()
                .map(ingredient -> ingredient(ingredient, "Gemini identified this ingredient category as relevant to the skin profile and selected concerns."))
                .toList());
        report.setIngredientsToAvoid(analysis.getAvoidIngredients().stream()
                .map(ingredient -> ingredient(ingredient, "Use this category cautiously based on the assessment and stated preferences."))
                .toList());
        report.setSafetyWarnings(List.of(
                warning("Patch Test", "Patch test new products before applying them widely, especially when introducing active ingredients."),
                warning("Sunscreen", "Use sunscreen in the morning routine and reapply when outdoors for extended periods."),
                warning("Active Ingredients", "Introduce strong actives slowly and avoid starting multiple new actives at the same time."),
                warning("Dermatologist", "Consult a dermatologist if irritation, acne, pigmentation or redness persists or worsens.")
        ));
        report.setDisclaimer("This application provides AI-generated skincare guidance and is not a substitute for professional dermatological advice.");
        return report;
    }

    private String buildSkinSummary(SkinAnalysis analysis, SkincareRoutine routine) {
        String summary = analysis.getReasoning();
        if (routine.getExplanationMetadata() != null && !routine.getExplanationMetadata().isEmpty()) {
            Optional<String> geminiSummary = routine.getExplanationMetadata().stream()
                    .filter(note -> note.startsWith("Gemini explanation: "))
                    .findFirst();
            if (geminiSummary.isPresent()) {
                return geminiSummary.get().replaceFirst("Gemini explanation: ", "");
            }
        }
        return summary;
    }

    private List<RoutineStep> buildRoutineSteps(List<RoutineProductStep> routineProductSteps, SkinAnalysis analysis) {
        List<RoutineStep> steps = new ArrayList<>();
        for (RoutineProductStep routineProductStep : routineProductSteps) {
            steps.add(routineStep(routineProductStep, analysis));
        }
        return steps;
    }

    private RoutineStep routineStep(RoutineProductStep routineProductStep, SkinAnalysis analysis) {
        RoutineStep step = new RoutineStep();
        step.setStepName(routineProductStep.getStepName());
        step.setRecommendedIngredients(String.join(", ", routineProductStep.getProductMatch().getMatchedIngredients()));
        step.setSuggestedProducts(routineProductStep.getProductMatch().getProduct().getName());
        step.setBrand(routineProductStep.getProductMatch().getProduct().getBrand());
        step.setApproximatePriceCategory("BDT " + routineProductStep.getProductMatch().getProduct().getPrice());
        step.setScientificReasoning(buildRoutineReasoning(routineProductStep, analysis));
        return step;
    }

    private String buildRoutineReasoning(RoutineProductStep routineProductStep, SkinAnalysis analysis) {
        if (StringUtils.hasText(routineProductStep.getWhyPicked())) {
            return routineProductStep.getWhyPicked();
        }

        String matchReasoning = String.join(" ", routineProductStep.getProductMatch().getReasons());
        if (StringUtils.hasText(matchReasoning)) {
            return matchReasoning;
        }
        return analysis.getReasoning();
    }

    private void applyGeminiExplanation(SkincareRoutine routine, RoutineExplanation explanation) {
        if (StringUtils.hasText(explanation.getSummary())) {
            routine.getExplanationMetadata().add("Gemini explanation: " + explanation.getSummary());
        }

        applyInstructions(routine.getMorning(), explanation.getMorningRoutine());
        applyInstructions(routine.getEvening(), explanation.getEveningRoutine());
        applyProductReasons(routine, explanation.getProductReasons());

        for (String caution : explanation.getCautions()) {
            if (StringUtils.hasText(caution)) {
                routine.getExplanationMetadata().add("Caution: " + caution);
            }
        }
        for (String tip : explanation.getGeneralTips()) {
            if (StringUtils.hasText(tip)) {
                routine.getExplanationMetadata().add("Tip: " + tip);
            }
        }

        fillMissingExplanations(routine);
    }

    private void applyInstructions(List<RoutineProductStep> steps, List<RoutineInstruction> instructions) {
        for (RoutineProductStep step : steps) {
            instructions.stream()
                    .filter(instruction -> instruction.getStep() == step.getOrder())
                    .filter(instruction -> step.getProductId().equals(instruction.getProductId()))
                    .findFirst()
                    .ifPresent(instruction -> step.setInstruction(instruction.getInstruction()));
        }
    }

    private void applyProductReasons(SkincareRoutine routine, List<ProductReason> reasons) {
        List<RoutineProductStep> allSteps = new ArrayList<>();
        allSteps.addAll(routine.getMorning());
        allSteps.addAll(routine.getEvening());

        for (RoutineProductStep step : allSteps) {
            reasons.stream()
                    .filter(reason -> step.getProductId().equals(reason.getProductId()))
                    .findFirst()
                    .ifPresent(reason -> step.setWhyPicked(reason.getReason()));
        }
    }

    private void applyDeterministicExplanation(SkincareRoutine routine, SkinAnalysis analysis) {
        for (RoutineProductStep step : routine.getMorning()) {
            applyFallbackStepExplanation(step, analysis);
        }
        for (RoutineProductStep step : routine.getEvening()) {
            applyFallbackStepExplanation(step, analysis);
        }
        routine.getExplanationMetadata().add("Routine explanation used deterministic SkinSense wording because Gemini explanation was unavailable.");
    }

    private void applyFallbackStepExplanation(RoutineProductStep step, SkinAnalysis analysis) {
        if (step.getProductMatch() == null || step.getProductMatch().getProduct() == null) {
            return;
        }

        if (!StringUtils.hasText(step.getInstruction())) {
            step.setInstruction(step.getFrequency());
        }

        String reasons = String.join(" ", step.getProductMatch().getReasons());
        if (!StringUtils.hasText(reasons)) {
            reasons = "Chosen because it is a verified catalog product that best matched your skin profile and selected routine needs.";
        }

        if (!StringUtils.hasText(step.getWhyPicked())) {
            step.setWhyPicked(reasons);
        }
    }

    private void fillMissingExplanations(SkincareRoutine routine) {
        for (RoutineProductStep step : routine.getMorning()) {
            applyFallbackStepExplanation(step, null);
        }
        for (RoutineProductStep step : routine.getEvening()) {
            applyFallbackStepExplanation(step, null);
        }
    }

    private String sendGeminiPrompt(String prompt) {
        try {
            GeminiGenerateRequest geminiRequest = new GeminiGenerateRequest(prompt);
            String requestBody = objectMapper.writeValueAsString(geminiRequest);

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(buildGeminiUri())
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new GeminiException(buildHttpErrorMessage(response.statusCode(), response.body()));
            }

            return extractResponseText(response.body());
        } catch (JsonProcessingException exception) {
            throw new GeminiException("SkinSense could not prepare the Gemini request. Please try again.", exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new GeminiException("The request was interrupted. Please try again.", exception);
        } catch (IOException exception) {
            if (exception instanceof java.net.http.HttpTimeoutException) {
                throw new GeminiException("Gemini took too long to respond. Please try again in a moment.", exception);
            }
            throw new GeminiException("Could not connect to Gemini. Check your internet connection and try again.", exception);
        }
    }

    private IngredientRecommendation ingredient(String name, String explanation) {
        IngredientRecommendation ingredient = new IngredientRecommendation();
        ingredient.setIngredient(name);
        ingredient.setExplanation(explanation);
        return ingredient;
    }

    private SafetyWarning warning(String topic, String guidance) {
        SafetyWarning warning = new SafetyWarning();
        warning.setTopic(topic);
        warning.setGuidance(guidance);
        return warning;
    }

    private URI buildGeminiUri() {
        String encodedKey = URLEncoder.encode(apiKey, StandardCharsets.UTF_8);
        String modelPath = model.startsWith("models/") ? model.substring("models/".length()) : model;
        return URI.create(apiUrl + "/" + modelPath + ":generateContent?key=" + encodedKey);
    }

    private String extractResponseText(String responseBody) {
        try {
            GeminiGenerateResponse response = objectMapper.readValue(responseBody, GeminiGenerateResponse.class);

            if (response.getCandidates().isEmpty()
                    || response.getCandidates().getFirst().getContent() == null
                    || response.getCandidates().getFirst().getContent().getParts() == null
                    || response.getCandidates().getFirst().getContent().getParts().isEmpty()) {
                throw new GeminiException("Gemini returned an empty response. Please try again.");
            }

            GeminiPart firstPart = response.getCandidates().getFirst().getContent().getParts().getFirst();
            if (!StringUtils.hasText(firstPart.getText())) {
                throw new GeminiException("Gemini returned an empty response. Please try again.");
            }

            return firstPart.getText();
        } catch (JsonProcessingException exception) {
            throw new GeminiException("Gemini returned an invalid API response. Please try again.", exception);
        }
    }

    private String buildHttpErrorMessage(int statusCode, String responseBody) {
        String defaultMessage = "Gemini returned an HTTP " + statusCode + " error. Please try again.";

        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode messageNode = root.path("error").path("message");

            if (messageNode.isTextual() && StringUtils.hasText(messageNode.asText())) {
                return "Gemini API error: " + messageNode.asText();
            }
        } catch (JsonProcessingException ignored) {
            return defaultMessage;
        }

        return defaultMessage;
    }
}
