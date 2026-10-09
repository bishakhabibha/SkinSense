package com.skinsense.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SkincareRoutine {

    private List<RoutineProductStep> morning = new ArrayList<>();
    private List<RoutineProductStep> evening = new ArrayList<>();
    private BigDecimal totalCost = BigDecimal.ZERO;
    private String currency = "BDT";
    private BudgetRange budgetRange = BudgetRange.UNDER_4000;
    private int routineScore;
    private boolean exactBudgetFit;
    private boolean complete;
    private String fallbackMessage;
    private List<String> explanationMetadata = new ArrayList<>();

    public List<RoutineProductStep> getMorning() {
        return morning;
    }

    public void setMorning(List<RoutineProductStep> morning) {
        this.morning = morning == null ? new ArrayList<>() : morning;
    }

    public List<RoutineProductStep> getEvening() {
        return evening;
    }

    public void setEvening(List<RoutineProductStep> evening) {
        this.evening = evening == null ? new ArrayList<>() : evening;
    }

    public BigDecimal getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(BigDecimal totalCost) {
        this.totalCost = totalCost == null ? BigDecimal.ZERO : totalCost;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BudgetRange getBudgetRange() {
        return budgetRange;
    }

    public void setBudgetRange(BudgetRange budgetRange) {
        this.budgetRange = budgetRange == null ? BudgetRange.UNDER_4000 : budgetRange;
    }

    public int getRoutineScore() {
        return routineScore;
    }

    public void setRoutineScore(int routineScore) {
        this.routineScore = routineScore;
    }

    public boolean isExactBudgetFit() {
        return exactBudgetFit;
    }

    public void setExactBudgetFit(boolean exactBudgetFit) {
        this.exactBudgetFit = exactBudgetFit;
    }

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    public String getFallbackMessage() {
        return fallbackMessage;
    }

    public void setFallbackMessage(String fallbackMessage) {
        this.fallbackMessage = fallbackMessage;
    }

    public List<String> getExplanationMetadata() {
        return explanationMetadata;
    }

    public void setExplanationMetadata(List<String> explanationMetadata) {
        this.explanationMetadata = explanationMetadata == null ? new ArrayList<>() : explanationMetadata;
    }

    public String getFormattedTotalCost() {
        return "BDT " + totalCost.toPlainString();
    }

    public String getBudgetStatusLabel() {
        return exactBudgetFit ? "Fits selected total budget" : "No exact total-budget fit";
    }

    public int getUniqueProductCount() {
        return getUniqueProductSteps().size();
    }

    public List<RoutineProductStep> getUniqueProductSteps() {
        Map<String, RoutineProductStep> uniqueSteps = new LinkedHashMap<>();
        for (RoutineProductStep step : morning) {
            addUniqueStep(uniqueSteps, step);
        }
        for (RoutineProductStep step : evening) {
            addUniqueStep(uniqueSteps, step);
        }
        return new ArrayList<>(uniqueSteps.values());
    }

    private void addUniqueStep(Map<String, RoutineProductStep> uniqueSteps, RoutineProductStep step) {
        if (step == null || step.getProductId() == null || step.getProductId().isBlank()) {
            return;
        }
        uniqueSteps.putIfAbsent(step.getProductId(), step);
    }
}
