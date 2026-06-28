package com.skinsense.controller;

import com.skinsense.dto.AssessmentRequest;
import com.skinsense.dto.SkincareReport;
import com.skinsense.exception.GeminiException;
import com.skinsense.service.GeminiService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class HomeController {

    private final GeminiService geminiService;

    public HomeController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    @GetMapping("/")
    public String showHomePage(Model model) {
        model.addAttribute("assessmentRequest", new AssessmentRequest());
        return "index";
    }

    @PostMapping("/assessment")
    public String submitAssessment(@ModelAttribute AssessmentRequest assessmentRequest, Model model) {
        try {
            SkincareReport report = geminiService.generateReport(assessmentRequest);
            model.addAttribute("report", report);
            return "results";
        } catch (GeminiException exception) {
            model.addAttribute("errorMessage", exception.getUserMessage());
            return "error";
        }
    }
}
