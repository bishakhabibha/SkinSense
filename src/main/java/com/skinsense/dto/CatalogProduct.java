package com.skinsense.dto;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class CatalogProduct {

    private String id;
    private String name;
    private String brand;
    private String category;
    private BigDecimal price;
    private String currency;
    private BigDecimal sizeValue;
    private String sizeUnit;
    private String retailer;
    private String productUrl;
    private String sourceProductUrl;
    private String imageUrl;
    private List<String> skinTypes = new ArrayList<>();
    private List<String> concerns = new ArrayList<>();
    private List<String> keyIngredients = new ArrayList<>();
    private List<String> ingredients = new ArrayList<>();
    private String sensitivitySuitability;
    private Boolean fragranceFree;
    private Boolean crueltyFree;
    private String priceLastVerified;
    private boolean verified;
    private boolean imageVerified;
    private boolean ingredientsVerified;
    private boolean readyForRecommendation;
    private String verificationNotes;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getCurrency() {
        return currency;
    }

    public BigDecimal getSizeValue() {
        return sizeValue;
    }

    public void setSizeValue(BigDecimal sizeValue) {
        this.sizeValue = sizeValue;
    }

    public String getSizeUnit() {
        return sizeUnit;
    }

    public void setSizeUnit(String sizeUnit) {
        this.sizeUnit = sizeUnit;
    }

    public String getSizeLabel() {
        if (sizeValue == null || sizeUnit == null || sizeUnit.isBlank()) {
            return "Size not verified";
        }
        return sizeValue.stripTrailingZeros().toPlainString() + " " + sizeUnit;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getRetailer() {
        return retailer;
    }

    public void setRetailer(String retailer) {
        this.retailer = retailer;
    }

    public String getProductUrl() {
        return productUrl;
    }

    public void setProductUrl(String productUrl) {
        this.productUrl = productUrl;
    }

    public String getSourceProductUrl() {
        return sourceProductUrl;
    }

    public void setSourceProductUrl(String sourceProductUrl) {
        this.sourceProductUrl = sourceProductUrl;
    }

    public String getGoogleSearchUrl() {
        String query = ((brand == null ? "" : brand) + " " + (name == null ? "" : name)).trim();
        String safeQuery = query.isBlank() ? "skincare product" : query;
        return "https://www.google.com/search?q=" + URLEncoder.encode(safeQuery, StandardCharsets.UTF_8);
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public List<String> getSkinTypes() {
        return skinTypes;
    }

    public void setSkinTypes(List<String> skinTypes) {
        this.skinTypes = skinTypes == null ? new ArrayList<>() : skinTypes;
    }

    public List<String> getConcerns() {
        return concerns;
    }

    public void setConcerns(List<String> concerns) {
        this.concerns = concerns == null ? new ArrayList<>() : concerns;
    }

    public List<String> getKeyIngredients() {
        return keyIngredients;
    }

    public void setKeyIngredients(List<String> keyIngredients) {
        this.keyIngredients = keyIngredients == null ? new ArrayList<>() : keyIngredients;
    }

    public List<String> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<String> ingredients) {
        this.ingredients = ingredients == null ? new ArrayList<>() : ingredients;
    }

    public String getSensitivitySuitability() {
        return sensitivitySuitability;
    }

    public void setSensitivitySuitability(String sensitivitySuitability) {
        this.sensitivitySuitability = sensitivitySuitability;
    }

    public Boolean getFragranceFree() {
        return fragranceFree;
    }

    public void setFragranceFree(Boolean fragranceFree) {
        this.fragranceFree = fragranceFree;
    }

    public Boolean getCrueltyFree() {
        return crueltyFree;
    }

    public void setCrueltyFree(Boolean crueltyFree) {
        this.crueltyFree = crueltyFree;
    }

    public String getPriceLastVerified() {
        return priceLastVerified;
    }

    public void setPriceLastVerified(String priceLastVerified) {
        this.priceLastVerified = priceLastVerified;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public boolean isImageVerified() {
        return imageVerified;
    }

    public void setImageVerified(boolean imageVerified) {
        this.imageVerified = imageVerified;
    }

    public boolean isIngredientsVerified() {
        return ingredientsVerified;
    }

    public void setIngredientsVerified(boolean ingredientsVerified) {
        this.ingredientsVerified = ingredientsVerified;
    }

    public boolean isReadyForRecommendation() {
        return readyForRecommendation;
    }

    public void setReadyForRecommendation(boolean readyForRecommendation) {
        this.readyForRecommendation = readyForRecommendation;
    }

    public String getVerificationNotes() {
        return verificationNotes;
    }

    public void setVerificationNotes(String verificationNotes) {
        this.verificationNotes = verificationNotes;
    }
}
