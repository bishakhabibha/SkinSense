package com.skinsense.dto;

import java.util.ArrayList;
import java.util.List;

public class ProductCatalog {

    private int schemaVersion;
    private String lastUpdated;
    private List<String> allowedCategories = new ArrayList<>();
    private List<String> allowedSkinTypes = new ArrayList<>();
    private List<String> allowedConcerns = new ArrayList<>();
    private List<String> allowedSensitivitySuitability = new ArrayList<>();
    private List<CatalogProduct> products = new ArrayList<>();

    public int getSchemaVersion() {
        return schemaVersion;
    }

    public void setSchemaVersion(int schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public List<String> getAllowedCategories() {
        return allowedCategories;
    }

    public void setAllowedCategories(List<String> allowedCategories) {
        this.allowedCategories = allowedCategories == null ? new ArrayList<>() : allowedCategories;
    }

    public List<String> getAllowedSkinTypes() {
        return allowedSkinTypes;
    }

    public void setAllowedSkinTypes(List<String> allowedSkinTypes) {
        this.allowedSkinTypes = allowedSkinTypes == null ? new ArrayList<>() : allowedSkinTypes;
    }

    public List<String> getAllowedConcerns() {
        return allowedConcerns;
    }

    public void setAllowedConcerns(List<String> allowedConcerns) {
        this.allowedConcerns = allowedConcerns == null ? new ArrayList<>() : allowedConcerns;
    }

    public List<String> getAllowedSensitivitySuitability() {
        return allowedSensitivitySuitability;
    }

    public void setAllowedSensitivitySuitability(List<String> allowedSensitivitySuitability) {
        this.allowedSensitivitySuitability = allowedSensitivitySuitability == null ? new ArrayList<>() : allowedSensitivitySuitability;
    }

    public List<CatalogProduct> getProducts() {
        return products;
    }

    public void setProducts(List<CatalogProduct> products) {
        this.products = products == null ? new ArrayList<>() : products;
    }
}
