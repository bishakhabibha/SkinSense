package com.skinsense.dto;

public class RoutineStep {

    private String stepName;
    private String recommendedIngredients;
    private String suggestedProducts;
    private String brand;
    private String approximatePriceCategory;
    private String scientificReasoning;

    public String getStepName() {
        return stepName;
    }

    public void setStepName(String stepName) {
        this.stepName = stepName;
    }

    public String getRecommendedIngredients() {
        return recommendedIngredients;
    }

    public void setRecommendedIngredients(String recommendedIngredients) {
        this.recommendedIngredients = recommendedIngredients;
    }

    public String getSuggestedProducts() {
        return suggestedProducts;
    }

    public void setSuggestedProducts(String suggestedProducts) {
        this.suggestedProducts = suggestedProducts;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getApproximatePriceCategory() {
        return approximatePriceCategory;
    }

    public void setApproximatePriceCategory(String approximatePriceCategory) {
        this.approximatePriceCategory = approximatePriceCategory;
    }

    public String getScientificReasoning() {
        return scientificReasoning;
    }

    public void setScientificReasoning(String scientificReasoning) {
        this.scientificReasoning = scientificReasoning;
    }
}
