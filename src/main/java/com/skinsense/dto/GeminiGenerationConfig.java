package com.skinsense.dto;

public class GeminiGenerationConfig {

    private String responseMimeType;
    private double temperature;

    public GeminiGenerationConfig() {
    }

    public GeminiGenerationConfig(String responseMimeType, double temperature) {
        this.responseMimeType = responseMimeType;
        this.temperature = temperature;
    }

    public String getResponseMimeType() {
        return responseMimeType;
    }

    public void setResponseMimeType(String responseMimeType) {
        this.responseMimeType = responseMimeType;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }
}
