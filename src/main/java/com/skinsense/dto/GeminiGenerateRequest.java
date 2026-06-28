package com.skinsense.dto;

import java.util.List;

public class GeminiGenerateRequest {

    private List<GeminiContent> contents;
    private GeminiGenerationConfig generationConfig;

    public GeminiGenerateRequest(String prompt) {
        this.contents = List.of(new GeminiContent("user", List.of(new GeminiPart(prompt))));
        this.generationConfig = new GeminiGenerationConfig("application/json", 0.2);
    }

    public List<GeminiContent> getContents() {
        return contents;
    }

    public void setContents(List<GeminiContent> contents) {
        this.contents = contents;
    }

    public GeminiGenerationConfig getGenerationConfig() {
        return generationConfig;
    }

    public void setGenerationConfig(GeminiGenerationConfig generationConfig) {
        this.generationConfig = generationConfig;
    }
}
