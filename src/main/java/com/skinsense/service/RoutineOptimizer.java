package com.skinsense.service;

import com.skinsense.dto.AssessmentRequest;
import com.skinsense.dto.BudgetRange;
import com.skinsense.dto.ProductMatch;
import com.skinsense.dto.RoutineProductStep;
import com.skinsense.dto.RoutineLengthPreference;
import com.skinsense.dto.SkinAnalysis;
import com.skinsense.dto.SkincareRoutine;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class RoutineOptimizer {

    private static final int MAX_CANDIDATES_PER_CATEGORY = 12;
    private static final int ESSENTIAL_COMPLETENESS_POINTS = 240;
    private static final int NEEDED_MOISTURIZER_POINTS = 40;
    private static final int RELEVANT_TREATMENT_POINTS = 18;
    private static final int APPROPRIATE_FIRST_CLEANSE_POINTS = 14;
    private static final int USEFUL_SUPPORT_STEP_POINTS = 10;
    private static final int STEP_TARGET_POINTS = 45;

    private final ProductMatcher productMatcher;

    public RoutineOptimizer(ProductMatcher productMatcher) {
        this.productMatcher = productMatcher;
    }

    public SkincareRoutine optimize(SkinAnalysis analysis, AssessmentRequest assessmentRequest) {
        BudgetRange budgetRange = BudgetRange.from(assessmentRequest == null ? null : assessmentRequest.getBudget());
        RoutineLengthPreference lengthPreference = RoutineLengthPreference.from(
                assessmentRequest == null ? null : assessmentRequest.getRoutinePreference()
        );

        List<ProductMatch> cleansers = topCandidates("cleanser", analysis, assessmentRequest);
        List<ProductMatch> moisturizers = moisturizerCandidates(analysis, assessmentRequest);
        List<ProductMatch> sunscreens = topCandidates("sunscreen", analysis, assessmentRequest);
        List<ProductMatch> treatments = treatmentCandidates(analysis, assessmentRequest);
        List<ProductMatch> firstCleansers = firstCleanseCandidates(analysis, assessmentRequest);
        ProductMatch toner = bestSuitableToner(analysis, assessmentRequest);
        ProductMatch hydratingSerum = bestHydratingSerum(analysis, assessmentRequest);

        List<CandidateRoutine> candidates = new ArrayList<>();
        for (ProductMatch cleanser : cleansers) {
            for (ProductMatch moisturizer : moisturizers) {
                for (ProductMatch sunscreen : sunscreens) {
                    for (ProductMatch treatment : treatments) {
                        for (ProductMatch firstCleanser : firstCleansers) {
                            CandidateRoutine candidate = evaluate(
                                    analysis, budgetRange, cleanser, moisturizer, sunscreen, treatment, firstCleanser
                            );
                            if (candidate.complete()) {
                                CandidateRoutine expanded = addUsefulSupportSteps(
                                        candidate, toner, hydratingSerum, budgetRange, analysis,
                                        assessmentRequest, lengthPreference
                                );
                                candidates.add(scoreRoutineLength(expanded, lengthPreference));
                            }
                        }
                    }
                }
            }
        }

        return chooseBestRoutine(candidates, budgetRange, assessmentRequest);
    }

    private List<ProductMatch> topCandidates(String category, SkinAnalysis analysis, AssessmentRequest request) {
        return productMatcher.rankProducts(category, analysis, request).stream()
                .limit(MAX_CANDIDATES_PER_CATEGORY)
                .toList();
    }

    private List<ProductMatch> moisturizerCandidates(SkinAnalysis analysis, AssessmentRequest request) {
        if (!analysis.getRoutineNeeds().isMoisturizer()) {
            return optionalNone(List.of());
        }
        return topCandidates("moisturizer", analysis, request);
    }

    private List<ProductMatch> treatmentCandidates(SkinAnalysis analysis, AssessmentRequest request) {
        if (!needsTargetedTreatment(analysis, request)) {
            return optionalNone(List.of());
        }

        List<String> explicitlySelectedConcerns = request.getConcerns().stream()
                .map(this::normalize)
                .toList();
        List<ProductMatch> relevantTreatments = topCandidates("treatment", analysis, request).stream()
                .filter(match -> match.getMatchedConcerns().stream()
                        .map(this::normalize)
                        .anyMatch(explicitlySelectedConcerns::contains))
                .toList();
        return optionalNone(relevantTreatments);
    }

    private List<ProductMatch> firstCleanseCandidates(SkinAnalysis analysis, AssessmentRequest request) {
        if (!shouldConsiderFirstCleanse(request)) {
            return optionalNone(List.of());
        }
        return optionalNone(topCandidates("first_cleanse", analysis, request));
    }

    private List<ProductMatch> optionalNone(List<ProductMatch> candidates) {
        List<ProductMatch> options = new ArrayList<>();
        options.add(null);
        options.addAll(candidates);
        return options;
    }

    private ProductMatch bestSuitableToner(SkinAnalysis analysis, AssessmentRequest request) {
        return topCandidates("toner", analysis, request).stream().findFirst().orElse(null);
    }

    private ProductMatch bestHydratingSerum(SkinAnalysis analysis, AssessmentRequest request) {
        return topCandidates("hydrating_serum", analysis, request).stream().findFirst().orElse(null);
    }

    private CandidateRoutine addUsefulSupportSteps(
            CandidateRoutine candidate,
            ProductMatch toner,
            ProductMatch hydratingSerum,
            BudgetRange budgetRange,
            SkinAnalysis analysis,
            AssessmentRequest request,
            RoutineLengthPreference lengthPreference
    ) {
        if (request == null || lengthPreference == RoutineLengthPreference.MINIMAL) {
            return candidate;
        }

        String skinType = normalize(analysis.getSkinType());
        String sensitivity = normalize(analysis.getSensitivity());
        boolean hydrationSupportIsUseful = Set.of("dry", "sensitive").contains(skinType)
                || Set.of("moderate", "high").contains(sensitivity)
                || analysis.getConcerns().stream().map(this::normalize).anyMatch("redness"::equals);

        CandidateRoutine enhanced = candidate;
        boolean tonerHasAssessmentMatch = toner != null
                && (!toner.getMatchedConcerns().isEmpty() || !toner.getMatchedIngredients().isEmpty());
        if (toner != null && (hydrationSupportIsUseful || tonerHasAssessmentMatch)) {
            enhanced = addSupportProduct(enhanced, toner, true, budgetRange, lengthPreference.targetStepCount());
        }

        if (!hydrationSupportIsUseful) {
            return enhanced;
        }

        if (hydratingSerum != null) {
            enhanced = addSupportProduct(
                    enhanced, hydratingSerum, false, budgetRange, lengthPreference.targetStepCount()
            );
        }
        return enhanced;
    }

    private CandidateRoutine addSupportProduct(
            CandidateRoutine candidate,
            ProductMatch supportProduct,
            boolean toner,
            BudgetRange budgetRange,
            int targetStepCount
    ) {
        if (meaningfulStepCount(candidate) >= targetStepCount) {
            return candidate;
        }
        List<ProductMatch> products = new ArrayList<>(Arrays.asList(
                candidate.cleanser(), candidate.moisturizer(), candidate.sunscreen(), candidate.treatment(),
                candidate.firstCleanser(), candidate.toner(), candidate.hydratingSerum()
        ));
        products.add(supportProduct);
        BigDecimal newTotal = totalUniqueCost(products);
        if (!budgetRange.contains(newTotal)
                && budgetRange.distanceFromRange(newTotal).compareTo(budgetRange.distanceFromRange(candidate.totalCost())) > 0) {
            return candidate;
        }
        return new CandidateRoutine(
                candidate.cleanser(), candidate.moisturizer(), candidate.sunscreen(), candidate.treatment(),
                candidate.firstCleanser(), toner ? supportProduct : candidate.toner(),
                toner ? candidate.hydratingSerum() : supportProduct, newTotal,
                candidate.score() + supportProduct.getCompatibilityScore() + USEFUL_SUPPORT_STEP_POINTS,
                candidate.complete()
        );
    }

    private CandidateRoutine scoreRoutineLength(
            CandidateRoutine candidate,
            RoutineLengthPreference lengthPreference
    ) {
        int achievedSteps = Math.min(meaningfulStepCount(candidate), lengthPreference.targetStepCount());
        return new CandidateRoutine(
                candidate.cleanser(), candidate.moisturizer(), candidate.sunscreen(), candidate.treatment(),
                candidate.firstCleanser(), candidate.toner(), candidate.hydratingSerum(), candidate.totalCost(),
                candidate.score() + achievedSteps * STEP_TARGET_POINTS, candidate.complete()
        );
    }

    private boolean needsTargetedTreatment(SkinAnalysis analysis, AssessmentRequest request) {
        return analysis.getRoutineNeeds().isTreatment()
                && request != null
                && request.getConcerns() != null
                && !request.getConcerns().isEmpty();
    }

    private boolean shouldConsiderFirstCleanse(AssessmentRequest request) {
        if (request == null) {
            return false;
        }
        String usage = normalize(request.getSunscreenUsage());
        String makeupUsage = normalize(request.getMakeupUsage());
        return "always".equals(usage)
                || Set.of("often", "daily", "water_resistant").contains(makeupUsage);
    }

    private CandidateRoutine evaluate(
            SkinAnalysis analysis,
            BudgetRange budgetRange,
            ProductMatch cleanser,
            ProductMatch moisturizer,
            ProductMatch sunscreen,
            ProductMatch treatment,
            ProductMatch firstCleanser
    ) {
        if (cleanser == null || sunscreen == null) {
            return CandidateRoutine.incomplete();
        }
        if (analysis.getRoutineNeeds().isMoisturizer() && moisturizer == null) {
            return CandidateRoutine.incomplete();
        }

        List<ProductMatch> products = Arrays.asList(cleanser, moisturizer, sunscreen, treatment, firstCleanser);
        BigDecimal totalCost = totalUniqueCost(products);
        int score = productScore(products) + ESSENTIAL_COMPLETENESS_POINTS;
        if (moisturizer != null && analysis.getRoutineNeeds().isMoisturizer()) {
            score += NEEDED_MOISTURIZER_POINTS;
        }
        if (treatment != null) {
            score += RELEVANT_TREATMENT_POINTS;
        }
        if (firstCleanser != null) {
            score += APPROPRIATE_FIRST_CLEANSE_POINTS;
        }
        if (budgetRange.contains(totalCost)) {
            score += 30;
        }

        return new CandidateRoutine(cleanser, moisturizer, sunscreen, treatment, firstCleanser,
                null, null, totalCost, score, true);
    }

    private SkincareRoutine chooseBestRoutine(
            List<CandidateRoutine> candidates,
            BudgetRange budgetRange,
            AssessmentRequest request
    ) {
        Comparator<CandidateRoutine> bestScore = Comparator.comparingInt(CandidateRoutine::score)
                .thenComparing(CandidateRoutine::totalCost, Comparator.reverseOrder());

        List<CandidateRoutine> exactFits = candidates.stream()
                .filter(candidate -> budgetRange.contains(candidate.totalCost()))
                .toList();

        CandidateRoutine selected;
        boolean exactFit;
        if (!exactFits.isEmpty()) {
            selected = exactFits.stream().max(bestScore).orElseThrow();
            exactFit = true;
        } else {
            selected = candidates.stream()
                    .min(Comparator.comparing((CandidateRoutine candidate) -> budgetRange.distanceFromRange(candidate.totalCost()))
                            .thenComparing(Comparator.comparingInt(CandidateRoutine::score).reversed()))
                    .orElse(CandidateRoutine.incomplete());
            exactFit = false;
        }

        return toRoutine(selected, budgetRange, exactFit, request);
    }

    private SkincareRoutine toRoutine(
            CandidateRoutine candidate,
            BudgetRange budgetRange,
            boolean exactFit,
            AssessmentRequest request
    ) {
        SkincareRoutine routine = new SkincareRoutine();
        routine.setBudgetRange(budgetRange);
        routine.setRequestedStepCount(request == null ? 3 : request.getRequestedRoutineStepCount());
        routine.setExactBudgetFit(exactFit);
        routine.setComplete(candidate.complete());
        routine.setRoutineScore(candidate.score());
        routine.setTotalCost(candidate.totalCost());
        routine.setCurrency("BDT");

        if (!candidate.complete()) {
            routine.setFallbackMessage("SkinSense could not build a complete routine with verified regular-use cleanser, moisturizer, and full-size sunscreen options from the catalog.");
            return routine;
        }

        List<RoutineProductStep> morning = new ArrayList<>();
        List<RoutineProductStep> evening = new ArrayList<>();

        morning.add(step("morning", "Cleanser", morning.size() + 1, false,
                "Use as part of the morning routine.", candidate.cleanser()));
        if (candidate.toner() != null) {
            morning.add(step("morning", "Toner", morning.size() + 1, true,
                    "Apply after cleansing for hydration and comfort.", candidate.toner()));
        }
        if (candidate.moisturizer() != null) {
            morning.add(step("morning", "Moisturizer", morning.size() + 1, false,
                    "Use as needed for comfort and barrier support.", candidate.moisturizer()));
        }
        morning.add(step("morning", "Sunscreen", morning.size() + 1, false,
                "Apply an adequate amount every morning and reapply during extended outdoor exposure.", candidate.sunscreen()));

        if (candidate.firstCleanser() != null) {
            evening.add(step("evening", "First Cleanse", evening.size() + 1, true,
                    "Use first in the evening to help remove daily sunscreen, then follow with the regular cleanser.", candidate.firstCleanser()));
        }
        evening.add(step("evening", "Cleanser", evening.size() + 1, false,
                "Use as the regular water-based evening cleanse.", candidate.cleanser()));
        if (candidate.hydratingSerum() != null) {
            evening.add(step("evening", "Hydrating Serum", evening.size() + 1, true,
                    "Apply after cleansing and before moisturizer; reduce frequency if irritation occurs.", candidate.hydratingSerum()));
        }
        if (candidate.treatment() != null) {
            evening.add(step("evening", "Treatment", evening.size() + 1, true,
                    "Use according to product directions and skin tolerance.", candidate.treatment()));
        }
        if (candidate.moisturizer() != null) {
            evening.add(step("evening", "Moisturizer", evening.size() + 1, false,
                    "Finish with moisturizer for comfort and barrier support.", candidate.moisturizer()));
        }

        routine.setMorning(morning);
        routine.setEvening(evening);
        routine.setExplanationMetadata(explanationMetadata(candidate, exactFit, budgetRange, request, routine));

        if (!exactFit) {
            routine.setFallbackMessage("No complete routine satisfies the selected total-budget boundary. The displayed routine is the closest compatible set of regular-use essentials from the verified catalog and is not marked as a budget fit.");
        } else if (!routine.isRequestedLengthMet()) {
            routine.setFallbackMessage("The requested " + routine.getRequestedStepCount()
                    + "-step routine could not be filled with distinct, suitable products within the selected budget and available verified categories. "
                    + "SkinSense generated " + routine.getActualStepCount() + " meaningful steps without dropping essentials or adding filler products.");
        }

        return routine;
    }

    private RoutineProductStep step(
            String period,
            String stepName,
            int order,
            boolean optional,
            String frequency,
            ProductMatch productMatch
    ) {
        RoutineProductStep step = new RoutineProductStep();
        step.setPeriod(period);
        step.setStepName(stepName);
        step.setOrder(order);
        step.setOptional(optional);
        step.setFrequency(frequency);
        step.setProductMatch(productMatch);
        return step;
    }

    private List<String> explanationMetadata(
            CandidateRoutine candidate,
            boolean exactFit,
            BudgetRange budgetRange,
            AssessmentRequest request,
            SkincareRoutine routine
    ) {
        List<String> metadata = new ArrayList<>();
        metadata.add("Budget choice: " + budgetRange.getLabel());
        metadata.add("Exact total-budget fit: " + exactFit);
        metadata.add("Total unique product cost: BDT " + candidate.totalCost());
        metadata.add("Unique products: " + uniqueProductCount(candidate));
        metadata.add("Requested meaningful steps: " + routine.getRequestedStepCount());
        metadata.add("Actual meaningful steps: " + routine.getActualStepCount());
        metadata.add("AM/PM product placements: " + routine.getProductPlacementCount());
        metadata.add("Same product reused across AM/PM is one purchase and one meaningful step, but each use is one placement.");
        if (!routine.isRequestedLengthMet()) {
            metadata.add("A shorter routine was retained because the verified catalog, selected needs, and budget did not justify filler products.");
        }
        return metadata;
    }

    private int meaningfulStepCount(CandidateRoutine candidate) {
        return (int) Arrays.asList(candidate.cleanser(), candidate.moisturizer(), candidate.sunscreen(),
                        candidate.treatment(), candidate.firstCleanser(), candidate.toner(), candidate.hydratingSerum())
                .stream()
                .filter(match -> match != null && match.getProduct() != null)
                .count();
    }

    private int uniqueProductCount(CandidateRoutine candidate) {
        return (int) Arrays.asList(candidate.cleanser(), candidate.moisturizer(), candidate.sunscreen(),
                        candidate.treatment(), candidate.firstCleanser(), candidate.toner(), candidate.hydratingSerum()).stream()
                .filter(match -> match != null && match.getProduct() != null)
                .map(match -> match.getProduct().getId())
                .distinct()
                .count();
    }

    private BigDecimal totalUniqueCost(List<ProductMatch> matches) {
        Map<String, BigDecimal> uniquePrices = new LinkedHashMap<>();
        for (ProductMatch match : matches) {
            if (match == null || match.getProduct() == null || match.getProduct().getId() == null) {
                continue;
            }
            uniquePrices.putIfAbsent(match.getProduct().getId(), match.getProduct().getPrice());
        }
        return uniquePrices.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private int productScore(List<ProductMatch> matches) {
        return matches.stream().filter(match -> match != null).mapToInt(ProductMatch::getCompatibilityScore).sum();
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
    }

    private record CandidateRoutine(
            ProductMatch cleanser,
            ProductMatch moisturizer,
            ProductMatch sunscreen,
            ProductMatch treatment,
            ProductMatch firstCleanser,
            ProductMatch toner,
            ProductMatch hydratingSerum,
            BigDecimal totalCost,
            int score,
            boolean complete
    ) {
        private static CandidateRoutine incomplete() {
            return new CandidateRoutine(null, null, null, null, null, null, null, BigDecimal.ZERO, 0, false);
        }
    }
}
