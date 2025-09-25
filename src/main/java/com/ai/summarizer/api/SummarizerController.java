package com.ai.summarizer.api;

import com.ai.summarizer.service.SummarizerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class SummarizerController {
    private final SummarizerService summarizerService;

    @PostMapping("/summarize")
    public ResponseEntity<SummarizeResponse> summarize(SummarizeRequest request) {
        return ResponseEntity.ok(summarizerService.summarize(request));
    }
}
