package com.skinsense.dto;

public class RoutineNeeds {

    private boolean cleanser;
    private boolean treatment;
    private boolean moisturizer;
    private boolean sunscreen;

    public boolean isCleanser() {
        return cleanser;
    }

    public void setCleanser(boolean cleanser) {
        this.cleanser = cleanser;
    }

    public boolean isTreatment() {
        return treatment;
    }

    public void setTreatment(boolean treatment) {
        this.treatment = treatment;
    }

    public boolean isMoisturizer() {
        return moisturizer;
    }

    public void setMoisturizer(boolean moisturizer) {
        this.moisturizer = moisturizer;
    }

    public boolean isSunscreen() {
        return sunscreen;
    }

    public void setSunscreen(boolean sunscreen) {
        this.sunscreen = sunscreen;
    }
}
