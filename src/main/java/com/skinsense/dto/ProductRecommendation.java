package com.skinsense.dto;

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
}
