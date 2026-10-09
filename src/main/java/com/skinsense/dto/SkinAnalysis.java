package com.skinsense.dto;

import java.util.ArrayList;
import java.util.List;

public class SkinAnalysis {

    private String skinType;
    private String sensitivity;
    private List<String> concerns = new ArrayList<>();
    private List<String> recommendedIngredients = new ArrayList<>();
    private List<String> avoidIngredients = new ArrayList<>();
    private RoutineNeeds routineNeeds = new RoutineNeeds();
    private String reasoning;

    public String getSkinType() {
        return skinType;
    }

    public void setSkinType(String skinType) {
        this.skinType = skinType;
    }

    public String getSensitivity() {
        return sensitivity;
    }

    public void setSensitivity(String sensitivity) {
        this.sensitivity = sensitivity;
    }

    public List<String> getConcerns() {
        return concerns;
    }

    public void setConcerns(List<String> concerns) {
        this.concerns = concerns == null ? new ArrayList<>() : concerns;
    }

    public List<String> getRecommendedIngredients() {
        return recommendedIngredients;
    }

    public void setRecommendedIngredients(List<String> recommendedIngredients) {
        this.recommendedIngredients = recommendedIngredients == null ? new ArrayList<>() : recommendedIngredients;
    }

    public List<String> getAvoidIngredients() {
        return avoidIngredients;
    }

    public void setAvoidIngredients(List<String> avoidIngredients) {
        this.avoidIngredients = avoidIngredients == null ? new ArrayList<>() : avoidIngredients;
    }

    public RoutineNeeds getRoutineNeeds() {
        return routineNeeds;
    }

    public void setRoutineNeeds(RoutineNeeds routineNeeds) {
        this.routineNeeds = routineNeeds == null ? new RoutineNeeds() : routineNeeds;
    }

    public String getReasoning() {
        return reasoning;
    }

    public void setReasoning(String reasoning) {
        this.reasoning = reasoning;
    }
}
