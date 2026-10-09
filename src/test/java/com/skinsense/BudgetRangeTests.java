package com.skinsense;

import com.skinsense.dto.AssessmentRequest;
import com.skinsense.dto.BudgetRange;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class BudgetRangeTests {

    @Test
    void allFourRequestValuesMapToTheirExactDisplayedLabels() {
        assertBudget("under-2000", BudgetRange.UNDER_2000, "Under ৳2,000");
        assertBudget("under-4000", BudgetRange.UNDER_4000, "Under ৳4,000");
        assertBudget("under-6000", BudgetRange.UNDER_6000, "Under ৳6,000");
        assertBudget("above-6000", BudgetRange.ABOVE_6000, "Above ৳6,000 (৳6,000 and above)");
    }

    @Test
    void strictCeilingsAndSixThousandBoundaryAreConsistent() {
        assertThat(BudgetRange.UNDER_2000.contains(new BigDecimal("1999.99"))).isTrue();
        assertThat(BudgetRange.UNDER_2000.contains(new BigDecimal("2000"))).isFalse();
        assertThat(BudgetRange.UNDER_4000.contains(new BigDecimal("4000"))).isFalse();
        assertThat(BudgetRange.UNDER_6000.contains(new BigDecimal("6000"))).isFalse();
        assertThat(BudgetRange.ABOVE_6000.contains(new BigDecimal("6000"))).isTrue();
        assertThat(BudgetRange.UNDER_6000.isOverBudget(new BigDecimal("6000"))).isTrue();
    }

    @Test
    void assessmentFormContainsOnlyTheFourSupportedBudgetValues() throws Exception {
        String html;
        try (var input = new ClassPathResource("templates/index.html").getInputStream()) {
            html = new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }

        assertThat(html).contains("<select name=\"budget\" required>")
                .contains("value=\"under-2000\"")
                .contains("value=\"under-4000\"")
                .contains("value=\"under-6000\"")
                .contains("value=\"above-6000\"")
                .doesNotContain("value=\"2000-4000\"")
                .doesNotContain("value=\"4000-6000\"")
                .doesNotContain("value=\"6000-plus\"");
    }

    private void assertBudget(String value, BudgetRange expected, String label) {
        AssessmentRequest request = new AssessmentRequest();
        request.setBudget(value);
        assertThat(BudgetRange.from(request.getBudget())).isEqualTo(expected);
        assertThat(request.getDisplayBudget()).isEqualTo(label);
    }
}
