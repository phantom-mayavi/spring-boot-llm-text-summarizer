package com.ai.summarizer.service;

import com.ai.summarizer.api.SummarizeRequest;
import com.ai.summarizer.api.SummarizeResponse;
import org.springframework.stereotype.Service;

@Service
public class LlmSummarizerService implements SummarizerService {

    @Override
    public SummarizeResponse summarize(SummarizeRequest request) {
        return new SummarizeResponse("This is a placeholder summary.");
    }
}
