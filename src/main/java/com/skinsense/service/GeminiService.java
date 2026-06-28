package com.skinsense.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skinsense.dto.AssessmentRequest;
import com.skinsense.dto.GeminiGenerateRequest;
import com.skinsense.dto.GeminiGenerateResponse;
import com.skinsense.dto.GeminiPart;
import com.skinsense.dto.SkincareReport;
import com.skinsense.exception.GeminiException;
import com.skinsense.util.GeminiPromptBuilder;
import com.skinsense.util.JsonReportValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Service
public class GeminiService {

    private final String apiKey;
    private final String model;
    private final String apiUrl;
    private final int timeoutSeconds;
    private final ObjectMapper objectMapper;
    private final GeminiPromptBuilder promptBuilder;
    private final JsonReportValidator reportValidator;
    private final HttpClient httpClient;

    public GeminiService(
            @Value("${gemini.api.key:}") String apiKey,
            @Value("${gemini.model}") String model,
            @Value("${gemini.api.url}") String apiUrl,
            @Value("${gemini.timeout.seconds}") int timeoutSeconds,
            ObjectMapper objectMapper,
            GeminiPromptBuilder promptBuilder,
            JsonReportValidator reportValidator
    ) {
        this.apiKey = apiKey;
        this.model = model;
        this.apiUrl = apiUrl;
        this.timeoutSeconds = timeoutSeconds;
        this.objectMapper = objectMapper;
        this.promptBuilder = promptBuilder;
        this.reportValidator = reportValidator;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(timeoutSeconds))
                .build();
    }

    public SkincareReport generateReport(AssessmentRequest assessmentRequest) {
        if (!StringUtils.hasText(apiKey)) {
            throw new GeminiException("The Gemini API key is missing. Add GEMINI_API_KEY to your .env file and restart the app.");
        }

        try {
            String prompt = promptBuilder.buildPrompt(assessmentRequest);
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

            String reportJson = extractReportJson(response.body());
            return reportValidator.parseAndValidate(reportJson);
        } catch (HttpTimeoutException exception) {
            throw new GeminiException("Gemini took too long to respond. Please try again in a moment.", exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new GeminiException("The request was interrupted. Please try again.", exception);
        } catch (IOException exception) {
            throw new GeminiException("Could not connect to Gemini. Check your internet connection and try again.", exception);
        }
    }

    private URI buildGeminiUri() {
        String encodedKey = URLEncoder.encode(apiKey, StandardCharsets.UTF_8);
        String modelPath = model.startsWith("models/") ? model.substring("models/".length()) : model;
        return URI.create(apiUrl + "/" + modelPath + ":generateContent?key=" + encodedKey);
    }

    private String extractReportJson(String responseBody) {
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
                throw new GeminiException("Gemini returned an empty report. Please try again.");
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
