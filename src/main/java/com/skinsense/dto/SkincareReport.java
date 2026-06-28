package com.skinsense.dto;

import java.util.ArrayList;
import java.util.List;

public class SkincareReport {

    private String skinSummary;
    private List<RoutineStep> morningRoutine = new ArrayList<>();
    private List<RoutineStep> nightRoutine = new ArrayList<>();
    private List<IngredientRecommendation> ingredientsToUse = new ArrayList<>();
    private List<IngredientRecommendation> ingredientsToAvoid = new ArrayList<>();
    private ProductRecommendations productRecommendations = new ProductRecommendations();
    private List<SafetyWarning> safetyWarnings = new ArrayList<>();
    private String disclaimer;

    public String getSkinSummary() {
        return skinSummary;
    }

    public void setSkinSummary(String skinSummary) {
        this.skinSummary = skinSummary;
    }

    public List<RoutineStep> getMorningRoutine() {
        return morningRoutine;
    }

    public void setMorningRoutine(List<RoutineStep> morningRoutine) {
        this.morningRoutine = morningRoutine == null ? new ArrayList<>() : morningRoutine;
    }

    public List<RoutineStep> getNightRoutine() {
        return nightRoutine;
    }

    public void setNightRoutine(List<RoutineStep> nightRoutine) {
        this.nightRoutine = nightRoutine == null ? new ArrayList<>() : nightRoutine;
    }

    public List<IngredientRecommendation> getIngredientsToUse() {
        return ingredientsToUse;
    }

    public void setIngredientsToUse(List<IngredientRecommendation> ingredientsToUse) {
        this.ingredientsToUse = ingredientsToUse == null ? new ArrayList<>() : ingredientsToUse;
    }

    public List<IngredientRecommendation> getIngredientsToAvoid() {
        return ingredientsToAvoid;
    }

    public void setIngredientsToAvoid(List<IngredientRecommendation> ingredientsToAvoid) {
        this.ingredientsToAvoid = ingredientsToAvoid == null ? new ArrayList<>() : ingredientsToAvoid;
    }

    public ProductRecommendations getProductRecommendations() {
        return productRecommendations;
    }

    public void setProductRecommendations(ProductRecommendations productRecommendations) {
        this.productRecommendations = productRecommendations == null ? new ProductRecommendations() : productRecommendations;
    }

    public List<SafetyWarning> getSafetyWarnings() {
        return safetyWarnings;
    }

    public void setSafetyWarnings(List<SafetyWarning> safetyWarnings) {
        this.safetyWarnings = safetyWarnings == null ? new ArrayList<>() : safetyWarnings;
    }

    public String getDisclaimer() {
        return disclaimer;
    }

    public void setDisclaimer(String disclaimer) {
        this.disclaimer = disclaimer;
    }
}
