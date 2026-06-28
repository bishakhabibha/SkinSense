package com.skinsense.dto;

import java.util.ArrayList;
import java.util.List;

public class GeminiGenerateResponse {

    private List<GeminiCandidate> candidates = new ArrayList<>();

    public List<GeminiCandidate> getCandidates() {
        return candidates;
    }

    public void setCandidates(List<GeminiCandidate> candidates) {
        this.candidates = candidates == null ? new ArrayList<>() : candidates;
    }
}
