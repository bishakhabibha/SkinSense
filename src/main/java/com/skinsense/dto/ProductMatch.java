package com.skinsense.dto;

import java.util.ArrayList;
import java.util.List;

public class ProductMatch {

    private CatalogProduct product;
    private int compatibilityScore;
    private List<String> matchedSkinTypes = new ArrayList<>();
    private List<String> matchedConcerns = new ArrayList<>();
    private List<String> matchedIngredients = new ArrayList<>();
    private List<String> avoidIngredientMatches = new ArrayList<>();
    private List<String> reasons = new ArrayList<>();
    private List<String> filterNotes = new ArrayList<>();

    public CatalogProduct getProduct() {
        return product;
    }

    public void setProduct(CatalogProduct product) {
        this.product = product;
    }

    public int getCompatibilityScore() {
        return compatibilityScore;
    }

    public void setCompatibilityScore(int compatibilityScore) {
        this.compatibilityScore = compatibilityScore;
    }

    public List<String> getMatchedSkinTypes() {
        return matchedSkinTypes;
    }

    public void setMatchedSkinTypes(List<String> matchedSkinTypes) {
        this.matchedSkinTypes = matchedSkinTypes == null ? new ArrayList<>() : matchedSkinTypes;
    }

    public List<String> getMatchedConcerns() {
        return matchedConcerns;
    }

    public void setMatchedConcerns(List<String> matchedConcerns) {
        this.matchedConcerns = matchedConcerns == null ? new ArrayList<>() : matchedConcerns;
    }

    public List<String> getMatchedIngredients() {
        return matchedIngredients;
    }

    public void setMatchedIngredients(List<String> matchedIngredients) {
        this.matchedIngredients = matchedIngredients == null ? new ArrayList<>() : matchedIngredients;
    }

    public List<String> getAvoidIngredientMatches() {
        return avoidIngredientMatches;
    }

    public void setAvoidIngredientMatches(List<String> avoidIngredientMatches) {
        this.avoidIngredientMatches = avoidIngredientMatches == null ? new ArrayList<>() : avoidIngredientMatches;
    }

    public List<String> getReasons() {
        return reasons;
    }

    public void setReasons(List<String> reasons) {
        this.reasons = reasons == null ? new ArrayList<>() : reasons;
    }

    public List<String> getFilterNotes() {
        return filterNotes;
    }

    public void setFilterNotes(List<String> filterNotes) {
        this.filterNotes = filterNotes == null ? new ArrayList<>() : filterNotes;
    }
}
