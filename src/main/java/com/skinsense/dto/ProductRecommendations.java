package com.skinsense.dto;

import java.util.ArrayList;
import java.util.List;

public class ProductRecommendations {

    private List<ProductRecommendation> affordable = new ArrayList<>();
    private List<ProductRecommendation> midRange = new ArrayList<>();
    private List<ProductRecommendation> premium = new ArrayList<>();

    public List<ProductRecommendation> getAffordable() {
        return affordable;
    }

    public void setAffordable(List<ProductRecommendation> affordable) {
        this.affordable = affordable == null ? new ArrayList<>() : affordable;
    }

    public List<ProductRecommendation> getMidRange() {
        return midRange;
    }

    public void setMidRange(List<ProductRecommendation> midRange) {
        this.midRange = midRange == null ? new ArrayList<>() : midRange;
    }

    public List<ProductRecommendation> getPremium() {
        return premium;
    }

    public void setPremium(List<ProductRecommendation> premium) {
        this.premium = premium == null ? new ArrayList<>() : premium;
    }
}
