package com.skinsense.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.skinsense.dto.AssessmentRequest;
import com.skinsense.dto.SkincareReport;
import com.skinsense.util.ProductImageResolver;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.net.URL;

@Service
public class PdfReportService {

    private final TemplateEngine templateEngine;
    private final ProductImageResolver productImageResolver;

    public PdfReportService(TemplateEngine templateEngine, ProductImageResolver productImageResolver) {
        this.templateEngine = templateEngine;
        this.productImageResolver = productImageResolver;
    }

    public byte[] createReportPdf(SkincareReport report, AssessmentRequest assessment) {
        Context context = new Context();
        context.setVariable("report", report);
        context.setVariable("assessment", assessment);
        context.setVariable("productImageResolver", productImageResolver);

        String html = templateEngine.process("report-pdf", context);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, getStaticResourceBaseUri());
            builder.toStream(outputStream);
            builder.run();
            return outputStream.toByteArray();
        } catch (Exception exception) {
            throw new IllegalStateException("Could not create the SkinSense PDF report.", exception);
        }
    }

    private String getStaticResourceBaseUri() {
        URL staticResources = getClass().getClassLoader().getResource("static/");
        return staticResources == null ? null : staticResources.toExternalForm();
    }
}
