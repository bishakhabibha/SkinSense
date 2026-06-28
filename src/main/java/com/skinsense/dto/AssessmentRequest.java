package com.skinsense.dto;

import java.util.ArrayList;
import java.util.List;

public class AssessmentRequest {

    private Integer age;
    private String gender;
    private String skinType;
    private String skinSensitivity;
    private List<String> concerns = new ArrayList<>();
    private String country;
    private String timeOutdoors;
    private String sunscreenUsage;
    private String budget;
    private String routinePreference;
    private String crueltyFree;
    private String fragranceFree;

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getSkinType() {
        return skinType;
    }

    public void setSkinType(String skinType) {
        this.skinType = skinType;
    }

    public String getSkinSensitivity() {
        return skinSensitivity;
    }

    public void setSkinSensitivity(String skinSensitivity) {
        this.skinSensitivity = skinSensitivity;
    }

    public List<String> getConcerns() {
        return concerns;
    }

    public void setConcerns(List<String> concerns) {
        this.concerns = concerns == null ? new ArrayList<>() : concerns;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getTimeOutdoors() {
        return timeOutdoors;
    }

    public void setTimeOutdoors(String timeOutdoors) {
        this.timeOutdoors = timeOutdoors;
    }

    public String getSunscreenUsage() {
        return sunscreenUsage;
    }

    public void setSunscreenUsage(String sunscreenUsage) {
        this.sunscreenUsage = sunscreenUsage;
    }

    public String getBudget() {
        return budget;
    }

    public void setBudget(String budget) {
        this.budget = budget;
    }

    public String getRoutinePreference() {
        return routinePreference;
    }

    public void setRoutinePreference(String routinePreference) {
        this.routinePreference = routinePreference;
    }

    public String getCrueltyFree() {
        return crueltyFree;
    }

    public void setCrueltyFree(String crueltyFree) {
        this.crueltyFree = crueltyFree;
    }

    public String getFragranceFree() {
        return fragranceFree;
    }

    public void setFragranceFree(String fragranceFree) {
        this.fragranceFree = fragranceFree;
    }

    public int getConcernCount() {
        return concerns == null ? 0 : concerns.size();
    }

    public String getDisplaySkinType() {
        return displayValue(skinType);
    }

    public String getDisplaySkinSensitivity() {
        return displayValue(skinSensitivity);
    }

    public String getDisplayBudget() {
        return displayValue(budget);
    }

    private String displayValue(String value) {
        if (value == null || value.isBlank()) {
            return "Not provided";
        }

        String text = value.replace("-", " ");
        String[] words = text.split(" ");
        StringBuilder builder = new StringBuilder();

        for (String word : words) {
            if (word.isBlank()) {
                continue;
            }

            if (!builder.isEmpty()) {
                builder.append(' ');
            }

            builder.append(Character.toUpperCase(word.charAt(0)));
            if (word.length() > 1) {
                builder.append(word.substring(1));
            }
        }

        return builder.toString();
    }
}
