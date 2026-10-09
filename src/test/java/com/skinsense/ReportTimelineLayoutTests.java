package com.skinsense;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class ReportTimelineLayoutTests {

    @Test
    void timelineHasAnIndependentBoundedScrollRegionAndAccessibleFinalContent() throws Exception {
        String template = resource("templates/results.html");
        String css = resource("static/css/styles.css");
        String javascript = resource("static/js/app.js");

        assertThat(template).contains("class=\"timeline-scroll-shell\"")
                .contains("class=\"timeline-scroll\"")
                .contains("class=\"timeline-scrollbar\"")
                .contains("class=\"timeline-scrollbar-thumb\"")
                .contains("tabindex=\"0\"")
                .contains("Scrollable daily skincare steps")
                .contains("@{/js/app.js}");
        assertThat(css).contains(".dashboard-sidebar")
                .contains("position: static")
                .contains(".timeline-scroll")
                .contains("overflow-y: auto")
                .contains("scrollbar-width: none")
                .contains(".timeline-scrollbar-thumb")
                .contains("cursor: grab")
                .contains("max-height: clamp(300px, 58vh, 560px)")
                .contains("min-height: 0")
                .contains("prefers-color-scheme: dark");
        assertThat(javascript).contains("thumb.setPointerCapture(event.pointerId)")
                .contains("pointermove")
                .contains("scroller.scrollTop")
                .contains("visibleHeight / contentHeight")
                .contains("scroller.addEventListener(\"keydown\"")
                .contains("PageDown: pageDistance")
                .contains("new ResizeObserver(updateScrollbar)")
                .contains("new MutationObserver(updateScrollbar)");
    }

    @Test
    void mobileLayoutKeepsAResponsiveIndependentTimelineScroller() throws Exception {
        String css = resource("static/css/styles.css");

        assertThat(css).contains("@media (max-width: 760px)")
                .contains("max-height: clamp(260px, 52vh, 420px)")
                .doesNotContain(".timeline-scroll {\r\n        overflow: visible")
                .doesNotContain(".timeline-scroll {\n        overflow: visible");
    }

    @Test
    void reportsDistinguishRequestedStepsProductsAndPlacements() throws Exception {
        String html = resource("templates/results.html");
        String pdf = resource("templates/report-pdf.html");

        assertThat(html).contains("Requested routine length:")
                .contains("Actual routine steps:")
                .contains("Unique products to purchase:")
                .contains("AM/PM product placements:")
                .contains("Routine-length status:")
                .contains("Verified size")
                .contains("How to use");
        assertThat(pdf).contains("Requested routine length:")
                .contains("Actual routine steps:")
                .contains("Unique products to purchase:")
                .contains("AM/PM product placements:")
                .contains("Routine-length status:")
                .contains("How to use:");
    }

    private String resource(String path) throws Exception {
        try (var input = new ClassPathResource(path).getInputStream()) {
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
