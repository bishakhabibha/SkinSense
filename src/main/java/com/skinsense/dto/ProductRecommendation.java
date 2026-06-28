package com.skinsense.dto;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class ProductRecommendation {

    private String productName;
    private String brand;
    private String whySuitable;

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getWhySuitable() {
        return whySuitable;
    }

    public void setWhySuitable(String whySuitable) {
        this.whySuitable = whySuitable;
    }

    public String getGoogleSearchUrl() {
        String query = productName == null || productName.isBlank() ? brand : productName;
        String safeQuery = query == null ? "skincare product" : query;
        return "https://www.google.com/search?q=" + URLEncoder.encode(safeQuery, StandardCharsets.UTF_8);
    }
}
