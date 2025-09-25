package com.ai.summarizer.service;

import com.ai.summarizer.api.SummarizeRequest;
import com.ai.summarizer.api.SummarizeResponse;
import com.ai.summarizer.llm.LlmClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LlmSummarizerService implements SummarizerService {

    private final LlmClient llmClient;

    @Override
    public SummarizeResponse summarize(SummarizeRequest req) {
        String summary = llmClient.summarize(req.text(), req.maxSentences());
        if (summary == null || summary.isBlank()) throw new RuntimeException("Empty summary from LLM");
        return new SummarizeResponse(summary.trim());
    }
}
