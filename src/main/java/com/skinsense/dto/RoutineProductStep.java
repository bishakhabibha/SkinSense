package com.skinsense.dto;

public class RoutineProductStep {

    private String period;
    private String stepName;
    private int order;
    private boolean optional;
    private String frequency;
    private String instruction;
    private String whyPicked;
    private ProductMatch productMatch;

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public String getStepName() {
        return stepName;
    }

    public void setStepName(String stepName) {
        this.stepName = stepName;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public boolean isOptional() {
        return optional;
    }

    public void setOptional(boolean optional) {
        this.optional = optional;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public ProductMatch getProductMatch() {
        return productMatch;
    }

    public void setProductMatch(ProductMatch productMatch) {
        this.productMatch = productMatch;
    }

    public String getInstruction() {
        return instruction;
    }

    public void setInstruction(String instruction) {
        this.instruction = instruction;
    }

    public String getWhyPicked() {
        return whyPicked;
    }

    public void setWhyPicked(String whyPicked) {
        this.whyPicked = whyPicked;
    }

    public CatalogProduct getProduct() {
        return productMatch == null ? null : productMatch.getProduct();
    }

    public String getProductId() {
        CatalogProduct product = getProduct();
        return product == null ? null : product.getId();
    }

    public String getProductName() {
        CatalogProduct product = getProduct();
        return product == null ? "" : product.getName();
    }

    public String getBrand() {
        CatalogProduct product = getProduct();
        return product == null ? "" : product.getBrand();
    }

    public String getImageUrl() {
        CatalogProduct product = getProduct();
        return product == null ? "" : product.getImageUrl();
    }

    public String getGoogleSearchUrl() {
        CatalogProduct product = getProduct();
        return product == null ? "https://www.google.com/search?q=skincare+product" : product.getGoogleSearchUrl();
    }
}
