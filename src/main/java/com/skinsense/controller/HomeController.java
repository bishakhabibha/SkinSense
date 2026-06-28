package com.skinsense.controller;

import com.skinsense.dto.AssessmentRequest;
import com.skinsense.dto.SkincareReport;
import com.skinsense.exception.GeminiException;
import com.skinsense.service.GeminiService;
import com.skinsense.service.PdfReportService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class HomeController {

    private final GeminiService geminiService;
    private final PdfReportService pdfReportService;

    public HomeController(GeminiService geminiService, PdfReportService pdfReportService) {
        this.geminiService = geminiService;
        this.pdfReportService = pdfReportService;
    }

    @GetMapping("/")
    public String showHomePage(Model model) {
        model.addAttribute("assessmentRequest", new AssessmentRequest());
        return "index";
    }

    @PostMapping(value = "/assessment", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String submitAssessment(@ModelAttribute AssessmentRequest assessmentRequest, Model model, HttpSession session) {
        try {
            SkincareReport report = geminiService.generateReport(assessmentRequest);
            session.setAttribute("latestReport", report);
            session.setAttribute("latestAssessment", assessmentRequest);
            model.addAttribute("report", report);
            model.addAttribute("assessment", assessmentRequest);
            return "results";
        } catch (GeminiException exception) {
            model.addAttribute("errorMessage", exception.getUserMessage());
            return "error";
        }
    }

    @GetMapping("/report/download")
    public ResponseEntity<byte[]> downloadReport(HttpSession session) {
        SkincareReport report = (SkincareReport) session.getAttribute("latestReport");
        AssessmentRequest assessment = (AssessmentRequest) session.getAttribute("latestAssessment");

        if (report == null || assessment == null) {
            return ResponseEntity.status(HttpStatus.SEE_OTHER)
                    .header(HttpHeaders.LOCATION, "/")
                    .build();
        }

        byte[] pdfBytes = pdfReportService.createReportPdf(report, assessment);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=skinsense-ai-report.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}
