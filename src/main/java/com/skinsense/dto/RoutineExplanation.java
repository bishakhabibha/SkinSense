package com.skinsense.dto;

import java.util.ArrayList;
import java.util.List;

public class RoutineExplanation {

    private String summary;
    private List<RoutineInstruction> morningRoutine = new ArrayList<>();
    private List<RoutineInstruction> eveningRoutine = new ArrayList<>();
    private List<ProductReason> productReasons = new ArrayList<>();
    private List<String> generalTips = new ArrayList<>();
    private List<String> cautions = new ArrayList<>();

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public List<RoutineInstruction> getMorningRoutine() {
        return morningRoutine;
    }

    public void setMorningRoutine(List<RoutineInstruction> morningRoutine) {
        this.morningRoutine = morningRoutine == null ? new ArrayList<>() : morningRoutine;
    }

    public List<RoutineInstruction> getEveningRoutine() {
        return eveningRoutine;
    }

    public void setEveningRoutine(List<RoutineInstruction> eveningRoutine) {
        this.eveningRoutine = eveningRoutine == null ? new ArrayList<>() : eveningRoutine;
    }

    public List<ProductReason> getProductReasons() {
        return productReasons;
    }

    public void setProductReasons(List<ProductReason> productReasons) {
        this.productReasons = productReasons == null ? new ArrayList<>() : productReasons;
    }

    public List<String> getGeneralTips() {
        return generalTips;
    }

    public void setGeneralTips(List<String> generalTips) {
        this.generalTips = generalTips == null ? new ArrayList<>() : generalTips;
    }

    public List<String> getCautions() {
        return cautions;
    }

    public void setCautions(List<String> cautions) {
        this.cautions = cautions == null ? new ArrayList<>() : cautions;
    }
}
