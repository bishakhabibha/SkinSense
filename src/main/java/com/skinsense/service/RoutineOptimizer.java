package com.skinsense.service;

import com.skinsense.dto.AssessmentRequest;
import com.skinsense.dto.BudgetRange;
import com.skinsense.dto.ProductMatch;
import com.skinsense.dto.RoutineProductStep;
import com.skinsense.dto.SkinAnalysis;
import com.skinsense.dto.SkincareRoutine;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class RoutineOptimizer {

    private static final int MAX_CANDIDATES_PER_CATEGORY = 12;
    private static final int CORE_COMPLETENESS_POINTS = 80;
    private static final int MOISTURIZER_COMPLETENESS_POINTS = 20;
    private static final int TREATMENT_COMPLETENESS_POINTS = 20;
    private static final int MISSING_NEEDED_TREATMENT_PENALTY = 12;
    private static final int MISSING_NEEDED_MOISTURIZER_PENALTY = 18;

    private final ProductMatcher productMatcher;

    public RoutineOptimizer(ProductMatcher productMatcher) {
        this.productMatcher = productMatcher;
    }

    public SkincareRoutine optimize(SkinAnalysis analysis, AssessmentRequest assessmentRequest) {
        BudgetRange budgetRange = BudgetRange.from(assessmentRequest == null ? null : assessmentRequest.getBudget());

        List<ProductMatch> cleansers = topCandidates("cleanser", analysis, assessmentRequest);
        List<ProductMatch> treatments = treatmentCandidates(analysis, assessmentRequest);
        List<ProductMatch> moisturizers = moisturizerCandidates(analysis, assessmentRequest);
        List<ProductMatch> sunscreens = topCandidates("sunscreen", analysis, assessmentRequest);

        List<CandidateRoutine> candidates = new ArrayList<>();

        for (ProductMatch cleanser : cleansers) {
            for (ProductMatch treatment : treatments) {
                for (ProductMatch moisturizer : moisturizers) {
                    for (ProductMatch sunscreen : sunscreens) {
                        CandidateRoutine candidate = evaluate(analysis, budgetRange, cleanser, treatment, moisturizer, sunscreen);
                        if (candidate.complete()) {
                            candidates.add(candidate);
                        }
                    }
                }
            }
        }

        return chooseBestRoutine(candidates, budgetRange);
    }

    private List<ProductMatch> topCandidates(String category, SkinAnalysis analysis, AssessmentRequest assessmentRequest) {
        return productMatcher.rankProducts(category, analysis, assessmentRequest).stream()
                .limit(MAX_CANDIDATES_PER_CATEGORY)
                .toList();
    }

    private List<ProductMatch> moisturizerCandidates(SkinAnalysis analysis, AssessmentRequest assessmentRequest) {
        List<ProductMatch> candidates = new ArrayList<>(topCandidates("moisturizer", analysis, assessmentRequest));
        if (!analysis.getRoutineNeeds().isMoisturizer()) {
            candidates.add(0, null);
        }
        return candidates;
    }

    private List<ProductMatch> treatmentCandidates(SkinAnalysis analysis, AssessmentRequest assessmentRequest) {
        if (!analysis.getRoutineNeeds().isTreatment()) {
            List<ProductMatch> noTreatment = new ArrayList<>();
            noTreatment.add(null);
            return noTreatment;
        }

        return withOptionalNone(topCandidates("treatment", analysis, assessmentRequest));
    }

    private List<ProductMatch> withOptionalNone(List<ProductMatch> candidates) {
        List<ProductMatch> optionalCandidates = new ArrayList<>();
        optionalCandidates.add(null);
        optionalCandidates.addAll(candidates);
        return optionalCandidates;
    }

    private CandidateRoutine evaluate(
            SkinAnalysis analysis,
            BudgetRange budgetRange,
            ProductMatch cleanser,
            ProductMatch treatment,
            ProductMatch moisturizer,
            ProductMatch sunscreen
    ) {
        if (cleanser == null || sunscreen == null) {
            return CandidateRoutine.incomplete();
        }

        if (analysis.getRoutineNeeds().isMoisturizer() && moisturizer == null) {
            return CandidateRoutine.incomplete();
        }

        List<ProductMatch> products = Arrays.asList(cleanser, treatment, moisturizer, sunscreen);
        BigDecimal totalCost = totalUniqueCost(products);
        int score = productScore(products);
        score += CORE_COMPLETENESS_POINTS;

        if (moisturizer != null) {
            score += MOISTURIZER_COMPLETENESS_POINTS;
        } else if (analysis.getRoutineNeeds().isMoisturizer()) {
            score -= MISSING_NEEDED_MOISTURIZER_PENALTY;
        }

        if (treatment != null) {
            score += analysis.getRoutineNeeds().isTreatment() ? TREATMENT_COMPLETENESS_POINTS : TREATMENT_COMPLETENESS_POINTS / 2;
        } else if (analysis.getRoutineNeeds().isTreatment()) {
            score -= MISSING_NEEDED_TREATMENT_PENALTY;
        }

        if (budgetRange.contains(totalCost)) {
            score += 30;
        } else {
            score -= budgetPenalty(budgetRange, totalCost);
        }

        return new CandidateRoutine(cleanser, treatment, moisturizer, sunscreen, totalCost, score, true);
    }

    private SkincareRoutine chooseBestRoutine(List<CandidateRoutine> candidates, BudgetRange budgetRange) {
        List<CandidateRoutine> exactFits = candidates.stream()
                .filter(candidate -> budgetRange.contains(candidate.totalCost()))
                .toList();

        CandidateRoutine selected;
        boolean exactFit;
        if (!exactFits.isEmpty()) {
            selected = exactFits.stream()
                    .max(Comparator.comparingInt(CandidateRoutine::score)
                            .thenComparing(CandidateRoutine::totalCost))
                    .orElseThrow();
            exactFit = true;
        } else {
            selected = candidates.stream()
                    .min(Comparator.comparing((CandidateRoutine candidate) -> budgetRange.distanceFromRange(candidate.totalCost()))
                            .thenComparing(Comparator.comparingInt(CandidateRoutine::score).reversed()))
                    .orElse(CandidateRoutine.incomplete());
            exactFit = false;
        }

        return toRoutine(selected, budgetRange, exactFit);
    }

    private SkincareRoutine toRoutine(CandidateRoutine candidate, BudgetRange budgetRange, boolean exactFit) {
        SkincareRoutine routine = new SkincareRoutine();
        routine.setBudgetRange(budgetRange);
        routine.setExactBudgetFit(exactFit);
        routine.setComplete(candidate.complete());
        routine.setRoutineScore(candidate.score());
        routine.setTotalCost(candidate.totalCost());
        routine.setCurrency("BDT");

        if (!candidate.complete()) {
            routine.setFallbackMessage("SkinSense could not build a complete routine from the available catalog candidates.");
            return routine;
        }

        List<RoutineProductStep> morning = new ArrayList<>();
        List<RoutineProductStep> evening = new ArrayList<>();

        morning.add(step("morning", "Cleanser", 1, false, "Use as part of the morning routine.", candidate.cleanser()));
        if (candidate.treatment() != null) {
            morning.add(step("morning", "Treatment", 2, true, "Use according to product directions and skin tolerance.", candidate.treatment()));
        }
        if (candidate.moisturizer() != null) {
            morning.add(step("morning", "Moisturizer", 3, true, "Use as needed for comfort and barrier support.", candidate.moisturizer()));
        }
        morning.add(step("morning", "Sunscreen", 4, false, "Use every morning; reapply when outdoors for extended periods.", candidate.sunscreen()));

        evening.add(step("evening", "Cleanser", 1, false, "Use as part of the evening routine.", candidate.cleanser()));
        if (candidate.treatment() != null) {
            evening.add(step("evening", "Treatment", 2, true, "Use according to product directions and skin tolerance.", candidate.treatment()));
        }
        if (candidate.moisturizer() != null) {
            evening.add(step("evening", "Moisturizer", 3, true, "Use as needed for comfort and barrier support.", candidate.moisturizer()));
        }

        routine.setMorning(morning);
        routine.setEvening(evening);
        routine.setExplanationMetadata(explanationMetadata(candidate, exactFit, budgetRange));

        if (!exactFit) {
            routine.setFallbackMessage("No complete routine fit the selected budget exactly. This is the closest compatible complete option from the verified catalog.");
        }

        return routine;
    }

    private RoutineProductStep step(String period, String stepName, int order, boolean optional, String frequency, ProductMatch productMatch) {
        RoutineProductStep step = new RoutineProductStep();
        step.setPeriod(period);
        step.setStepName(stepName);
        step.setOrder(order);
        step.setOptional(optional);
        step.setFrequency(frequency);
        step.setProductMatch(productMatch);
        return step;
    }

    private List<String> explanationMetadata(CandidateRoutine candidate, boolean exactFit, BudgetRange budgetRange) {
        List<String> metadata = new ArrayList<>();
        metadata.add("Budget range: " + budgetRange.getLabel());
        metadata.add("Exact budget fit: " + exactFit);
        metadata.add("Total unique product cost: BDT " + candidate.totalCost());
        metadata.add("Routine score: " + candidate.score());
        metadata.add("Same product reused across AM/PM is counted once.");
        return metadata;
    }

    private BigDecimal totalUniqueCost(List<ProductMatch> matches) {
        Map<String, BigDecimal> uniquePrices = new LinkedHashMap<>();
        for (ProductMatch match : matches) {
            if (match == null || match.getProduct() == null) {
                continue;
            }
            uniquePrices.putIfAbsent(match.getProduct().getId(), match.getProduct().getPrice());
        }

        return uniquePrices.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private int productScore(List<ProductMatch> matches) {
        return matches.stream()
                .filter(match -> match != null)
                .mapToInt(ProductMatch::getCompatibilityScore)
                .sum();
    }

    private int budgetPenalty(BudgetRange budgetRange, BigDecimal totalCost) {
        BigDecimal distance = budgetRange.distanceFromRange(totalCost);
        return distance.divide(BigDecimal.valueOf(100), java.math.RoundingMode.UP).intValue();
    }

    private record CandidateRoutine(
            ProductMatch cleanser,
            ProductMatch treatment,
            ProductMatch moisturizer,
            ProductMatch sunscreen,
            BigDecimal totalCost,
            int score,
            boolean complete
    ) {

        private static CandidateRoutine incomplete() {
            return new CandidateRoutine(null, null, null, null, BigDecimal.ZERO, 0, false);
        }
    }
}
