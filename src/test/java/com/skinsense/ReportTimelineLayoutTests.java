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

        assertThat(template).contains("class=\"timeline-scroll\"")
                .contains("tabindex=\"0\"")
                .contains("Scrollable daily skincare steps");
        assertThat(css).contains("max-height: calc(100vh - 116px)")
                .contains(".timeline-scroll")
                .contains("overflow-y: auto")
                .contains("min-height: 0")
                .contains("scrollbar-gutter: stable");
    }

    @Test
    void mobileLayoutRemovesNestedTimelineScrolling() throws Exception {
        String css = resource("static/css/styles.css");

        assertThat(css).contains("@media (max-width: 760px)")
                .contains("max-height: none")
                .contains("overflow: visible");
    }

    private String resource(String path) throws Exception {
        try (var input = new ClassPathResource(path).getInputStream()) {
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
